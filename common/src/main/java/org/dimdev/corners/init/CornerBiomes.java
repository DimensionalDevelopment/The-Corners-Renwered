package org.dimdev.corners.init;

import org.dimdev.corners.TheCorners;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;

public class CornerBiomes {

    public static final ResourceKey<Biome> YEARNING_CANAL_BIOME = ResourceKey
            .create(Registries.BIOME, TheCorners.id(CornerWorlds.YEARNING_CANAL));
    public static final ResourceKey<Biome> COMMUNAL_CORRIDORS_BIOME = ResourceKey
            .create(Registries.BIOME, TheCorners.id(CornerWorlds.COMMUNAL_CORRIDORS));
    public static final ResourceKey<Biome> HOARY_CROSSROADS_BIOME = ResourceKey
            .create(Registries.BIOME, TheCorners.id(CornerWorlds.HOARY_CROSSROADS));
    public static final ResourceKey<Feature<?>> GAIA_TREE_FEATURE = ResourceKey
            .create(Registries.FEATURE, TheCorners.id("gaia_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_GAIA_TREE_FEATURE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, TheCorners.id("gaia_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> CONFIGURED_SAPLING_GAIA_TREE_FEATURE = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, TheCorners.id("gaia_sapling"));
}
