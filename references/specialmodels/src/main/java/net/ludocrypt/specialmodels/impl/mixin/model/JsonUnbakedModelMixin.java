package net.ludocrypt.specialmodels.impl.mixin.model;

import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Maps;

import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.ludocrypt.specialmodels.impl.access.BakedModelAccess;
import net.ludocrypt.specialmodels.impl.access.UnbakedModelAccess;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.resource.Material;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;

@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin implements UnbakedModelAccess {

	@Shadow
	@Final
	private static Logger LOGGER;
	@Unique
	private Map<SpecialModelRenderer, Identifier> subModels = Maps.newHashMap();

	@Inject(method = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;bake(Lnet/minecraft/client/render/model/ModelBaker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;", at = @At("RETURN"), cancellable = true)
	private void specialModels$bake(ModelBaker loader, JsonUnbakedModel parent, Function<Material, Sprite> textureGetter,
			ModelBakeSettings settings, Identifier id, boolean hasDepth, CallbackInfoReturnable<BakedModel> ci) {
		this.getSubModels().forEach((modelRenderer, modelId) -> {

			if (!modelId.equals(id)) {
				UnbakedModel model = loader.getModel(modelId);
				model.resolveParents(loader::getModel);
				BakedModel bakedModel = model.bake(loader, textureGetter, settings, modelId);
				((BakedModelAccess) ci.getReturnValue()).addModel(modelRenderer, null, bakedModel);
			} else {
				LOGGER.warn("Model '{}' caught in chain! Renderer '{}' caught model '{}'", id, modelRenderer, modelId);
			}

		});
	}

	@Override
	public Map<SpecialModelRenderer, Identifier> getSubModels() {
		return subModels;
	}

}
