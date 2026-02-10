package net.ludocrypt.corners.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.limlib.api.LimLibRegistries;
import net.ludocrypt.limlib.api.effects.sound.SoundEffects;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CornersDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();

        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<DataProvider>) (output, registriesFuture) -> new FabricDynamicRegistryProvider(output, registriesFuture) {
            @Override
            protected void configure(HolderLookup.Provider registries, Entries entries) {
                registries.lookup(SoundEffects.SOUND_EFFECTS_KEY).ifPresent(entries::addAll);
                registries.lookup(LimLibRegistries.SKYBOX).ifPresent(entries::addAll);
                registries.lookup(LimLibRegistries.DIMENSION_EFFECTS).ifPresent(entries::addAll);
                registries.lookup(LimLibRegistries.POST_EFFECT).ifPresent(entries::addAll);
                registries.lookup(Registries.CONFIGURED_FEATURE).ifPresent(entries::addAll);
                registries.lookup(Registries.BIOME).ifPresent(entries::addAll);
                registries.lookup(Registries.PAINTING_VARIANT).ifPresent(entries::addAll);
                registries.lookup(Registries.DIMENSION_TYPE).ifPresent(entries::addAll);
                registries.lookup(Registries.LEVEL_STEM).ifPresent(entries::addAll);
            }

            @Override
            public String getName() {
                return "The Corners Datagen";
            }
        });

        pack.addProvider(new FabricDataGenerator.Pack.RegistryDependentFactory<DataProvider>() {
            @Override
            public DataProvider create(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
                return new FabricCodecDataProvider<>(output, registriesFuture, Registries.LEVEL_STEM, LevelStem.CODEC) {
                    @Override
                    public String getName() {
                        return "Corners World Stem Datagen";
                    }

                    @Override
                    protected void configure(BiConsumer<ResourceLocation, LevelStem> provider, HolderLookup.Provider lookup) {
                        lookup.lookup(Registries.LEVEL_STEM).stream().flatMap(a -> a.listElements()).forEach(new Consumer<Holder.Reference<LevelStem>>() {
                            @Override
                            public void accept(Holder.Reference<LevelStem> levelStemReference) {
                                provider.accept(levelStemReference.key().location(), levelStemReference.value());
                            }
                        });
                    }
                };
            }
        });

        //        pack.addProvider((output, registriesFuture) -> new TypedDataProvider<>(output, registriesFuture, Registries.BIOME, Biome.DIRECT_CODEC));
//        pack.addProvider((output, registriesFuture) -> new TypedDataProvider<>(output, registriesFuture, Registries.CONFIGURED_FEATURE, ConfiguredFeature.DIRECT_CODEC));
        pack.addProvider((FabricDataGenerator.Pack.RegistryDependentFactory<DataProvider>) (output, registriesFuture) -> new FabricAdvancementProvider(output, registriesFuture) {
            @Override
            public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
//                consumer.accept(new Advancement.Builder()
//                        .display(new DisplayInfo(
//                                Items.PAINTING.getDefaultInstance(),
//                                Component.translatable("advancements.the_corners.root.title"),
//                                Component.translatable("advancements.the_corners.root.description"),
//                                Optional.of(TheCorners.id("textures/block/gaia_log.png")),
//                                AdvancementType.TASK,
//                                false, false, false)
//                        )
//                        .addCriterion("impossible", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
//                                .requirements(AdvancementRequirements.allOf(Set.of("impossible")))
//                        .build(TheCorners.id("root")));
            }
        });
    }

    @Override
    public @Nullable String getEffectiveModId() {
        return "corners";
    }

    @Override
    public void buildRegistry(RegistrySetBuilder builder) {
        builder.add(SoundEffects.SOUND_EFFECTS_KEY, CornersDynamicRegistryProvider::soundEffects);
        builder.add(LimLibRegistries.SKYBOX, CornersDynamicRegistryProvider::skyboxes);
        builder.add(LimLibRegistries.DIMENSION_EFFECTS, CornersDynamicRegistryProvider::dimensionEffects);
        builder.add(LimLibRegistries.POST_EFFECT, CornersDynamicRegistryProvider::postEffects);
        builder.add(Registries.CONFIGURED_FEATURE, CornersDynamicRegistryProvider::configuredFeature);
        builder.add(Registries.BIOME, CornersDynamicRegistryProvider::biomes);
        builder.add(Registries.PAINTING_VARIANT, CornersDynamicRegistryProvider::paintingVariants);
        builder.add(Registries.DIMENSION_TYPE, CornersDynamicRegistryProvider::dimensionTypes);
        builder.add(Registries.LEVEL_STEM, CornersDynamicRegistryProvider::levelStems);
    }
}
