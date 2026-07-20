package org.dimdev.limlib.client.specialmodels.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

public final class SpecialModelsSodiumMixinPlugin implements IMixinConfigPlugin {

    private final boolean sodiumLoaded;

    public SpecialModelsSodiumMixinPlugin() {
        boolean loaded;

        try {
            loaded = MixinService.getService()
                .getBytecodeProvider()
                .getClassNode("net.caffeinemc.mods.sodium.client.SodiumClientMod") != null;
        } catch (Exception exception) {
            loaded = false;
        }

        this.sodiumLoaded = loaded;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return this.sodiumLoaded;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
