package net.ludocrypt.corners.mixin.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public interface DefaultTerrainRenderPassesAccessor {

    @Mutable
    @Accessor("ALL")
    static void corners$setAll(TerrainRenderPass[] passes) {
        throw new AssertionError();
    }
}
