package net.ludocrypt.specialmodels.impl.access;

import org.joml.Matrix4f;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;

public interface WorldRendererAccess {

	public void render(MatrixStack matrices, Matrix4f positionMatrix, float tickDelta, Camera camera, boolean outside);

}
