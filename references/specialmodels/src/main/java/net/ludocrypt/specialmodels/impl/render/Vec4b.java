package net.ludocrypt.specialmodels.impl.render;
public class Vec4b {

	private final byte x;
	private final byte y;
	private final byte z;
	private final byte w;

	public Vec4b(int x, int y, int z, int w) {
		this((byte) x, (byte) y, (byte) z, (byte) w);
	}

	public Vec4b(byte x, byte y, byte z, byte w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public byte getX() {
		return x;
	}

	public byte getY() {
		return y;
	}

	public byte getZ() {
		return z;
	}

	public byte getW() {
		return w;
	}

}
