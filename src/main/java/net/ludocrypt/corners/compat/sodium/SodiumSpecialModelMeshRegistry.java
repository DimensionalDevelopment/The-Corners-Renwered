package net.ludocrypt.corners.compat.sodium;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class SodiumSpecialModelMeshRegistry {

    private static final ConcurrentLinkedQueue<PendingSectionMeshes> pendingSections = new ConcurrentLinkedQueue<>();
    private static final Map<Long, UploadedSectionMeshes> uploadedSections = new LinkedHashMap<>();

    public static PendingMeshBuilder createBuilder(RenderType renderType) {
        ByteBufferBuilder byteBuffer = new ByteBufferBuilder(renderType.bufferSize());
        return new PendingMeshBuilder(byteBuffer, new BufferBuilder(byteBuffer, renderType.mode(), renderType.format()));
    }

    public static void submit(SectionPos sectionPos, Map<RenderType, PendingMeshBuilder> builders) {
        Map<RenderType, PendingMesh> meshes = new LinkedHashMap<>();

        for (Map.Entry<RenderType, PendingMeshBuilder> entry : builders.entrySet()) {
            PendingMesh mesh = entry.getValue().build();

            if (mesh != null) {
                meshes.put(entry.getKey(), mesh);
            }
        }

        pendingSections.add(new PendingSectionMeshes(sectionPos.asLong(), meshes));
    }

    public static void remove(SectionPos sectionPos) {
        pendingSections.add(new PendingSectionMeshes(sectionPos.asLong(), Map.of()));
    }

    public static void renderAll(double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        uploadPending();

        if (uploadedSections.isEmpty()) {
            return;
        }

        for (RenderType renderType : CornerRenderTypes.chunkBufferLayers()) {
            boolean drawing = false;

            for (UploadedSectionMeshes sectionMeshes : uploadedSections.values()) {
                VertexBuffer vertexBuffer = sectionMeshes.meshes().get(renderType);

                if (vertexBuffer == null) {
                    continue;
                }

                if (!drawing) {
                    renderType.setupRenderState();
                    drawing = true;
                }

                ShaderInstance shader = RenderSystem.getShader();

                if (shader != null) {
                    vertexBuffer.bind();
                    shader.setDefaultUniforms(renderType.mode(), modelViewMatrix, projectionMatrix, Minecraft.getInstance().getWindow());

                    if (shader.CHUNK_OFFSET != null) {
                        shader.CHUNK_OFFSET.set((float) -cameraX, (float) -cameraY, (float) -cameraZ);
                    }

                    shader.apply();
                    vertexBuffer.draw();

                    if (shader.CHUNK_OFFSET != null) {
                        shader.CHUNK_OFFSET.set(0.0F, 0.0F, 0.0F);
                        shader.CHUNK_OFFSET.upload();
                    }

                    shader.clear();
                }
            }

            if (drawing) {
                VertexBuffer.unbind();
                renderType.clearRenderState();
            }
        }
    }

    public static void clear() {
        PendingSectionMeshes pending;

        while ((pending = pendingSections.poll()) != null) {
            pending.close();
        }

        uploadedSections.values().forEach(UploadedSectionMeshes::close);
        uploadedSections.clear();
    }

    private static void uploadPending() {
        PendingSectionMeshes pending;

        while ((pending = pendingSections.poll()) != null) {
            UploadedSectionMeshes previous = uploadedSections.remove(pending.sectionKey());

            if (previous != null) {
                previous.close();
            }

            if (pending.meshes().isEmpty()) {
                continue;
            }

            uploadedSections.put(pending.sectionKey(), pending.upload());
        }
    }

    public record PendingMeshBuilder(ByteBufferBuilder byteBuffer, BufferBuilder buffer) {

        @Nullable
        private PendingMesh build() {
            MeshData meshData = this.buffer.build();

            if (meshData == null) {
                this.byteBuffer.close();
                return null;
            }

            return new PendingMesh(meshData, this.byteBuffer);
        }
    }

    private record PendingMesh(MeshData meshData, ByteBufferBuilder byteBuffer) {

        private VertexBuffer upload() {
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();

            try {
                vertexBuffer.upload(this.meshData);
            } finally {
                VertexBuffer.unbind();
                this.byteBuffer.close();
            }

            return vertexBuffer;
        }

        private void close() {
            try {
                this.meshData.close();
            } finally {
                this.byteBuffer.close();
            }
        }
    }

    private record PendingSectionMeshes(long sectionKey, Map<RenderType, PendingMesh> meshes) {

        private UploadedSectionMeshes upload() {
            Map<RenderType, VertexBuffer> uploaded = new LinkedHashMap<>();

            try {
                for (Map.Entry<RenderType, PendingMesh> entry : this.meshes.entrySet()) {
                    uploaded.put(entry.getKey(), entry.getValue().upload());
                }
            } catch (RuntimeException exception) {
                uploaded.values().forEach(VertexBuffer::close);
                throw exception;
            }

            return new UploadedSectionMeshes(uploaded);
        }

        private void close() {
            this.meshes.values().forEach(PendingMesh::close);
        }
    }

    private record UploadedSectionMeshes(Map<RenderType, VertexBuffer> meshes) {

        private void close() {
            this.meshes.values().forEach(VertexBuffer::close);
        }
    }

    private SodiumSpecialModelMeshRegistry() {
    }
}
