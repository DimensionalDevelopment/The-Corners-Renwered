package net.ludocrypt.corners.compat.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.ludocrypt.corners.mixin.sodium.DefaultTerrainRenderPassesAccessor;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SodiumSpecialModelRenderPasses {

    private static final Map<RenderType, TerrainRenderPass> PASSES = new LinkedHashMap<>();
    private static final Map<RenderType, Material> MATERIALS = new LinkedHashMap<>();

    public static synchronized TerrainRenderPass register(RenderType renderType) {
        TerrainRenderPass existing = PASSES.get(renderType);

        if (existing != null) {
            return existing;
        }

        TerrainRenderPass pass = new TerrainRenderPass(renderType, false, false);
        PASSES.put(renderType, pass);
        MATERIALS.put(renderType, new Material(pass, AlphaCutoffParameter.ZERO, true));
        appendToDefaultPasses(pass);
        return pass;
    }

    @Nullable
    public static synchronized TerrainRenderPass getPass(RenderType renderType) {
        return PASSES.get(renderType);
    }

    @Nullable
    public static synchronized Material getMaterial(RenderType renderType) {
        Material material = MATERIALS.get(renderType);

        if (material == null) {
            register(renderType);
            material = MATERIALS.get(renderType);
        }

        return material;
    }

    public static synchronized Iterable<TerrainRenderPass> passes() {
        return List.copyOf(PASSES.values());
    }

    private static void appendToDefaultPasses(TerrainRenderPass pass) {
        TerrainRenderPass[] passes = DefaultTerrainRenderPasses.ALL;

        if (Arrays.asList(passes).contains(pass)) {
            return;
        }

        TerrainRenderPass[] modifiedPasses = Arrays.copyOf(passes, passes.length + 1);
        modifiedPasses[passes.length] = pass;
        DefaultTerrainRenderPassesAccessor.corners$setAll(modifiedPasses);
    }

    private SodiumSpecialModelRenderPasses() {
    }
}
