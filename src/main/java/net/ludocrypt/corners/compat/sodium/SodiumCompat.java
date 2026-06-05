package net.ludocrypt.corners.compat.sodium;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderType;

public final class SodiumCompat {

    public static void registerSpecialModelRenderType(RenderType renderType) {
        if (!FabricLoader.getInstance().isModLoaded("sodium")) {
            return;
        }

        try {
            SodiumApiProxy.registerSpecialModelRenderType(renderType);
        } catch (LinkageError | RuntimeException ignored) {
        }
    }

    private SodiumCompat() {
    }
}
