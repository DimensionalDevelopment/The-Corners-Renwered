package net.ludocrypt.corners.mixin;

import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.ludocrypt.corners.compat.iris.IrisCompat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 2000)
public abstract class LevelRendererMixin {

    @Shadow
    private void renderSectionLayer(RenderType renderType, double x, double y, double z, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        throw new AssertionError();
    }

    @Inject(method = "renderSectionLayer", at = @At("HEAD"), cancellable = true)
    private void corners$renderSpecialModelLayers(RenderType renderType, double x, double y, double z, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (IrisCompat.shouldDisableSpecialModelRenderTypes()) {
            if (CornerRenderTypes.isKnownSpecialModelRenderType(renderType)) {
                ci.cancel();
            }

            return;
        }

        if (renderType != RenderType.translucent()) {
            return;
        }

        for (RenderType specialModelLayer : CornerRenderTypes.chunkBufferLayers()) {
            this.renderSectionLayer(specialModelLayer, x, y, z, modelViewMatrix, projectionMatrix);
        }
    }
}
