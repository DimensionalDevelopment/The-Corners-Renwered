package net.ludocrypt.corners.init;

import net.ludocrypt.corners.TheCorners;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class CornerWorlds {
	public static final String YEARNING_CANAL = "yearning_canal";
	public static final String COMMUNAL_CORRIDORS = "communal_corridors";
	public static final String HOARY_CROSSROADS = "hoary_crossroads";
	public static final ResourceKey<Level> YEARNING_CANAL_KEY = ResourceKey.create(Registries.DIMENSION, TheCorners.id(YEARNING_CANAL));
	public static final ResourceKey<Level> COMMUNAL_CORRIDORS_KEY = ResourceKey.create(Registries.DIMENSION, TheCorners.id(COMMUNAL_CORRIDORS));
	public static final ResourceKey<Level> HOARY_CROSSROADS_KEY = ResourceKey.create(Registries.DIMENSION, TheCorners.id(HOARY_CROSSROADS));
}
