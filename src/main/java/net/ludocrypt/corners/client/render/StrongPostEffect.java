package net.ludocrypt.corners.client.render;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ludocrypt.corners.config.CornerConfig;
import net.ludocrypt.limlib.api.effects.post.PostEffect;
import net.minecraft.resources.ResourceLocation;

public class StrongPostEffect extends PostEffect {

	public static final MapCodec<StrongPostEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
		return instance.group(ResourceLocation.CODEC.fieldOf("shader_name").stable().forGetter((postEffect) -> {
			return postEffect.shaderName;
		}), ResourceLocation.CODEC.fieldOf("fallback_shader_name").stable().forGetter((postEffect) -> {
			return postEffect.fallbackShaderName;
		})).apply(instance, instance.stable(StrongPostEffect::new));
	});
	private final ResourceLocation shaderName;
	private final ResourceLocation fallbackShaderName;

	public StrongPostEffect(ResourceLocation shaderName, ResourceLocation fallbackShaderName) {
		this.shaderName = shaderName;
		this.fallbackShaderName = fallbackShaderName;
	}

	@Override
	public MapCodec<? extends PostEffect> getCodec() {
		return CODEC;
	}

	@Override
	public boolean shouldRender() {
		return true;
	}

	@Override
	public void beforeRender() {
	}

	@Override
	public ResourceLocation getShaderLocation() {
		return CornerConfig.get().disableStrongShaders ? this.getFallbackShaderLocation() : this.getStrongShaderLocation();
	}

	public ResourceLocation getStrongShaderLocation() {
		return ResourceLocation.fromNamespaceAndPath(shaderName.getNamespace(), "shaders/post/" + shaderName.getPath() + ".json");
	}

	public ResourceLocation getFallbackShaderLocation() {
		return ResourceLocation.fromNamespaceAndPath(fallbackShaderName.getNamespace(), "shaders/post/" + fallbackShaderName.getPath() + ".json");
	}

}
