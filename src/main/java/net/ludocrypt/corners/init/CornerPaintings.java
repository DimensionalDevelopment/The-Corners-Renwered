package net.ludocrypt.corners.init;

import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.entity.DimensionalPaintingEntity;
import net.ludocrypt.corners.util.DimensionalPaintingTeleportLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;

import java.util.HashMap;
import java.util.Map;

public class CornerPaintings {

	public static final TriFunction<ServerLevel, LivingEntity, DimensionalPaintingEntity, DimensionTransition> overworldPaintingTarget = (level, entity,
                                                                                                                                          painting) -> {
        if (entity instanceof ServerPlayer player) {
            BlockPos pos = player.getRespawnPosition();

            if (pos != null) {
				return player.findRespawnPositionAndUseSpawnBlock(false, entity1 -> {

                });
			}

		}

        return new DimensionTransition(level, entity, new DimensionTransition.PostDimensionTransition() {
            @Override
            public void onTransition(Entity entity) {

            }
        });

	};

    public static final Map<ResourceKey<PaintingVariant>, DimensionalPaintingTeleportLogic> LOGICS = new HashMap<>();

	public static final ResourceKey<PaintingVariant> OVERWORLD = get("overworld");

	public static final ResourceKey<PaintingVariant> OVERWORLD_THIN = get("overworld_thin");
	public static final ResourceKey<PaintingVariant> OVERWORLD_WIDE = get("overworld_wide");
	public static final ResourceKey<PaintingVariant> YEARNING_CANAL = get("yearning_canal");
	public static final ResourceKey<PaintingVariant> COMMUNAL_CORRIDORS = get("communal_corridors");
	public static final ResourceKey<PaintingVariant> HOARY_CROSSROADS = get("hoary_crossroads");

	public static void init() {
        LOGICS.put(OVERWORLD, new DimensionalPaintingTeleportLogic(Level.OVERWORLD, overworldPaintingTarget));
        LOGICS.put(OVERWORLD_THIN, new DimensionalPaintingTeleportLogic(Level.OVERWORLD, overworldPaintingTarget));
        LOGICS.put(OVERWORLD_WIDE, new DimensionalPaintingTeleportLogic(Level.OVERWORLD, overworldPaintingTarget));
        LOGICS.put(YEARNING_CANAL, DimensionalPaintingTeleportLogic.create(CornerWorlds.YEARNING_CANAL_KEY, new Vec3(5.5D, 1.0D, 5.5D)));
        LOGICS.put(COMMUNAL_CORRIDORS, DimensionalPaintingTeleportLogic.create(CornerWorlds.COMMUNAL_CORRIDORS_KEY,
                                (player, painting) -> player
                                        .position()
                                        .subtract(new Vec3(player.getX() % 8.0D, player.getY(), player.getZ() % 8.0D))
                                        .add(2.0D, 2.0D, 2.0D)));
        LOGICS.put(HOARY_CROSSROADS, DimensionalPaintingTeleportLogic.create(CornerWorlds.HOARY_CROSSROADS_KEY,
                                (player, painting) -> player
                                        .position()
                                        .subtract(new Vec3(player.getX() % 512.0D, player.getY(), player.getZ() % 512.0D))
                                        .add(256.0D, 263.0D, 0.0D)
                                        .add(4.0D, 0, 4.0D)));
	}

	public static ResourceKey<PaintingVariant> get(String id) {
		return ResourceKey.create(Registries.PAINTING_VARIANT, TheCorners.id(id));
	}

}
