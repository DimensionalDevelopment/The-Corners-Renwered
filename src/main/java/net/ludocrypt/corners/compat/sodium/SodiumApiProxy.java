package net.ludocrypt.corners.compat.sodium;

import net.minecraft.client.renderer.RenderType;

final class SodiumApiProxy {

    static void registerSpecialModelRenderType(RenderType renderType) {
        SodiumSpecialModelRenderPasses.register(renderType);
    }

    private SodiumApiProxy() {
    }
}
