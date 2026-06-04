package net.ludocrypt.corners.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.random.WeightedEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WeightedBakedModel.class)
public interface WeightedBakedModelAccessor {

    @Accessor("list")
    List<WeightedEntry.Wrapper<BakedModel>> corners$getList();

    @Accessor("totalWeight")
    int corners$getTotalWeight();
}
