package net.ludocrypt.specialmodels.impl.render;

import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class MutableVertice {

	private Vec3d pos;
	private Vec2f uv;

	public MutableVertice() {
		this.pos = Vec3d.ZERO;
		this.uv = Vec2f.ZERO;
	}

	public MutableVertice(Vec3d pos, Vec2f uv) {
		this.pos = pos;
		this.uv = uv;
	}

	public MutableVertice(double x, double y, double z, double u, double v) {
		this.pos = new Vec3d(x, y, z);
		this.uv = new Vec2f((float) u, (float) v);
	}

	public Vec3d getPos() {
		return pos;
	}

	public Vec2f getUv() {
		return uv;
	}

	public void setPos(Vec3d pos) {
		this.pos = pos;
	}

	public void setUv(Vec2f uv) {
		this.uv = uv;
	}

	public void add(double x, double y, double z) {
		this.pos = this.pos.add(x, y, z);
	}

	public void shift(double u, double v) {
		this.uv = new Vec2f(this.uv.x + (float) u, this.uv.y + (float) v);
	}

}
