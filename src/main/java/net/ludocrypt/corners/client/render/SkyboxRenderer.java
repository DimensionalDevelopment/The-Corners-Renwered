package net.ludocrypt.corners.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.mixin.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

public record SkyboxRenderer(String id) implements ShaderCallback {

    @Override
    @Environment(EnvType.CLIENT)
    public void setup(ShaderInstance shader) {

        for (int i = 0; i < 6; i++) {
            RenderSystem.setShaderTexture(i, TheCorners.id("textures/sky/" + id + "_" + i + ".png"));
        }

        Minecraft client = Minecraft.getInstance();
        Camera camera = client.gameRenderer.getMainCamera();
        Matrix4f matrix = new PoseStack().last().pose();
        matrix.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
        matrix.rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));

        if (shader.getUniform("RotMat") != null) {
            shader.getUniform("RotMat").set(matrix);
        }

        var tickDelta = 0f; //TODO: REplace with actual tickDelta again


        PoseStack matrixStack = new PoseStack();
        ((GameRendererAccessor) client.gameRenderer).callBobHurt(matrixStack, tickDelta);

        if (client.options.bobView().get()) {
            ((GameRendererAccessor) client.gameRenderer).callBobView(matrixStack, tickDelta);
        }

        if (shader.getUniform("bobMat") != null) {
            shader.getUniform("bobMat").set(matrixStack.last().pose());
        }

    }

//	@Override
//	@Environment(EnvType.CLIENT)
//	public MutableQuad modifyQuad(RenderChunkRegion chunkRenderRegion, BlockPos pos, BlockState state, BakedModel model,
//			BakedQuad quadIn, long modelSeed, MutableQuad quad) {
//		quad.getV1().setUv(new Vec2(0.0F, 0.0F));
//		quad.getV2().setUv(new Vec2(0.0F, 1.0F));
//		quad.getV3().setUv(new Vec2(1.0F, 1.0F));
//		quad.getV4().setUv(new Vec2(1.0F, 0.0F));
//		return quad;
//	}

}
