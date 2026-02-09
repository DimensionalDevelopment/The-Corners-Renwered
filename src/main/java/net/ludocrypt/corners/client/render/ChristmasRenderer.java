package net.ludocrypt.corners.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.config.CornerConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class ChristmasRenderer implements ShaderCallback {

	private final String id;

	public ChristmasRenderer(String id) {
		this.id = id;
	}

	@Environment(EnvType.CLIENT)
	private double gazeTimer = 0;
	@Environment(EnvType.CLIENT)
	private double gazeWaiting = 0;

	@Override
	@Environment(EnvType.CLIENT)
	public void setup(ShaderInstance shader) {

		if (CornerConfig.get().christmas.isChristmas()) {

			if (shader.getUniform("christmas") != null) {
				shader.getUniform("christmas").set(1);
			}

			for (int i = 0; i < 6; i++) {
				RenderSystem.setShaderTexture(i + 4, TheCorners.id("textures/sky/" + id + "_lights_" + i + ".png"));
				shader.setSampler("Light" + i, RenderSystem.getShaderTexture(i + 4));

				if (shader.getUniform("leftTint" + i) != null) {
					shader
						.getUniform("leftTint" + i)
						.set(new Vector4f(hexToRGBA(CornerConfig.get().christmas.leftColors
							.get((((int) Math.floor(RenderSystem.getShaderGameTime() * 1000)) + i) % CornerConfig
								.get().christmas.leftColors.size()))));
				}

				if (shader.getUniform("rightTint" + i) != null) {
					shader
						.getUniform("rightTint" + i)
						.set(new Vector4f(hexToRGBA(CornerConfig.get().christmas.rightColors
							.get((((int) Math.floor(RenderSystem.getShaderGameTime() * 1000)) + i) % CornerConfig
								.get().christmas.rightColors.size()))));
				}

			}

		} else {

			if (shader.getUniform("christmas") != null) {
				shader.getUniform("christmas").set(0);
			}

		}

		RenderSystem.setShaderTexture(0, TheCorners.id("textures/sky/" + id + ".png"));

		for (int i = 0; i < 3; i++) {
			RenderSystem.setShaderTexture(i + 1, TheCorners.id("textures/sky/" + id + "_twinkles_" + i + ".png"));
			shader.setSampler("Twinkle" + i, RenderSystem.getShaderTexture(i + 1));
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

	public static float[] hexToRGBA(String hex) {
		float[] rgba = new float[4];
		hex = hex.replace("#", "");
		hex = hex.replace(" ", "");

		if (hex.length() == 6) {
			rgba[0] = Integer.parseInt(hex.substring(0, 2), 16) / 255f; // Red
			rgba[1] = Integer.parseInt(hex.substring(2, 4), 16) / 255f; // Green
			rgba[2] = Integer.parseInt(hex.substring(4, 6), 16) / 255f; // Blue
			rgba[3] = 1.0f; // Alpha (fully opaque)
		} else if (hex.length() == 8) {
			rgba[0] = Integer.parseInt(hex.substring(0, 2), 16) / 255f; // Red
			rgba[1] = Integer.parseInt(hex.substring(2, 4), 16) / 255f; // Green
			rgba[2] = Integer.parseInt(hex.substring(4, 6), 16) / 255f; // Blue
			rgba[3] = Integer.parseInt(hex.substring(6, 8), 16) / 255f; // Alpha
		} else {
			throw new IllegalArgumentException("Invalid hexadecimal color format.");
		}

		return rgba;
	}
}
