package net.ludocrypt.corners.init;

import net.ludocrypt.corners.client.render.ChristmasRenderer;
import net.ludocrypt.corners.client.render.DeepBookshelfRenderer;
import net.ludocrypt.corners.client.render.ShaderCallback;
import net.ludocrypt.corners.client.render.SkyboxRenderer;

public class CornerModelRenderers {

	public static final ShaderCallback SNOWY_SKYBOX_RENDERER = new ChristmasRenderer("snow");
	public static final ShaderCallback OFFICE_SKYBOX_RENDERER = new SkyboxRenderer("office");
	public static final ShaderCallback SUNBEACH_SKYBOX_RENDERER = new SkyboxRenderer("sunbeach");
	public static final ShaderCallback DEEP_BOOKSHELF = new DeepBookshelfRenderer();

	public static void init() {
	}
}
