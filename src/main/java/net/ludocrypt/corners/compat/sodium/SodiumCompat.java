package net.ludocrypt.corners.compat.sodium;

import net.fabricmc.loader.api.FabricLoader;
import org.joml.Matrix4f;

public final class SodiumCompat {

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("sodium");
    }

    public static void renderSpecialModelMeshes(double cameraX, double cameraY, double cameraZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        if (isLoaded()) {
            SodiumSpecialModelMeshRegistry.renderAll(cameraX, cameraY, cameraZ, modelViewMatrix, projectionMatrix);
        }
    }

    private SodiumCompat() {
    }
}
