package org.dimdev.corners.client;

import org.dimdev.corners.client.render.CornerBoatEntityRenderer;
import org.dimdev.corners.client.render.StrongPostEffect;
import org.dimdev.corners.entity.CornerBoatEntity.CornerBoat;
import org.dimdev.corners.init.CornerBlocks;
import org.dimdev.corners.init.CornerEntities;
import org.dimdev.corners.init.CornerModelRenderers;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Painting;
import org.dimdev.limlib.api.client.ModClient;
import org.dimdev.limlib.api.client.effect.EffectRenderers;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TheCornersClient implements ModClient<CornersClientSided<?>> {
	private CornersClientSided<?> sided;

	@Override
	public void init(CornersClientSided<?> sided) {
		this.sided = sided;

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
	public String getModId() {
		return "corners";
	}
}
