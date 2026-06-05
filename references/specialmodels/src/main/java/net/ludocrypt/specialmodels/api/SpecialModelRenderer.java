package net.ludocrypt.specialmodels.api;

import org.joml.Matrix4f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.vertex.VertexBuffer;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.ludocrypt.specialmodels.impl.SpecialModels;
import net.ludocrypt.specialmodels.impl.render.MutableQuad;
import net.ludocrypt.specialmodels.impl.render.Vec4b;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.render.chunk.ChunkRenderRegion;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public abstract class SpecialModelRenderer {

	public static final RegistryKey<Registry<SpecialModelRenderer>> SPECIAL_MODEL_RENDERER_KEY = RegistryKey
		.ofRegistry(new Identifier("limlib/special_model_renderer"));

	public static final SimpleRegistry<SpecialModelRenderer> SPECIAL_MODEL_RENDERER = FabricRegistryBuilder
		.createDefaulted(SPECIAL_MODEL_RENDERER_KEY, new Identifier("specialmodels", "textured"))
		.attribute(RegistryAttribute.SYNCED)
		.buildAndRegister();

	public final boolean performOutside;

	public SpecialModelRenderer() {
		this.performOutside = true;
	}

	public SpecialModelRenderer(boolean performOutside) {
		this.performOutside = performOutside;
	}

	@ClientOnly
	public abstract void setup(MatrixStack matrices, Matrix4f viewMatrix, Matrix4f positionMatrix, float tickDelta,
			ShaderProgram shader, BlockPos chunkOrigin);

	@ClientOnly
	public MutableQuad modifyQuad(ChunkRenderRegion chunkRenderRegion, BlockPos pos, BlockState state, BakedModel model,
			BakedQuad quadIn, long modelSeed, MutableQuad quad) {
		return quad;
	}

	@ClientOnly
	public Matrix4f positionMatrix(Matrix4f in) {
		return in;
	}

	@ClientOnly
	public Matrix4f viewMatrix(Matrix4f in) {
		return in;
	}

	@ClientOnly
	public Vec4b appendState(ChunkRenderRegion chunkRenderRegion, BlockPos pos, BlockState state, BakedModel model,
			long modelSeed) {
		return new Vec4b(0, 0, 0, 0);
	}

	@ClientOnly
	public ShaderProgram getShaderProgram(MatrixStack matrices, float tickDelta, Camera camera, Matrix4f positionMatrix,
			SpecialModelRenderer modelRenderer, VertexBuffer vertexBuffer, BlockPos origin) {
		return SpecialModels.LOADED_SHADERS.get(this);
	}

}
