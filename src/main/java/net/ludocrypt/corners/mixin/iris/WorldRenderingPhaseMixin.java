package net.ludocrypt.corners.mixin.iris;

import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.ludocrypt.corners.compat.iris.IrisCompat;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = WorldRenderingPhase.class, remap = false)
public class WorldRenderingPhaseMixin {

    @Inject(method = "fromTerrainRenderType", at = @At("HEAD"), cancellable = true)
    private static void corners$fromSpecialModelRenderType(RenderType renderType, CallbackInfoReturnable<WorldRenderingPhase> cir) {
        if (!IrisCompat.shouldDisableSpecialModelRenderTypes() && CornerRenderTypes.isKnownSpecialModelRenderType(renderType)) {
            cir.setReturnValue(WorldRenderingPhase.TERRAIN_TRANSLUCENT);
        }
    }
}
