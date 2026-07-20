package net.ludocrypt.corners.mixin;

import net.ludocrypt.corners.entity.DimensionalPaintingEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "interact", at = @At("HEAD"), cancellable = true)
	private void corners$interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> ci) {

		if (!player.level().isClientSide) {

			if (((Entity) (Object) this) instanceof Painting painting) {

				if (!(((Entity) (Object) this) instanceof DimensionalPaintingEntity)) {

					if (painting.getVariant().is(id -> id.location().getNamespace().equals("corners"))) {

						if (player.getItemInHand(hand).getItem().equals(Items.FLINT_AND_STEEL)) {
							DimensionalPaintingEntity dimensional = DimensionalPaintingEntity
								.create(painting.level(), painting.getPos(),
									painting.getDirection(), painting.getVariant());
							painting.level().addFreshEntity(dimensional);
							painting
								.level()
								.playSound(null, painting.blockPosition(), SoundEvents.FLINTANDSTEEL_USE,
									SoundSource.BLOCKS, 1.0F, 1.0F);
							player
								.getItemInHand(hand)
                                    .hurtAndBreak(1, player, hand.equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
							discard();
							ci.setReturnValue(InteractionResult.SUCCESS);
						}

					}

				}

			}

		}

	}

	@Shadow
	public abstract void discard();

}
