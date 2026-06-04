package net.ludocrypt.corners.mixin;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.function.Predicate;

@Mixin(MultiPartBakedModel.class)
public interface MultiPartBakedModelAccessor {

    @Accessor("selectors")
    List<Pair<Predicate<BlockState>, BakedModel>> corners$getSelectors();
}
