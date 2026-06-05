package net.ludocrypt.corners.compat.iris;

import net.fabricmc.loader.api.FabricLoader;

public final class IrisCompat {

    public static boolean shouldDisableSpecialModelRenderTypes() {
        if (!FabricLoader.getInstance().isModLoaded("iris")) {
            return false;
        }

        try {
            return IrisApiProxy.shouldDisableSpecialModelRenderTypes();
        } catch (LinkageError | RuntimeException exception) {
            return true;
        }
    }

    private IrisCompat() {
    }
}
