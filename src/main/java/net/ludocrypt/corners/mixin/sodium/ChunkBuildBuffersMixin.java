package net.ludocrypt.corners.mixin.sodium;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.caffeinemc.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.BakedChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.ludocrypt.corners.compat.sodium.SodiumChunkBuildBuffersExt;
import net.ludocrypt.corners.compat.sodium.SodiumSpecialModelRenderPasses;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkBuildBuffers.class, remap = false)
public abstract class ChunkBuildBuffersMixin implements SodiumChunkBuildBuffersExt {

    @Shadow
    @Final
    private Reference2ReferenceOpenHashMap<TerrainRenderPass, BakedChunkModelBuilder> builders;

    @Shadow
    @Final
    private ChunkVertexType vertexType;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void corners$addSpecialModelBuilders(ChunkVertexType vertexType, CallbackInfo ci) {
        for (TerrainRenderPass pass : SodiumSpecialModelRenderPasses.passes()) {
            corners$getOrCreateSpecialModelBuilder(pass);
        }
    }

    @Override
    public ChunkModelBuilder corners$getOrCreateSpecialModelBuilder(TerrainRenderPass pass) {
        BakedChunkModelBuilder builder = this.builders.get(pass);

        if (builder == null) {
            builder = corners$createBuilder();
            this.builders.put(pass, builder);
        }

        return builder;
    }

    @Unique
    private BakedChunkModelBuilder corners$createBuilder() {
        ChunkMeshBufferBuilder[] vertexBuffers = new ChunkMeshBufferBuilder[ModelQuadFacing.COUNT];

        for (int i = 0; i < ModelQuadFacing.COUNT; i++) {
            vertexBuffers[i] = new ChunkMeshBufferBuilder(this.vertexType, 131072);
        }

        return new BakedChunkModelBuilder(vertexBuffers);
    }
}
