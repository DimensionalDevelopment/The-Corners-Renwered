package org.dimdev.corners;

import org.dimdev.corners.world.chunk.CommunalCorridorsChunkGenerator;
import org.dimdev.corners.world.chunk.HoaryCrossroadsChunkGenerator;
import org.dimdev.corners.world.chunk.YearningCanalChunkGenerator;
import org.dimdev.corners.world.feature.GaiaTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.dimdev.corners.client.render.StrongPostEffect;
import org.dimdev.corners.config.CornerConfig;
import org.dimdev.corners.init.CornerBiomes;
import org.dimdev.corners.init.CornerBlocks;
import org.dimdev.corners.init.CornerEntities;
import org.dimdev.corners.init.CornerPaintings;
import org.dimdev.corners.init.CornerRadioRegistry;
import org.dimdev.corners.init.CornerSoundEvents;
import org.dimdev.corners.packet.ClientToServerPackets;
import net.minecraft.resources.ResourceLocation;
import org.dimdev.limlib.api.LimLibRegistryKeys;
import org.dimdev.limlib.api.ModCommon;

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

	@Override
	public String getModId() {
		return "corners";
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
					hexToRGBA(color);
				} catch (IllegalArgumentException e) {
					config.christmas.leftColors.remove(color);
				}

			}

			for (String color : config.christmas.rightColors) {

				try {
					hexToRGBA(color);
				} catch (IllegalArgumentException e) {
					config.christmas.rightColors.remove(color);
				}

			}

			config.christmas.leftColors = trimList(config.christmas.leftColors);
			config.christmas.rightColors = trimList(config.christmas.rightColors);
		}
		return config;
	}

	public static float[] hexToRGBA(String hex) {
		float[] rgba = new float[4];
		hex = hex.replace("#", "");
		hex = hex.replace(" ", "");

		if (hex.length() == 6) {
			rgba[0] = Integer.parseInt(hex.substring(0, 2), 16) / 255f; // Red
			rgba[1] = Integer.parseInt(hex.substring(2, 4), 16) / 255f; // Green
			rgba[2] = Integer.parseInt(hex.substring(4, 6), 16) / 255f; // Blue
			rgba[3] = 1.0f; // Alpha (fully opaque)
		} else if (hex.length() == 8) {
			rgba[0] = Integer.parseInt(hex.substring(0, 2), 16) / 255f; // Red
			rgba[1] = Integer.parseInt(hex.substring(2, 4), 16) / 255f; // Green
			rgba[2] = Integer.parseInt(hex.substring(4, 6), 16) / 255f; // Blue
			rgba[3] = Integer.parseInt(hex.substring(6, 8), 16) / 255f; // Alpha
		} else {
			throw new IllegalArgumentException("Invalid hexadecimal color format.");
		}

		return rgba;
	}
}
