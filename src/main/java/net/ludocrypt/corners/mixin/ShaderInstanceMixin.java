package net.ludocrypt.corners.mixin;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.ludocrypt.corners.client.ShaderInstanceExt;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ShaderInstance.class)
public abstract class ShaderInstanceMixin implements ShaderInstanceExt {
    @Unique
    private Consumer<ShaderInstance> callback = instance -> {};

    @Override
    public void addUniformSetCallback(Consumer<ShaderInstance> callback) {
        if(callback != null) this.callback = callback;
        else this.callback = instance -> {};
    }

    @Inject(method = "setDefaultUniforms", at = @At("TAIL"))
    public void uniformCallback(VertexFormat.Mode mode, Matrix4f matrix4f, Matrix4f matrix4f2, Window window, CallbackInfo ci) {
        callback.accept((ShaderInstance) (Object) this);
    }
}
