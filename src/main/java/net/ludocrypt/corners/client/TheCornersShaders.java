package net.ludocrypt.corners.client;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

public class TheCornersShaders {
    public static ShaderInstance christmas = null;

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(new CoreShaderRegistrationCallback() {
            @Override
            public void registerShaders(RegistrationContext context) throws IOException {
//                context.register(TheCorners.id("christmas"), DefaultVertexFormat.NEW_ENTITY, shaderInstance -> {
//                    ((ShaderInstanceExt) shaderInstance).addUniformSetCallback(CornerModelRenderers.SNOWY_SKYBOX_RENDERER::setup);
//                    christmas = shaderInstance;
//                });

            }
        });
    }
}
