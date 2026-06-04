package net.ludocrypt.corners.mixin;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ludocrypt.corners.client.model.SpecialModelSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(BlockModel.Deserializer.class)
public class BlockModelDeserializerMixin {

    @Unique
    private static final String CORNERS_SPECIAL_MODELS_KEY = "specialmodels";

    @Inject(
        method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/renderer/block/model/BlockModel;",
        at = @At("RETURN"))
    private void corners$readSpecialModels(JsonElement jsonElement, Type type, JsonDeserializationContext context, CallbackInfoReturnable<BlockModel> cir) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject specialModels = GsonHelper.getAsJsonObject(jsonObject, CORNERS_SPECIAL_MODELS_KEY, null);

        if (specialModels == null) {
            return;
        }

        Map<ResourceLocation, ResourceLocation> parsed = new LinkedHashMap<>();

        for (Map.Entry<String, JsonElement> entry : specialModels.entrySet()) {
            ResourceLocation rendererId = ResourceLocation.parse(entry.getKey());
            ResourceLocation modelId = ResourceLocation.parse(GsonHelper.convertToString(entry.getValue(), CORNERS_SPECIAL_MODELS_KEY));
            parsed.put(rendererId, modelId);
        }

        BlockModel model = cir.getReturnValue();
        ((SpecialModelSource) model).corners$setSpecialModels(parsed);
    }
}
