package net.ludocrypt.specialmodels.impl.mixin.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.ludocrypt.specialmodels.impl.access.BakedModelAccess;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;

@Mixin(BakedModel.class)
public interface BakedModelMixin extends BakedModelAccess {

	@Override
	default List<Pair<SpecialModelRenderer, BakedModel>> getModels(@Nullable BlockState state) {
		return Lists.newArrayList();
	}

	@Override
	default void addModel(SpecialModelRenderer modelRenderer, @Nullable BlockState state, BakedModel model) {
	}

}
