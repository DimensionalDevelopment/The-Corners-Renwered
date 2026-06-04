package net.ludocrypt.corners.client;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.ludocrypt.corners.client.render.SpecialModelShaderRegistry;

public class TheCornersShaders {

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(SpecialModelShaderRegistry::registerCoreShaders);
    }
}
