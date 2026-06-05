package net.ludocrypt.corners.compat.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

public interface SodiumChunkBuildBuffersExt {

    ChunkModelBuilder corners$getOrCreateSpecialModelBuilder(TerrainRenderPass pass);
}
