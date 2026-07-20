package net.ludocrypt.corners.world.feature;

import net.ludocrypt.corners.init.CornerBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import java.util.Optional;

public class GaiaSaplingGenerator {


    // Replacement for AbstractMegaTreeGrower subclass:
    // - 'tree' -> single-sapling feature
    // - 'megaTree' -> 2×2 feature
    public static final TreeGrower GAIA = new TreeGrower(
            "gaia",
            Optional.of(CornerBiomes.CONFIGURED_GAIA_TREE_FEATURE),                 // tree
            Optional.of(CornerBiomes.CONFIGURED_SAPLING_GAIA_TREE_FEATURE),        // megaTree (2×2)
            Optional.empty()                                                       // flowers (unused)
    );

    public static boolean generateRadio(ServerLevel level,
                                      ChunkGenerator generator,
                                      BlockPos pos,
                                      BlockState state,
                                      RandomSource random) {
        Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolder(CornerBiomes.CONFIGURED_GAIA_TREE_FEATURE)
                .orElse(null);
        return holder != null && holder.value().place(level, generator, random, pos);
    }
}
