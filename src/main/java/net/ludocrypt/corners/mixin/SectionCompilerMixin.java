package net.ludocrypt.corners.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.ludocrypt.corners.client.TheCornersModelPlugin;
import net.ludocrypt.corners.client.render.SpecialModelShaderRegistry;
import net.ludocrypt.corners.compat.iris.IrisCompat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = SectionCompiler.class, priority = 1100)
public abstract class SectionCompilerMixin {

    @Shadow
    @Final
    private BlockRenderDispatcher blockRenderer;

    @Inject(
        method = "compile",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderBatched(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;)V",
            shift = At.Shift.AFTER))
    private void corners$renderSpecialModelParts(
        SectionPos sectionPos,
        RenderChunkRegion renderChunkRegion,
        VertexSorting vertexSorting,
        SectionBufferBuilderPack sectionBufferBuilderPack,
        CallbackInfoReturnable<SectionCompiler.Results> cir,
        @Local(index = 9) PoseStack poseStack,
        @Local(index = 10) Map<RenderType, BufferBuilder> map,
        @Local(index = 13) BlockPos blockPos,
        @Local(index = 14) BlockState blockState) {

        if (IrisCompat.shouldDisableSpecialModelRenderTypes()) {
            return;
        }

        long seed = blockState.getSeed(blockPos);
        BakedModel blockModel = this.blockRenderer.getBlockModel(blockState);
        List<TheCornersModelPlugin.SpecialModelPart> specialModelParts = TheCornersModelPlugin.getSpecialModelParts(blockModel, blockState, seed);

        if (specialModelParts.isEmpty()) {
            return;
        }

        int light = LevelRenderer.getLightColor(renderChunkRegion, blockState, blockPos);

        for (TheCornersModelPlugin.SpecialModelPart specialModelPart : specialModelParts) {
            int overlay = SpecialModelShaderRegistry
                .appendOverlayState(specialModelPart.rendererId(), renderChunkRegion, blockPos, blockState, specialModelPart.model(), seed);
            BufferBuilder specialBuffer = this.corners$getOrBeginSpecialLayer(map, sectionBufferBuilderPack, specialModelPart.renderType());
            this.blockRenderer.getModelRenderer().renderModel(poseStack.last(), specialBuffer, blockState, specialModelPart.model(), 1.0F, 1.0F, 1.0F, light, overlay);
        }
    }

    @Unique
    private BufferBuilder corners$getOrBeginSpecialLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack,
                                                         RenderType renderType) {
        BufferBuilder bufferBuilder = map.get(renderType);

        if (bufferBuilder == null) {
            ByteBufferBuilder byteBuffer = sectionBufferBuilderPack.buffer(renderType);
            bufferBuilder = new BufferBuilder(byteBuffer, renderType.mode(), renderType.format());
            map.put(renderType, bufferBuilder);
        }

        return bufferBuilder;
    }
}
