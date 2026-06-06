package net.ludocrypt.corners.init;

import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.client.render.ChristmasRenderer;
import net.ludocrypt.corners.client.render.DeepBookshelfRenderer;
import net.ludocrypt.corners.client.render.ShaderCallback;
import net.ludocrypt.corners.client.render.SkyboxRenderer;
import net.ludocrypt.corners.client.render.SpecialModelShaderRegistry;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;

public class CornerModelRenderers {

	public static final ResourceLocation SNOWY_SKYBOX = TheCorners.id("snowy_skybox");
	public static final ResourceLocation DEEP_BOOKSHELF = TheCorners.id("deep_bookshelf");

	public static final ShaderCallback SNOWY_SKYBOX_RENDERER = new ChristmasRenderer("snow");
	public static final ShaderCallback OFFICE_SKYBOX_RENDERER = new SkyboxRenderer("office");
	public static final ShaderCallback SUNBEACH_SKYBOX_RENDERER = new SkyboxRenderer("sunbeach");
	public static final ShaderCallback DEEP_BOOKSHELF_RENDERER = new DeepBookshelfRenderer();

	public static void init() {
		SpecialModelShaderRegistry.register(SNOWY_SKYBOX, ResourceLocation.withDefaultNamespace("rendertype_corners_christmas"),
			SNOWY_SKYBOX_RENDERER);
		SpecialModelShaderRegistry
			.register(DEEP_BOOKSHELF, ResourceLocation.withDefaultNamespace("rendertype_corners_deep_bookshelf"),
				DefaultVertexFormat.NEW_ENTITY, DEEP_BOOKSHELF_RENDERER);
	}
}
