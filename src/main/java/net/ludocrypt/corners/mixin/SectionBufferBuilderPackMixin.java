package net.ludocrypt.corners.mixin;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.ludocrypt.corners.client.render.CornerRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SectionBufferBuilderPack.class)
public class SectionBufferBuilderPackMixin {

    @Shadow
    @Final
    private Map<RenderType, ByteBufferBuilder> buffers;

    @Inject(method = "buffer", at = @At("HEAD"), cancellable = true)
    private void corners$getSpecialModelBuffer(RenderType renderType, CallbackInfoReturnable<ByteBufferBuilder> cir) {
        if (!CornerRenderTypes.isSpecialModelRenderType(renderType)) {
            return;
        }

        ByteBufferBuilder buffer = this.buffers.get(renderType);

        if (buffer == null) {
            buffer = new ByteBufferBuilder(renderType.bufferSize());
            this.buffers.put(renderType, buffer);
        }

        cir.setReturnValue(buffer);
    }
}
