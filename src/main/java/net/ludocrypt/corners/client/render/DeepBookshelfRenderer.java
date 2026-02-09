package net.ludocrypt.corners.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.mixin.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class DeepBookshelfRenderer implements ShaderCallback {

	public static final ResourceLocation DEEP_BOOKSHELF_ATLAS_TEXTURE = TheCorners.id("textures/atlas/deep.png");

	@Override
	@Environment(EnvType.CLIENT)
	public void setup(ShaderInstance shader) {
		RenderSystem.enablePolygonOffset();
		RenderSystem.polygonOffset(-3.0F, -3.0F);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.setShaderTexture(1, DEEP_BOOKSHELF_ATLAS_TEXTURE);
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
//
//	@Override
//	@Environment(EnvType.CLIENT)
//	public Vec4b appendState(RenderChunkRegion chunkRenderRegion, BlockPos pos, BlockState state, BakedModel model,
//			long modelSeed) {
//
//		if (state.is(CornerBlocks.DEEP_BOOKSHELF)) {
//			byte b1 = 0;
//			byte b2 = 0;
//			byte b3 = 0;
//			byte b4 = 0;
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED)) {
//				b1 += 1;
//			}
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED)) {
//				b1 += 2;
//			}
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED)) {
//				b1 += 4;
//			}
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED)) {
//				b2 += 1;
//			}
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED)) {
//				b2 += 2;
//			}
//
//			if (state.getValue(BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED)) {
//				b2 += 4;
//			}
//
//			return new Vec4b(b1, b2, b3, b4);
//		}
//
//		return super.appendState(chunkRenderRegion, pos, state, model, modelSeed);
//	}

}
