package org.dimdev.corners.init;

import org.dimdev.corners.TheCorners;
import org.dimdev.corners.util.RadioSoundTable;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class CornerRadioRegistry {

	public static final ResourceKey<Registry<RadioSoundTable>> RADIO_REGISTRY_KEY = ResourceKey
		.createRegistryKey(TheCorners.id("radio_registry"));
	public static final Registry<RadioSoundTable> RADIO_REGISTRY = TheCorners.getSided().createRegistry(RADIO_REGISTRY_KEY, TheCorners.id("default_radio"), true);
	public static final RadioSoundTable DEFAULT = new RadioSoundTable(CornerSoundEvents.RADIO_DEFAULT_STATIC, CornerSoundEvents.RADIO_DEFAULT_STATIC, CornerSoundEvents.RADIO_DEFAULT_STATIC);

	public static void init() {

		Registry.register(RADIO_REGISTRY, TheCorners.id("default_radio"), DEFAULT);
		register("yearning_canal", new RadioSoundTable(CornerSoundEvents.RADIO_YEARNING_CANAL_MUSIC,
			CornerSoundEvents.RADIO_YEARNING_CANAL_STATIC, CornerSoundEvents.RADIO_YEARNING_CANAL));
		register("communal_corridors", new RadioSoundTable(CornerSoundEvents.RADIO_COMMUNAL_CORRIDORS_MUSIC,
			CornerSoundEvents.RADIO_COMMUNAL_CORRIDORS_STATIC, CornerSoundEvents.RADIO_COMMUNAL_CORRIDORS));
		register("hoary_crossroads", new RadioSoundTable(CornerSoundEvents.RADIO_HOARY_CROSSROADS_MUSIC,
			CornerSoundEvents.RADIO_HOARY_CROSSROADS_STATIC, CornerSoundEvents.RADIO_HOARY_CROSSROADS));
	}

	public static RadioSoundTable getCurrent(Minecraft client) {
		return getCurrent(client.level.dimension());
	}

	public static RadioSoundTable getCurrent(ResourceKey<Level> key) {
		return RADIO_REGISTRY.getOptional(key.location()).orElse(DEFAULT);
	}

	public static <T extends RadioSoundTable> T register(String id, T radio) {
		return TheCorners.getSided().register(CornerRadioRegistry.RADIO_REGISTRY_KEY, TheCorners.id(id), radio);
	}

}
