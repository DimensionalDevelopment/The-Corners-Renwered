package org.dimdev.limlib.client.specialmodels;

import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;

public class SpecialModelShaders {

    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(SpecialModelShaderRegistry::registerCoreShaders);
    }
}
