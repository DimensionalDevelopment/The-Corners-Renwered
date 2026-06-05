package net.ludocrypt.specialmodels.api;

import org.joml.Matrix4f;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.ShaderProgram;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registry;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TexturedSpecialModelRenderer extends SpecialModelRenderer {

	public static final SpecialModelRenderer TEXTURED = Registry
		.register(SpecialModelRenderer.SPECIAL_MODEL_RENDERER, new Identifier("specialmodels", "textured"),
			new TexturedSpecialModelRenderer());

	public TexturedSpecialModelRenderer() {
		super();
	}

	public TexturedSpecialModelRenderer(boolean performOutside) {
		super(performOutside);
	}

	@Override
	@ClientOnly
	public void setup(MatrixStack matrices, Matrix4f viewMatrix, Matrix4f positionMatrix, float tickDelta,
			ShaderProgram shader, BlockPos origin) {
		RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
	}

	public static void init() {
	}

}
