package net.ludocrypt.corners;

import net.fabricmc.api.ModInitializer;
import net.ludocrypt.corners.world.feature.GaiaTreeFeature;
import net.ludocrypt.limlib.api.LimLibRegistries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.ludocrypt.corners.client.render.StrongPostEffect;
import net.ludocrypt.corners.config.CornerConfig;
import net.ludocrypt.corners.init.CornerBiomes;
import net.ludocrypt.corners.init.CornerBlocks;
import net.ludocrypt.corners.init.CornerEntities;
import net.ludocrypt.corners.init.CornerModelRenderers;
import net.ludocrypt.corners.init.CornerPaintings;
import net.ludocrypt.corners.init.CornerRadioRegistry;
import net.ludocrypt.corners.init.CornerSoundEvents;
import net.ludocrypt.corners.packet.ClientToServerPackets;
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
