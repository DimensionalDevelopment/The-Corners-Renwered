package net.ludocrypt.specialmodels.impl.render;
public class MutableQuad {

	private MutableVertice v1;
	private MutableVertice v2;
	private MutableVertice v3;
	private MutableVertice v4;

	public MutableQuad(MutableVertice v1, MutableVertice v2, MutableVertice v3, MutableVertice v4) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.v4 = v4;
	}

	public MutableVertice getV1() {
		return v1;
	}

	public MutableVertice getV2() {
		return v2;
	}

	public MutableVertice getV3() {
		return v3;
	}

	public MutableVertice getV4() {
		return v4;
	}

}
