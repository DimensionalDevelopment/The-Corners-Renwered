package org.dimdev.limlib.client.specialmodels.compat.sodium;

import net.ludocrypt.corners.TheCorners;
import org.joml.Matrix4f;

public final class SodiumCompat {

    public static boolean isLoaded() {
        return TheCorners.getSided().isModLoaded("sodium");
    }

    public static void renderSpecialModelMeshes(double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (isLoaded()) {
            SodiumSpecialModelMeshRegistry.renderAll(cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
        }
    }

    private SodiumCompat() {
    }
}
