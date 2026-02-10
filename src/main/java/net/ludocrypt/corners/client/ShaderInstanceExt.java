package net.ludocrypt.corners.client;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Consumer;

public interface ShaderInstanceExt {
    void addUniformSetCallback(Consumer<ShaderInstance> callback);
}
