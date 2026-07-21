package org.dimdev.corners;

import net.fabricmc.api.ModInitializer;
import org.dimdev.corners.world.feature.GaiaTreeFeature;
import net.ludocrypt.limlib.api.LimLibRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import org.dimdev.corners.client.render.StrongPostEffect;
import org.dimdev.corners.config.CornerConfig;
import org.dimdev.corners.init.CornerBiomes;
import org.dimdev.corners.init.CornerBlocks;
import org.dimdev.corners.init.CornerEntities;
import org.dimdev.corners.init.CornerModelRenderers;
import org.dimdev.corners.init.CornerPaintings;
import org.dimdev.corners.init.CornerRadioRegistry;
import org.dimdev.corners.init.CornerSoundEvents;
import org.dimdev.corners.packet.ClientToServerPackets;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class TheCorners implements ModInitializer {

	public static final Logger LOGGER = LogManager.getLogger("The Corners");

	@Override
	public void onInitialize() {
		AutoConfig.register(CornerConfig.class, GsonConfigSerializer::new);
		CornerBlocks.init();
		CornerBiomes.init();
		CornerEntities.init();
		CornerPaintings.init();
		CornerSoundEvents.init();
		CornerRadioRegistry.init();
		ClientToServerPackets.manageClientToServerPackets();
		Registry.register(PostEffect.REGISTRY, id("strong_shader"), StrongPostEffect.CODEC);
        Registry.register(BuiltInRegistries.FEATURE, CornerBiomes.GAIA_TREE_FEATURE, new GaiaTreeFeature(NoneFeatureConfiguration.CODEC));
	}

	public static ResourceLocation id(String id) {
		return ResourceLocation.fromNamespaceAndPath("corners", id);
	}

}
