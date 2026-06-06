package net.ludocrypt.corners.client.render;

import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface ShaderCallback {
    void setup(ShaderInstance shader);

    default int appendOverlayState(BlockAndTintGetter level, BlockPos pos, BlockState state, BakedModel model, long modelSeed) {
        return OverlayTexture.NO_OVERLAY;
    }
}
