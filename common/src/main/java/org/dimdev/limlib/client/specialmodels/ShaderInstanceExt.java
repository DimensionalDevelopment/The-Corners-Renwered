package org.dimdev.limlib.client.specialmodels;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Consumer;

public interface ShaderInstanceExt {
    void addUniformSetCallback(Consumer<ShaderInstance> callback);
}
