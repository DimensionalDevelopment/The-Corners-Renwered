package net.ludocrypt.corners.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.client.render.StrongPostEffect;
import net.ludocrypt.corners.init.CornerBiomes;
import net.ludocrypt.corners.init.CornerBlocks;
import net.ludocrypt.corners.init.CornerPaintings;
import net.ludocrypt.corners.init.CornerSoundEvents;
import net.ludocrypt.corners.world.biome.CommunalCorridorsBiome;
import net.ludocrypt.corners.world.biome.HoaryCrossroadsBiome;
import net.ludocrypt.corners.world.biome.YearningCanalBiome;
import net.ludocrypt.corners.world.chunk.CommunalCorridorsChunkGenerator;
import net.ludocrypt.corners.world.chunk.HoaryCrossroadsChunkGenerator;
import net.ludocrypt.corners.world.chunk.YearningCanalChunkGenerator;
import net.ludocrypt.corners.world.feature.GaiaTreeFeature;
import net.ludocrypt.limlib.api.LimLibRegistries;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.ludocrypt.limlib.api.effects.post.StaticPostEffect;
import net.ludocrypt.limlib.api.effects.sky.DimensionEffects;
import net.ludocrypt.limlib.api.effects.sky.StaticDimensionEffects;
import net.ludocrypt.limlib.api.effects.sound.SoundEffects;
import net.ludocrypt.limlib.api.effects.sound.reverb.StaticReverbEffect;
import net.ludocrypt.limlib.api.skybox.Skybox;
import net.ludocrypt.limlib.api.skybox.TexturedSkybox;
import net.ludocrypt.limlib.impl.Limlib;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.DimensionType.MonsterSettings;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static net.ludocrypt.corners.init.CornerWorlds.*;

public class CornersDynamicRegistryProvider {

    public static void soundEffects(BootstrapContext<SoundEffects> context) {
        context.register(ResourceKey.create(SoundEffects.SOUND_EFFECTS_KEY, TheCorners.id(YEARNING_CANAL)), new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(20.0F).build()), Optional.empty(), Optional.of(new Music(CornerSoundEvents.MUSIC_YEARNING_CANAL, 3000, 8000, true))));
        context.register(ResourceKey.create(SoundEffects.SOUND_EFFECTS_KEY, TheCorners.id(COMMUNAL_CORRIDORS)), new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(2.15F).setDensity(0.0725F).build()), Optional.empty(), Optional.empty()));
        context.register(ResourceKey.create(SoundEffects.SOUND_EFFECTS_KEY, TheCorners.id(HOARY_CROSSROADS)), new SoundEffects(Optional.of(new StaticReverbEffect.Builder().setDecayTime(15.0F).setDensity(1.0F).build()), Optional.empty(), Optional.of(new Music(CornerSoundEvents.MUSIC_HOARY_CROSSROADS, 3000, 8000, true))));
    }

    public static void skyboxes(BootstrapContext<Skybox> context) {
        context.register(ResourceKey.create(LimLibRegistries.SKYBOX, TheCorners.id(YEARNING_CANAL)), new TexturedSkybox(TheCorners.id("textures/sky/yearning_canal")));
        context.register(ResourceKey.create(LimLibRegistries.SKYBOX, TheCorners.id(COMMUNAL_CORRIDORS)), new TexturedSkybox(TheCorners.id("textures/sky/snow")));
        context.register(ResourceKey.create(LimLibRegistries.SKYBOX, TheCorners.id(HOARY_CROSSROADS)), new TexturedSkybox(TheCorners.id("textures/sky/hoary_crossroads")));
    }

    public static void dimensionEffects(BootstrapContext<DimensionEffects> context) {
        context.register(ResourceKey.create(LimLibRegistries.DIMENSION_EFFECTS, TheCorners.id(YEARNING_CANAL)), new StaticDimensionEffects(Optional.empty(), false, "NONE", true, false, false, 1.0F));
        context.register(ResourceKey.create(LimLibRegistries.DIMENSION_EFFECTS, TheCorners.id(COMMUNAL_CORRIDORS)), new StaticDimensionEffects(Optional.empty(), false, "NONE", true, false, false, 1.0F));
        context.register(ResourceKey.create(LimLibRegistries.DIMENSION_EFFECTS, TheCorners.id(HOARY_CROSSROADS)), new StaticDimensionEffects(Optional.empty(), false, "NONE", true, false, true, 1.0F));
    }

    public static void postEffects(BootstrapContext<PostEffect> context) {
        context.register(ResourceKey.create(LimLibRegistries.POST_EFFECT, TheCorners.id(YEARNING_CANAL)), new StaticPostEffect(TheCorners.id(YEARNING_CANAL)));
        context.register(ResourceKey.create(LimLibRegistries.POST_EFFECT, TheCorners.id(COMMUNAL_CORRIDORS)), new StrongPostEffect(TheCorners.id(COMMUNAL_CORRIDORS), TheCorners.id(COMMUNAL_CORRIDORS + "_fallback")));
        context.register(ResourceKey.create(LimLibRegistries.POST_EFFECT, TheCorners.id(HOARY_CROSSROADS)), new StaticPostEffect(TheCorners.id(HOARY_CROSSROADS)));
    }

//    public static void features(BootstrapContext<Feature<?>> context) {
//        context.register(CornerBiomes.GAIA_TREE_FEATURE, new GaiaTreeFeature(NoneFeatureConfiguration.CODEC));
//    }

    public static void configuredFeature(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        GaiaTreeFeature gaia_tree_feature = (GaiaTreeFeature) BuiltInRegistries.FEATURE.get(CornerBiomes.GAIA_TREE_FEATURE);
        context.register(CornerBiomes.CONFIGURED_GAIA_TREE_FEATURE, new ConfiguredFeature<>(gaia_tree_feature, NoneFeatureConfiguration.INSTANCE));
        context.register(CornerBiomes.CONFIGURED_SAPLING_GAIA_TREE_FEATURE, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(BlockStateProvider.simple(CornerBlocks.GAIA_LOG),
                        new GiantTrunkPlacer(10, 5, 5), BlockStateProvider.simple(CornerBlocks.GAIA_LEAVES),
                        new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(8, 10)),
                        new TwoLayersFeatureSize(1, 1, 2)).build()));
    }

    public static void paintingVariants(BootstrapContext<PaintingVariant> context) {
        context.register(CornerPaintings.OVERWORLD, new PaintingVariant(3, 3, TheCorners.id("overworld")));
        context.register(CornerPaintings.OVERWORLD_THIN, new PaintingVariant(1, 2, TheCorners.id("overworld_thin")));
        context.register(CornerPaintings.OVERWORLD_WIDE, new PaintingVariant(4, 2, TheCorners.id("overworld_wide")));
        context.register(CornerPaintings.YEARNING_CANAL, new PaintingVariant(3, 3, TheCorners.id("yearning_canal")));
        context.register(CornerPaintings.COMMUNAL_CORRIDORS, new PaintingVariant(2, 2, TheCorners.id("communal_corridors")));
        context.register(CornerPaintings.HOARY_CROSSROADS, new PaintingVariant(2, 3, TheCorners.id("hoary_crossroads")));

	}

    public static void dimensionTypes(BootstrapContext<DimensionType> context) {
        context.register(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(YEARNING_CANAL)),
                new DimensionType(OptionalLong.of(1200), true, false, false, true, 1.0, true, false, 0, 1024, 1024,
                        TagKey.create(Registries.BLOCK, TheCorners.id(YEARNING_CANAL)), TheCorners.id(YEARNING_CANAL), 1.0F,
                        new MonsterSettings(false, false, ConstantInt.ZERO, 0)));
        context.register(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(COMMUNAL_CORRIDORS)),
                new DimensionType(OptionalLong.of(23500), true, false, false, true, 1.0, true, false, 0, 256, 256,
                        TagKey.create(Registries.BLOCK, TheCorners.id(COMMUNAL_CORRIDORS)), TheCorners.id(COMMUNAL_CORRIDORS),
                        0.075F, new MonsterSettings(false, false, ConstantInt.ZERO, 0)));

        context.register(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(HOARY_CROSSROADS)),
                new DimensionType(OptionalLong.of(1200), true, false, false, true, 1.0, true, false, 0, 512, 512,
                        TagKey.create(Registries.BLOCK, TheCorners.id(HOARY_CROSSROADS)), TheCorners.id(HOARY_CROSSROADS), 0.725F,
                        new MonsterSettings(false, false, ConstantInt.ZERO, 0)));
    }

    public static void levelStems(BootstrapContext<LevelStem> context) {
        var dimensionTypeGetter = context.lookup(Registries.DIMENSION_TYPE);
        var biomeGetter = context.lookup(Registries.BIOME);

        context.register(ResourceKey.create(Registries.LEVEL_STEM, TheCorners.id(YEARNING_CANAL)), new LevelStem(
                dimensionTypeGetter.getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(YEARNING_CANAL))),
                        new YearningCanalChunkGenerator(new FixedBiomeSource(
                                biomeGetter.getOrThrow(CornerBiomes.YEARNING_CANAL_BIOME)),
                                YearningCanalChunkGenerator.createGroup()))
        );

        context.register(ResourceKey.create(Registries.LEVEL_STEM, TheCorners.id(COMMUNAL_CORRIDORS)), new LevelStem(
                dimensionTypeGetter.getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(COMMUNAL_CORRIDORS))),
                        new CommunalCorridorsChunkGenerator(
                                new FixedBiomeSource(
                                biomeGetter.getOrThrow(CornerBiomes.COMMUNAL_CORRIDORS_BIOME)),
                                CommunalCorridorsChunkGenerator.createGroup(), 16, 16, 8, 0))
        );

        context.register(ResourceKey.create(Registries.LEVEL_STEM, TheCorners.id(HOARY_CROSSROADS)), new LevelStem(
                dimensionTypeGetter.getOrThrow(ResourceKey.create(Registries.DIMENSION_TYPE, TheCorners.id(HOARY_CROSSROADS))),
                        new HoaryCrossroadsChunkGenerator(
                                new FixedBiomeSource(
                                biomeGetter.getOrThrow(CornerBiomes.HOARY_CROSSROADS_BIOME)),
                                HoaryCrossroadsChunkGenerator.createGroup(), 16, 16, 4, 0))
        );
    }

    public static void biomes(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> features = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> carvers = context.lookup(Registries.CONFIGURED_CARVER);

        context.register(CornerBiomes.YEARNING_CANAL_BIOME, YearningCanalBiome.create(features, carvers));
        context.register(CornerBiomes.COMMUNAL_CORRIDORS_BIOME, CommunalCorridorsBiome.create(features, carvers));
        context.register(CornerBiomes.HOARY_CROSSROADS_BIOME, HoaryCrossroadsBiome.create(features, carvers));
    }
}
