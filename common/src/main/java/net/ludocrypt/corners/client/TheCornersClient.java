package net.ludocrypt.corners.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.ludocrypt.corners.client.render.CornerBoatEntityRenderer;
import net.ludocrypt.corners.client.render.StrongPostEffect;
import net.ludocrypt.corners.entity.CornerBoatEntity.CornerBoat;
import net.ludocrypt.corners.init.CornerBlocks;
import net.ludocrypt.corners.init.CornerEntities;
import net.ludocrypt.corners.init.CornerModelRenderers;
import net.ludocrypt.corners.packet.ServerToClientPackets;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import org.apache.commons.lang3.function.TriConsumer;
import org.dimdev.limlib.api.client.ModClient;
import org.dimdev.limlib.api.client.effect.EffectRenderers;
import org.dimdev.limlib.client.specialmodels.SpecialModelLoadingPlugin;
import org.dimdev.limlib.client.specialmodels.SpecialModelShaderRegistry;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TheCornersClient implements ModClient<CornersClientSided<?>> {
	private CornersClientSided<?> sided;

	@Override
	public void init(CornersClientSided<?> sided) {
		this.sided = sided;

		SpecialModelLoadingPlugin.init(sided);
        CornerModelRenderers.init();
	}

	@Override
	public void delayedInit() {
		sided.register(
			RenderType.cutout(), CornerBlocks.SNOWY_GLASS_PANE, CornerBlocks.SNOWY_GLASS,
			CornerBlocks.SNOWY_GLASS_SLAB, CornerBlocks.GAIA_DOOR, CornerBlocks.GAIA_TRAPDOOR, CornerBlocks.GAIA_SAPLING,
			CornerBlocks.POTTED_GAIA_SAPLING);
		EffectRenderers.register(StrongPostEffect.getType(), effect -> {});
	}

	@Override
	public void initEntityRenderers(EntityRegister register) {
		register.register((EntityType<Painting>) (EntityType<?>) CornerEntities.DIMENSIONAL_PAINTING_ENTITY, PaintingRenderer::new);
		register.register(CornerBoat.GAIA.entityType(false), context -> new CornerBoatEntityRenderer(context, false, CornerBoat.GAIA));
//		EntityRendererRegistryImpl.register(CornerEntities.CORVUS_ENTITY, CorvusEntityRenderer::new);
		register.register(CornerBoat.GAIA.entityType(true), context -> new CornerBoatEntityRenderer(context, true, CornerBoat.GAIA));
	}

	@Override
	public void initModelLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> consumer) {
		consumer.accept(CornerBoatEntityRenderer.getModelLayer(CornerBoat.GAIA, false), BoatModel::createBodyModel);
//		consumer.accept(CorvusEntityModel.LAYER_LOCATION, () -> CorvusEntityModel.createBodyLayer());
		consumer.accept(CornerBoatEntityRenderer.getModelLayer(CornerBoat.GAIA, true), ChestBoatModel::createBodyModel);
	}

	@Override
	public void initShaders(TriConsumer<ResourceLocation, VertexFormat, Consumer<ShaderInstance>> shaderRegister) {
        try {
            SpecialModelShaderRegistry.registerCoreShaders(shaderRegister);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public String getModId() {
		return "corners";
	}
}
