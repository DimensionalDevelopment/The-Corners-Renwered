package net.ludocrypt.corners.block;

import net.ludocrypt.corners.entity.CornerBoatEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class CornerBoatItem extends Item {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final CornerBoatEntity.CornerBoat boatData;
	private final boolean chest;

	public CornerBoatItem(boolean chest, CornerBoatEntity.CornerBoat boatData, Item.Properties settings) {
		super(settings);
		this.chest = chest;
		this.boatData = boatData;
	}

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        } else {
            Vec3 vec3 = player.getViewVector(1.0F);
            double d = (double)5.0F;
            List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(vec3.scale((double)5.0F)).inflate((double)1.0F), ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3 vec32 = player.getEyePosition();

                for(Entity entity : list) {
                    AABB aABB = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                    if (aABB.contains(vec32)) {
                        return InteractionResultHolder.pass(itemStack);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                Boat boat = this.getBoat(level, hitResult, itemStack, player);
                boat.setYRot(player.getYRot());
                if (!level.noCollision(boat, boat.getBoundingBox())) {
                    return InteractionResultHolder.fail(itemStack);
                } else {
                    if (!level.isClientSide) {
                        level.addFreshEntity(boat);
                        level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                        itemStack.consume(1, player);
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            } else {
                return InteractionResultHolder.pass(itemStack);
            }
        }
    }

    private Boat getBoat(Level level, HitResult hitResult, ItemStack itemStack, Player player) {
        var boat = boatData.factory(chest).create(boatData.entityType(chest), level);
        boat.absMoveTo(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
        if (level instanceof ServerLevel serverLevel) {
            EntityType.createDefaultStackConfig(serverLevel, itemStack, player).accept(boat);
        }

        return boat;
    }


}
