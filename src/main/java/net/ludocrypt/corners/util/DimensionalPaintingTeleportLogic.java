package net.ludocrypt.corners.util;

import net.ludocrypt.corners.entity.DimensionalPaintingEntity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public record DimensionalPaintingTeleportLogic(ResourceKey<Level> radioRedirect, BiFunction<LivingEntity, DimensionalPaintingEntity, ResourceKey<Level>> dimension, TriFunction<ServerLevel, LivingEntity, DimensionalPaintingEntity, DimensionTransition> teleportTarget) {

	public DimensionalPaintingTeleportLogic(ResourceKey<Level> dimension, TriFunction<ServerLevel, LivingEntity, DimensionalPaintingEntity, DimensionTransition> teleportTarget) {
        this(dimension, (player, painting) -> dimension, teleportTarget);
	}

	public DimensionalPaintingTeleportLogic(ResourceKey<Level> dimension, DimensionTransition teleport) {
		this(dimension, (player, painting) -> dimension, (level, player, painting) -> teleport);
	}

	public static DimensionalPaintingTeleportLogic create(ResourceKey<Level> dimension, BiFunction<LivingEntity, DimensionalPaintingEntity, Vec3> teleportTarget) {
		return new DimensionalPaintingTeleportLogic(dimension, (player, painting) -> dimension,
			(level, player, painting) -> new DimensionTransition(level, teleportTarget.apply(player, painting), player.getDeltaMovement(), player.getYRot(), player.getXRot(), entity -> {}));
    }

	public static DimensionalPaintingTeleportLogic create(ResourceKey<Level> dimension, Vec3 dest) {
		return new DimensionalPaintingTeleportLogic(dimension, (player, painting) -> dimension,
			(level, player, painting) -> new DimensionTransition(level, dest, player.getDeltaMovement(), player.getYRot(), player.getXRot(), entity -> {}));
	}

}
