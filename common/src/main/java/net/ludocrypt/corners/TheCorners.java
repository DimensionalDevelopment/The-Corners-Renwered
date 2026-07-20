package net.ludocrypt.corners;

import net.ludocrypt.corners.client.render.ChristmasRenderer;
import net.ludocrypt.corners.world.chunk.CommunalCorridorsChunkGenerator;
import net.ludocrypt.corners.world.chunk.HoaryCrossroadsChunkGenerator;
import net.ludocrypt.corners.world.chunk.YearningCanalChunkGenerator;
import net.ludocrypt.corners.world.feature.GaiaTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.ludocrypt.corners.client.render.StrongPostEffect;
import net.ludocrypt.corners.config.CornerConfig;
import net.ludocrypt.corners.init.CornerBiomes;
import net.ludocrypt.corners.init.CornerBlocks;
import net.ludocrypt.corners.init.CornerEntities;
import net.ludocrypt.corners.init.CornerPaintings;
import net.ludocrypt.corners.init.CornerRadioRegistry;
import net.ludocrypt.corners.init.CornerSoundEvents;
import net.ludocrypt.corners.packet.ClientToServerPackets;
import net.minecraft.resources.ResourceLocation;
import org.dimdev.limlib.api.ISided;
import org.dimdev.limlib.api.LimLibRegistryKeys;
import org.dimdev.limlib.api.ModCommon;
import org.dimdev.limlib.impl.SidedImpl;

import java.util.ArrayList;
import java.util.List;

public class TheCorners implements ModCommon<TheCornersSided<?>> {
	private static TheCornersSided<?> sided;
	public static final Logger LOGGER = LogManager.getLogger("The Corners");
	private static CornerConfig config;

	@Override
	public void init(TheCornersSided<?> sided) {
		TheCorners.sided = sided;

		sided.registerRunnable(Registries.BLOCK, () -> CornerBlocks.init(sided));

		sided.registerRunnable(Registries.CHUNK_GENERATOR, new Runnable() {
			@Override
			public void run() {
				sided.registerChunkGenerator("yearning_canal_chunk_generator", YearningCanalChunkGenerator.CODEC);
				sided.registerChunkGenerator("communal_corridors_chunk_generator", CommunalCorridorsChunkGenerator.CODEC);
				sided.registerChunkGenerator("hoary_crossroads_chunk_generator", HoaryCrossroadsChunkGenerator.CODEC);
			}
		});

		sided.registerRunnable(Registries.ENTITY_TYPE, () -> CornerEntities.init());
		CornerPaintings.init();
		sided.registerRunnable(Registries.SOUND_EVENT, CornerSoundEvents::init);
		sided.registerRunnable(CornerRadioRegistry.RADIO_REGISTRY_KEY, () -> CornerRadioRegistry.init());
		ClientToServerPackets.manageClientToServerPackets();
		sided.registerRunnable(LimLibRegistryKeys.POST_EFFECT_TYPE, StrongPostEffect::init);
		sided.registerRunnable(Registries.FEATURE, () -> sided.register(
			Registries.FEATURE,
			CornerBiomes.GAIA_TREE_FEATURE.location(),
			new GaiaTreeFeature(NoneFeatureConfiguration.CODEC)));
	}

	public static ResourceLocation id(String id) {
		return ResourceLocation.fromNamespaceAndPath("corners", id);
	}

	public static TheCornersSided<?> getSided() {
		return sided;
	}

	private static List<String> trimList(List<String> inputList) {
		List<String> trimmedList = new ArrayList<>();
		int limit = Math.min(6, inputList.size());

		for (int i = 0; i < limit; i++) {
			trimmedList.add(inputList.get(i));
		}

		return trimmedList;
	}

	public static CornerConfig getConfig() {
		if(config == null) {

			config = sided.loadConfig(CornerConfig.class);

			for (String color : config.christmas.leftColors) {

				try {
					ChristmasRenderer.hexToRGBA(color);
				} catch (IllegalArgumentException e) {
					config.christmas.leftColors.remove(color);
				}

			}

			for (String color : config.christmas.rightColors) {

				try {
					ChristmasRenderer.hexToRGBA(color);
				} catch (IllegalArgumentException e) {
					config.christmas.rightColors.remove(color);
				}

			}

			config.christmas.leftColors = trimList(config.christmas.leftColors);
			config.christmas.rightColors = trimList(config.christmas.rightColors);
		}
		return config;
	}
}
