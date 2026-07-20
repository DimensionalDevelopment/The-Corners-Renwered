package net.ludocrypt.corners.init;

import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.entity.CornerBoatEntity.CornerBoat;
import net.ludocrypt.corners.entity.DimensionalPaintingEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;

public class CornerEntities {

	public static final EntityType<DimensionalPaintingEntity> DIMENSIONAL_PAINTING_ENTITY = get("dimensional_painting",
		EntityType.Builder.of(DimensionalPaintingEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(10)
			.updateInterval(Integer.MAX_VALUE)
			.build(TheCorners.id("dimensional_painting").toString()));
//	public static final EntityType<CorvusEntity> CORVUS_ENTITY = get("corvus", QuiltEntityTypeBuilder.createMob().entityFactory(CorvusEntity::new).spawnGroup(SpawnGroup.AMBIENT)
//			.setDimensions(new EntityDimensions(0.375F, 0.875F, false)).maxBlockTrackingRange(10).defaultAttributes(CorvusEntity.createAttributes()).build());
	public static final EntityType<Boat> GAIA_BOAT = get("gaia_boat",
			EntityType.Builder.of(CornerBoat.GAIA.factory(false), MobCategory.MISC)
			.sized(1.375f, 0.5625f)
				.clientTrackingRange(10)
			.build(TheCorners.id("gaia_boat").toString()));
	public static final EntityType<Boat> GAIA_CHEST_BOAT = get("gaia_chest_boat",
			EntityType.Builder.of(CornerBoat.GAIA.factory(true), MobCategory.MISC)
			.sized(1.375f, 0.5625f).clientTrackingRange(10)
			.build(TheCorners.id("gaia_chest_boat").toString()));

	public static void init() {
	}

	public static <E extends Entity, T extends EntityType<E>> T get(String id, T entity) {
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, TheCorners.id(id), entity);
	}

}
