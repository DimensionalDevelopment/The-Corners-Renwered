package net.ludocrypt.corners.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.ludocrypt.corners.TheCorners;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.dimdev.limlib.client.specialmodels.ShaderCallback;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static net.ludocrypt.corners.TheCorners.hexToRGBA;

public class ChristmasRenderer implements ShaderCallback {

	private final String id;
	private static final int[] TWINKLE_TEXTURE_UNITS = {1, 3, 4};
	private static final int LIGHT_TEXTURE_UNIT_START = 5;

	public ChristmasRenderer(String id) {
		this.id = id;
	}

	private double gazeTimer = 0;
	private double gazeWaiting = 0;

	@Override
	public void setup(ShaderInstance shader) {
		var conig = TheCorners.getConfig();


		if (conig.christmas.isChristmas()) {

			if (shader.getUniform("christmas") != null) {
				shader.getUniform("christmas").set(1);
			}

			for (int i = 0; i < 6; i++) {
				RenderSystem.setShaderTexture(i + LIGHT_TEXTURE_UNIT_START, TheCorners.id("textures/sky/" + id + "_lights_" + i + ".png"));
				shader.setSampler("Light" + i, RenderSystem.getShaderTexture(i + LIGHT_TEXTURE_UNIT_START));

				if (shader.getUniform("leftTint" + i) != null) {
					shader.getUniform("leftTint" + i).set(new Vector4f(hexToRGBA(conig.christmas.leftColors.get((((int) Math.floor(RenderSystem.getShaderGameTime() * 1000)) + i) % conig.christmas.leftColors.size()))));
				}

				if (shader.getUniform("rightTint" + i) != null) {
					shader.getUniform("rightTint" + i).set(new Vector4f(hexToRGBA(conig.christmas.rightColors.get((((int) Math.floor(RenderSystem.getShaderGameTime() * 1000)) + i) % conig.christmas.rightColors.size()))));
				}

			}

		} else {

			if (shader.getUniform("christmas") != null) {
				shader.getUniform("christmas").set(0);
			}

		}

		RenderSystem.setShaderTexture(0, TheCorners.id("textures/sky/" + id + ".png"));
		shader.setSampler("Sampler0", RenderSystem.getShaderTexture(0));

		for (int i = 0; i < 3; i++) {
			RenderSystem.setShaderTexture(TWINKLE_TEXTURE_UNITS[i], TheCorners.id("textures/sky/" + id + "_twinkles_" + i + ".png"));
			shader.setSampler("Twinkle" + i, RenderSystem.getShaderTexture(TWINKLE_TEXTURE_UNITS[i]));
		}

		if (shader.getUniform("GameTime") != null) {
			shader.getUniform("GameTime").set(RenderSystem.getShaderGameTime());
		}

		Minecraft client = Minecraft.getInstance();
		Camera camera = client.gameRenderer.getMainCamera();
		Matrix4f matrix = new PoseStack().last().pose();
		matrix.rotate(Axis.XP.rotationDegrees(camera.getXRot()));
		matrix.rotate(Axis.YP.rotationDegrees(camera.getYRot() + 180.0F));
		double gazeAngle = Math.max(Math.toRadians(camera.getXRot() % 360) / Math.PI * -2, 0);

		if (gazeAngle > 0.4) {
			gazeWaiting += ((gazeAngle - 0.5) / (0.5)) * (0.004) + 0.001;

			if (gazeWaiting > 0.5) {
				gazeTimer += ((gazeAngle - 0.5) / (0.5)) * (0.002) + 0.001;
			}

		} else {
			gazeTimer -= 0.01D;
			gazeWaiting = 0.0D;
		}

		gazeTimer = Mth.clamp(gazeTimer, 0, 1);

		if (shader.getUniform("gaze") != null) {
			shader.getUniform("gaze").set((float) gazeTimer);
		}

		if (shader.getUniform("RotMat") != null) {
			shader.getUniform("RotMat").set(matrix);
		}

	}
}
