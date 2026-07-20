package org.dimdev.limlib.client.specialmodels;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.dimdev.limlib.client.specialmodels.SpecialModelLoadingPlugin.SpecialModelPart;
import org.dimdev.limlib.client.specialmodels.compat.iris.IrisCompat;

import java.util.List;
import java.util.Map;

public final class SectionCompilerSpecialModelRenderer {

    public static void renderSpecialModelParts(
        BlockRenderDispatcher blockRenderer,
        RenderChunkRegion renderChunkRegion,
        SectionBufferBuilderPack sectionBufferBuilderPack,
        PoseStack poseStack,
        Map<RenderType, BufferBuilder> map,
        BlockPos blockPos,
        BlockState blockState) {
        if (IrisCompat.shouldDisableSpecialModelRenderTypes()) {
            return;
        }

        long seed = blockState.getSeed(blockPos);
        BakedModel blockModel = blockRenderer.getBlockModel(blockState);
        List<SpecialModelPart> specialModelParts = SpecialModelLoadingPlugin.getSpecialModelParts(blockModel, blockState, seed);

        if (specialModelParts.isEmpty()) {
            return;
        }

        int light = LevelRenderer.getLightColor(renderChunkRegion, blockState, blockPos);

        for (SpecialModelPart specialModelPart : specialModelParts) {
            int overlay = SpecialModelShaderRegistry
                .appendOverlayState(specialModelPart.rendererId(), renderChunkRegion, blockPos, blockState, specialModelPart.model(), seed);
            BufferBuilder specialBuffer = getOrBeginSpecialLayer(map, sectionBufferBuilderPack, specialModelPart.renderType());
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), specialBuffer, blockState, specialModelPart.model(), 1.0F, 1.0F, 1.0F, light, overlay);
        }
    }

    private static BufferBuilder getOrBeginSpecialLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack,
                                                        RenderType renderType) {
        BufferBuilder bufferBuilder = map.get(renderType);

        if (bufferBuilder == null) {
            ByteBufferBuilder byteBuffer = sectionBufferBuilderPack.buffer(renderType);
            bufferBuilder = new BufferBuilder(byteBuffer, renderType.mode(), renderType.format());
            map.put(renderType, bufferBuilder);
        }

        return bufferBuilder;
    }

    private SectionCompilerSpecialModelRenderer() {
    }
}
