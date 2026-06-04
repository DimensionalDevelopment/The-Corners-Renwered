package net.ludocrypt.corners.mixin;

import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Inject(method = "chunkBufferLayers", at = @At("RETURN"), cancellable = true)
    private static void corners$addSpecialModelLayers(CallbackInfoReturnable<List<RenderType>> cir) {
        List<RenderType> layers = cir.getReturnValue();
        List<RenderType> cornersLayers = CornerRenderTypes.chunkBufferLayers();

        if (layers.containsAll(cornersLayers)) {
            return;
        }

        List<RenderType> modifiedLayers = new ArrayList<>(layers.size() + cornersLayers.size());
        modifiedLayers.addAll(layers);
        cornersLayers.stream().filter(layer -> !modifiedLayers.contains(layer)).forEach(modifiedLayers::add);
        cir.setReturnValue(List.copyOf(modifiedLayers));
    }
}
