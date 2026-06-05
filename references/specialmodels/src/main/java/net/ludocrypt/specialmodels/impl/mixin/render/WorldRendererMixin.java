package net.ludocrypt.specialmodels.impl.mixin.render;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.framebuffer.Framebuffer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.ludocrypt.specialmodels.impl.access.WorldChunkBuilderAccess;
import net.ludocrypt.specialmodels.impl.access.WorldRendererAccess;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.BuiltChunk;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.ChunkInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

@Mixin(value = WorldRenderer.class, priority = 900)
public abstract class WorldRendererMixin implements WorldRendererAccess, WorldChunkBuilderAccess {

	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	private ClientWorld world;

	@Shadow
	@Nullable
	private Framebuffer translucentFramebuffer;

	@Unique
	private double lastSpecialSortX;
	@Unique
	private double lastSpecialSortY;
	@Unique
	private double lastSpecialSortZ;

	@Override
	public void render(MatrixStack matrices, Matrix4f positionMatrix, float tickDelta, Camera camera, boolean outside) {

		ObjectListIterator<ChunkInfo> chunkInfos = this
			.getSpecialChunkInfoList()
			.listIterator(this.getSpecialChunkInfoList().size());

		while (chunkInfos.hasPrevious()) {
			ChunkInfo chunkInfo = chunkInfos.previous();
			BuiltChunk builtChunk = chunkInfo.chunk;
			builtChunk.getSpecialModelBuffers().forEach((modelRenderer, vertexBuffer) -> {

				if (modelRenderer.performOutside == outside) {

					if (builtChunk.getData().renderedBuffers.containsKey(modelRenderer)) {

						specialModels$renderBuffer(matrices, tickDelta, camera, positionMatrix, modelRenderer, vertexBuffer,
							builtChunk.getOrigin().toImmutable());

					}

				}

			});
		}

	}

	@Unique
	public void specialModels$renderBuffer(MatrixStack matrices, float tickDelta, Camera camera, Matrix4f positionMatrix,
			SpecialModelRenderer modelRenderer, VertexBuffer vertexBuffer, BlockPos origin) {
		ShaderProgram shader = modelRenderer
			.getShaderProgram(matrices, tickDelta, camera, positionMatrix, modelRenderer, vertexBuffer, origin);

		if (shader != null && ((VertexBufferAccessor) vertexBuffer).getIndexCount() > 0) {

			this.client.getProfiler().push("translucent_sort");
			double d = camera.getPos().getX() - this.lastSpecialSortX;
			double e = camera.getPos().getY() - this.lastSpecialSortY;
			double f = camera.getPos().getZ() - this.lastSpecialSortZ;

			if (d * d + e * e + f * f > 1.0) {
				int i = ChunkSectionPos.getSectionCoord(camera.getPos().getX());
				int j = ChunkSectionPos.getSectionCoord(camera.getPos().getY());
				int k = ChunkSectionPos.getSectionCoord(camera.getPos().getZ());
				boolean bl = i != ChunkSectionPos.getSectionCoord(this.lastSpecialSortX) || k != ChunkSectionPos
					.getSectionCoord(this.lastSpecialSortZ) || j != ChunkSectionPos.getSectionCoord(this.lastSpecialSortY);
				this.lastSpecialSortX = camera.getPos().getX();
				this.lastSpecialSortY = camera.getPos().getY();
				this.lastSpecialSortZ = camera.getPos().getZ();
				int l = 0;

				for (ChunkInfo chunkInfo : this.getSpecialChunkInfoList()) {

					if (l < 15 && (bl || chunkInfo.isAxisAlignedWith(i, j, k)) && chunkInfo.chunk
						.scheduleSort(modelRenderer, this.getSpecialChunkBuilder())) {
						++l;
					}

				}

			}

			this.client.getProfiler().pop();

			RenderSystem.depthMask(true);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem
				.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
					GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.polygonOffset(3.0F, 3.0F);
			RenderSystem.enablePolygonOffset();
			RenderSystem.setShader(() -> shader);
			client.gameRenderer.getLightmapTextureManager().enable();
			vertexBuffer.bind();
			Matrix4f viewMatrix = modelRenderer.viewMatrix(new Matrix4f(matrices.peek().getModel()));
			Matrix4f projectionMatrix = modelRenderer.positionMatrix(new Matrix4f(positionMatrix));
			modelRenderer
				.setup(matrices, new Matrix4f(viewMatrix), new Matrix4f(projectionMatrix), tickDelta, shader, origin);

			if (origin != null) {

				if (shader.chunkOffset != null) {
					BlockPos blockPos = origin;
					float vx = (float) (blockPos.getX() - camera.getPos().getX());
					float vy = (float) (blockPos.getY() - camera.getPos().getY());
					float vz = (float) (blockPos.getZ() - camera.getPos().getZ());
					shader.chunkOffset.setVec3(vx, vy, vz);
				}

			}

			vertexBuffer.draw(viewMatrix, projectionMatrix, shader);

			if (shader.chunkOffset != null) {
				shader.chunkOffset.setVec3(0.0F, 0.0F, 0.0F);
			}

			VertexBuffer.unbind();
			client.gameRenderer.getLightmapTextureManager().disable();
			RenderSystem.polygonOffset(0.0F, 0.0F);
			RenderSystem.disablePolygonOffset();
			RenderSystem.disableBlend();

		}

	}

	@Shadow
	abstract void renderEntity(Entity entity, double cameraX, double cameraY, double cameraZ, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers);

}
