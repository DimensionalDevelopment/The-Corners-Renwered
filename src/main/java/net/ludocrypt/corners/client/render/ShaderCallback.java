package net.ludocrypt.corners.client.render;

import net.minecraft.client.renderer.ShaderInstance;

@FunctionalInterface
public interface ShaderCallback {
    void setup(ShaderInstance shader);
}
