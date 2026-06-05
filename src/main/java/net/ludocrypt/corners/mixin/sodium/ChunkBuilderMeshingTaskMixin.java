package net.ludocrypt.corners.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.material.Material;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.ludocrypt.corners.client.TheCornersModelPlugin;
import net.ludocrypt.corners.compat.iris.IrisCompat;
import net.ludocrypt.corners.compat.sodium.SodiumChunkBuildBuffersExt;
import net.ludocrypt.corners.compat.sodium.SodiumSpecialModelRenderPasses;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {

    @Inject(
        method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.AFTER))
    private void corners$renderSpecialModelParts(ChunkBuildContext context, CancellationToken cancellationToken,
                                                 CallbackInfoReturnable<ChunkBuildOutput> cir,
                                                 @Local(index = 5) ChunkBuildBuffers buffers,
                                                 @Local(index = 6) BlockRenderCache cache,
                                                 @Local(index = 7) LevelSlice levelSlice,
                                                 @Local(index = 14) BlockPos.MutableBlockPos blockPos,
                                                 @Local(index = 15) BlockPos.MutableBlockPos localPos,
                                                 @Local(index = 16) TranslucentGeometryCollector collector,
                                                 @Local(index = 21) BlockState blockState) {
        if (IrisCompat.shouldDisableSpecialModelRenderTypes()) {
            return;
        }

        BakedModel blockModel = cache.getBlockModels().getBlockModel(blockState);
        long seed = blockState.getSeed(blockPos);
        List<TheCornersModelPlugin.SpecialModelPart> specialModelParts = TheCornersModelPlugin.getSpecialModelParts(blockModel, blockState, seed);

        if (specialModelParts.isEmpty()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());
        int light = LevelRenderer.getLightColor(levelSlice, blockState, blockPos);

        for (TheCornersModelPlugin.SpecialModelPart specialModelPart : specialModelParts) {
            Material material = SodiumSpecialModelRenderPasses.getMaterial(specialModelPart.renderType());
            TerrainRenderPass pass = material.pass;
            ChunkModelBuilder builder = ((SodiumChunkBuildBuffersExt) buffers).corners$getOrCreateSpecialModelBuilder(pass);
            VertexConsumer consumer = builder.asFallbackVertexConsumer(material, collector);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), consumer, blockState,
                specialModelPart.model(), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
