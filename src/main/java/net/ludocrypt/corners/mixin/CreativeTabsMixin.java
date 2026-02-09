package net.ludocrypt.corners.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.function.Predicate;

@Mixin(CreativeModeTabs.class)
public class CreativeTabsMixin {
    @Shadow
    @Final
    private static Comparator<Holder<PaintingVariant>> PAINTING_COMPARATOR;

    @Inject(method = "generatePresetPaintings", at = @At("HEAD"), cancellable = true)
    private static void painting(CreativeModeTab.Output output, HolderLookup.Provider provider, HolderLookup.RegistryLookup<PaintingVariant> registryLookup, Predicate<Holder<PaintingVariant>> predicate, CreativeModeTab.TabVisibility tabVisibility, CallbackInfo ci) {
        System.out.println();

        RegistryOps<Tag> registryOps = provider.createSerializationContext(NbtOps.INSTANCE);

        var list = registryLookup.listElements().filter(predicate).sorted(PAINTING_COMPARATOR).toList();

        list.forEach((reference) -> {
            CustomData customData = ((CustomData)CustomData.EMPTY.update(registryOps, Painting.VARIANT_MAP_CODEC, reference).getOrThrow()).update((compoundTag) -> compoundTag.putString("id", "minecraft:painting"));
            ItemStack itemStack = new ItemStack(Items.PAINTING);
            itemStack.set(DataComponents.ENTITY_DATA, customData);
            output.accept(itemStack, tabVisibility);
        });

        ci.cancel();

    }
}
