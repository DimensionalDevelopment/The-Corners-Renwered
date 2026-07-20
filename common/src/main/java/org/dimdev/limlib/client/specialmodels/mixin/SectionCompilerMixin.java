package org.dimdev.limlib.client.specialmodels.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.dimdev.limlib.client.specialmodels.SectionCompilerSpecialModelRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        SectionCompilerSpecialModelRenderer.renderSpecialModelParts(
            this.blockRenderer, renderChunkRegion, sectionBufferBuilderPack, poseStack, map, blockPos, blockState);
    }
}
