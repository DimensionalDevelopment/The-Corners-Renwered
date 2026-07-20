package net.ludocrypt.corners.client.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ludocrypt.corners.TheCorners;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.dimdev.limlib.api.LimLibRegistryKeys;
import org.dimdev.limlib.post.PostEffect;

public record StrongPostEffect(ResourceLocation shaderName, ResourceLocation fallbackShaderName) implements PostEffect {
    public static final MapCodec<StrongPostEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(ResourceLocation.CODEC.fieldOf("shader_name").stable().forGetter(StrongPostEffect::shaderName), ResourceLocation.CODEC.fieldOf("fallback_shader_name").stable().forGetter(StrongPostEffect::fallbackShaderName)).apply(instance, instance.stable(StrongPostEffect::new)));
    public static PostEffect.PostEffectType<StrongPostEffect> TYPE;

    public static void init() {
        if (TYPE != null) {
            return;
        }

        Registry<?> registry = BuiltInRegistries.REGISTRY.get(LimLibRegistryKeys.POST_EFFECT_TYPE.location());

        if (registry == null) {
            return;
        }

        TYPE = TheCorners.getSided().register(LimLibRegistryKeys.POST_EFFECT_TYPE, "strong_shader", new PostEffectType<>(CODEC));
    }

    public static PostEffect.PostEffectType<StrongPostEffect> getType() {
        init();

        if (TYPE == null) {
            throw new IllegalStateException("Unable to register corners:strong_shader because LimLib post effect types are not initialized");
        }

        return TYPE;
    }

    @Override
    public PostEffectType<? extends PostEffect> type() {
        return getType();
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    @Override
    public ResourceLocation getShaderLocation() {
        return TheCorners.getConfig().disableStrongShaders ? this.getFallbackShaderLocation() : this.getStrongShaderLocation();
    }

    public ResourceLocation getStrongShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(shaderName.getNamespace(), "shaders/post/" + shaderName.getPath() + ".json");
    }

    public ResourceLocation getFallbackShaderLocation() {
        return ResourceLocation.fromNamespaceAndPath(fallbackShaderName.getNamespace(), "shaders/post/" + fallbackShaderName.getPath() + ".json");
    }

}
