package net.ludocrypt.corners.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.ChunkBuildOutput;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask;
import net.caffeinemc.mods.sodium.client.util.task.CancellationToken;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.ludocrypt.corners.client.TheCornersModelPlugin;
import net.ludocrypt.corners.compat.iris.IrisCompat;
import net.ludocrypt.corners.compat.sodium.SodiumSpecialModelMeshRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mixin(value = ChunkBuilderMeshingTask.class, remap = false)
public class ChunkBuilderMeshingTaskMixin {

    @Unique
    private Map<RenderType, SodiumSpecialModelMeshRegistry.PendingMeshBuilder> corners$specialModelBuffers;

    @Inject(
        method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At("HEAD"))
    private void corners$beginSpecialModelMesh(ChunkBuildContext context, CancellationToken cancellationToken,
                                               CallbackInfoReturnable<ChunkBuildOutput> cir) {
        this.corners$specialModelBuffers = new LinkedHashMap<>();
    }

    @Inject(
        method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer;renderModel(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)V",
            shift = At.Shift.AFTER))
    private void corners$renderSpecialModelParts(ChunkBuildContext context, CancellationToken cancellationToken,
                                                 CallbackInfoReturnable<ChunkBuildOutput> cir,
                                                 @Local(index = 6) BlockRenderCache cache,
                                                 @Local(index = 7) LevelSlice levelSlice,
                                                 @Local(index = 14) BlockPos.MutableBlockPos blockPos,
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
        poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        int light = LevelRenderer.getLightColor(levelSlice, blockState, blockPos);

        for (TheCornersModelPlugin.SpecialModelPart specialModelPart : specialModelParts) {
            BufferBuilder specialBuffer = this.corners$getOrCreateSpecialModelBuffer(specialModelPart.renderType());
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), specialBuffer, blockState,
                specialModelPart.model(), 1.0F, 1.0F, 1.0F, light, OverlayTexture.NO_OVERLAY);
        }
    }

    @Inject(
        method = "execute(Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lnet/caffeinemc/mods/sodium/client/util/task/CancellationToken;)Lnet/caffeinemc/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
        at = @At("RETURN"))
    private void corners$finishSpecialModelMesh(ChunkBuildContext context, CancellationToken cancellationToken,
                                                CallbackInfoReturnable<ChunkBuildOutput> cir) {
        if (cir.getReturnValue() != null && this.corners$specialModelBuffers != null) {
            SodiumSpecialModelMeshRegistry.submit(((ChunkBuilderTaskAccessor) this).corners$getRender().getPosition(), this.corners$specialModelBuffers);
        }

        this.corners$specialModelBuffers = null;
    }

    @Unique
    private BufferBuilder corners$getOrCreateSpecialModelBuffer(RenderType renderType) {
        return this.corners$specialModelBuffers
            .computeIfAbsent(renderType, SodiumSpecialModelMeshRegistry::createBuilder)
            .buffer();
    }
}
