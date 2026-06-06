package net.ludocrypt.corners.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.init.CornerBlocks;
import net.ludocrypt.corners.mixin.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DeepBookshelfRenderer implements ShaderCallback {

	public static final ResourceLocation DEEP_BOOKSHELF_BOOKS_TEXTURE = TheCorners.id("textures/block/deep_bookshelf_books.png");
	public static final ResourceLocation DEEP_BOOKSHELF_TOP_TEXTURE = TheCorners.id("textures/block/deep_bookshelf_interior_top.png");
	public static final ResourceLocation DEEP_BOOKSHELF_SIDE_TEXTURE = TheCorners.id("textures/block/deep_bookshelf_interior_side.png");

	@Override
	@Environment(EnvType.CLIENT)
	public void setup(ShaderInstance shader) {
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-3.0F, -3.0F);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.setShaderTexture(1, DEEP_BOOKSHELF_BOOKS_TEXTURE);
		RenderSystem.setShaderTexture(3, DEEP_BOOKSHELF_TOP_TEXTURE);
		RenderSystem.setShaderTexture(4, DEEP_BOOKSHELF_SIDE_TEXTURE);
		shader.setSampler("Sampler0", RenderSystem.getShaderTexture(0));
		shader.setSampler("Sampler1", RenderSystem.getShaderTexture(1));
		shader.setSampler("Sampler3", RenderSystem.getShaderTexture(3));
		shader.setSampler("Sampler4", RenderSystem.getShaderTexture(4));
		Minecraft client = Minecraft.getInstance();
		Camera camera = client.gameRenderer.getMainCamera();
		PoseStack matrixStack = new PoseStack();
        var tickDelta = 0f; //TODO: REplace with actual tickDelta again

		((GameRendererAccessor) client.gameRenderer).callBobHurt(matrixStack, tickDelta);

		if (client.options.bobView().get()) {
			((GameRendererAccessor) client.gameRenderer).callBobView(matrixStack, tickDelta);
		}

		PoseStack basicStack = new PoseStack();
		basicStack
			.mulPose(client.gameRenderer
				.getProjectionMatrix(((GameRendererAccessor) client.gameRenderer).callGetFov(camera, tickDelta, true)));

		if (shader.getUniform("BasicMat") != null) {
			shader.getUniform("BasicMat").set(basicStack.last().pose());
		}

		if (shader.getUniform("BobMat") != null) {
			shader.getUniform("BobMat").set(matrixStack.last().pose());
		}

		if (shader.getUniform("cameraPos") != null) {
			shader
				.getUniform("cameraPos")
				.set((float) camera.getPosition().x(), (float) camera.getPosition().y(), (float) camera.getPosition().z());
		}

	}

	@Override
	@Environment(EnvType.CLIENT)
	public int appendOverlayState(BlockAndTintGetter level, BlockPos pos, BlockState state, BakedModel model,
			long modelSeed) {

		if (!state.is(CornerBlocks.DEEP_BOOKSHELF)) {
			return ShaderCallback.super.appendOverlayState(level, pos, state, model, modelSeed);
		}

		int top = 0;
		int bottom = 0;

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED)) {
			top |= 1;
		}

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED)) {
			top |= 2;
		}

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED)) {
			top |= 4;
		}

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED)) {
			bottom |= 1;
		}

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED)) {
			bottom |= 2;
		}

		if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED)) {
			bottom |= 4;
		}

		return OverlayTexture.pack(top, bottom);
	}

}
