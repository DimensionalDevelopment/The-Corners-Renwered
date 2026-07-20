package org.dimdev.limlib.client.specialmodels.compat.iris;


import net.ludocrypt.corners.TheCorners;

public final class IrisCompat {

    public static boolean shouldDisableSpecialModelRenderTypes() {
        if (!TheCorners.getSided().isModLoaded("iris")) {
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
