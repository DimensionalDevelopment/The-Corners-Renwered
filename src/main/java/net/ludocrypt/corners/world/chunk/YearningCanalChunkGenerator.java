package net.ludocrypt.corners.world.chunk;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.ludocrypt.corners.TheCorners;
import net.ludocrypt.corners.init.CornerWorlds;
import net.ludocrypt.limlib.api.world.LimlibHelper;
import net.ludocrypt.limlib.api.world.Manipulation;
import net.ludocrypt.limlib.api.world.NbtGroup;
import net.ludocrypt.limlib.api.world.NbtPlacerUtil;
import net.ludocrypt.limlib.api.world.chunk.AbstractNbtChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class YearningCanalChunkGenerator extends AbstractNbtChunkGenerator {

    public static final MapCodec<YearningCanalChunkGenerator> CODEC = RecordCodecBuilder.mapCodec((instance) -> {
        return instance.group(BiomeSource.CODEC.fieldOf("biome_source").stable().forGetter((chunkGenerator) -> {
            return chunkGenerator.biomeSource;
        }), NbtGroup.CODEC.fieldOf("group").stable().forGetter((chunkGenerator) -> {
            return chunkGenerator.nbtGroup;
        })).apply(instance, instance.stable(YearningCanalChunkGenerator::new));
    });

    public YearningCanalChunkGenerator(BiomeSource biomeSource, NbtGroup group) {
        super(biomeSource, group);
    }


    private static boolean isInChunk(ChunkAccess chunk, BlockPos pos) {
        return (pos.getX() >> 4) == chunk.getPos().x && (pos.getZ() >> 4) == chunk.getPos().z;
    }


    public static NbtGroup  createGroup() {
        return NbtGroup.Builder
            .create(TheCorners.id(CornerWorlds.YEARNING_CANAL))
            .with("yearning_canal", 1, 14)
            .with("yearning_canal_bottom")
            .with("yearning_canal_hallway")
            .with("yearning_canal_hallway_connected")
            .with("yearning_canal_hallway_decorated", 1, 12)
            .with("yearning_canal_top")
            .with("yearning_canal_with_hallway")
            .build();
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private void generateNbtClipped(WorldGenRegion region, ChunkAccess chunk, BlockPos at, ResourceLocation id) {
        generateNbtClipped(region, chunk, at, id, Manipulation.NONE);
    }

    private void generateNbtClipped(WorldGenRegion region, ChunkAccess chunk, BlockPos at, ResourceLocation id, Manipulation manipulation) {
        try {
            NbtPlacerUtil util = this.structures.eval(id, region.getServer().getResourceManager()).manipulate(manipulation);

            int chunkMinX = chunk.getPos().getMinBlockX();
            int chunkMinZ = chunk.getPos().getMinBlockZ();
            int chunkMaxX = chunkMinX + 16;
            int chunkMaxZ = chunkMinZ + 16;

            int worldMinY = chunk.getMinBuildHeight();
            int worldMaxY = chunk.getMaxBuildHeight();

            int pieceMinX = at.getX();
            int pieceMinY = at.getY();
            int pieceMinZ = at.getZ();

            int pieceMaxX = pieceMinX + util.sizeX;
            int pieceMaxY = pieceMinY + util.sizeY;
            int pieceMaxZ = pieceMinZ + util.sizeZ;

            int fromX = Math.max(pieceMinX, chunkMinX);
            int fromY = Math.max(pieceMinY, worldMinY);
            int fromZ = Math.max(pieceMinZ, chunkMinZ);

            int toX = Math.min(pieceMaxX, chunkMaxX);
            int toY = Math.min(pieceMaxY, worldMaxY);
            int toZ = Math.min(pieceMaxZ, chunkMaxZ);

            if (fromX >= toX || fromY >= toY || fromZ >= toZ) {
                return;
            }

            BlockPos from = new BlockPos(fromX, fromY, fromZ);
            BlockPos to = new BlockPos(toX, toY, toZ);
            BlockPos localOffset = new BlockPos(fromX - pieceMinX, fromY - pieceMinY, fromZ - pieceMinZ);

            util.generateNbt(region, localOffset, from, to, (pos, state, nbt) -> this.modifyStructure(region, pos, state, nbt))
                    .spawnEntities(region, localOffset, from, to, manipulation);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("Attempted to load undefined structure '" + id + "'");
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> populateNoise(WorldGenRegion region, ServerLevel serverLevel, ChunkGenerator generator, ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager) {
        int max = Math.floorDiv(chunk.getMaxBuildHeight(), 54);

        int chunkMinX = chunk.getPos().getMinBlockX();
        int chunkMinZ = chunk.getPos().getMinBlockZ();
        int chunkMaxX = chunkMinX + 16;
        int chunkMaxZ = chunkMinZ + 16;

        int expand = this.getPlacementRadius() * 16;

        int scanMinX = chunkMinX - expand;
        int scanMinZ = chunkMinZ - expand;
        int scanMaxX = chunkMaxX + expand;
        int scanMaxZ = chunkMaxZ + expand;

        for (int x = scanMinX; x < scanMaxX; x++) {
            for (int z = scanMinZ; z < scanMaxZ; z++) {
                BlockPos pos = new BlockPos(x, 0, z);

                boolean isTower = x % 19 == 0 && z % 19 == 0;
                boolean isHallAnchor = x % 16 == 0 && z % 16 == 0;

                if (!isTower && !isHallAnchor) {
                    continue;
                }

                for (int yi = 0; yi < max; yi++) {
                    BlockPos towerPos = pos.offset(0, yi * 54, 0);

                    RandomSource pieceRandom = RandomSource.create(
                            region.getSeed() + LimlibHelper.blockSeed(yi * 2L, yi * 3L, yi)
                    );

                    boolean hallwaySpawnsAtHeight =
                            (pieceRandom.nextDouble() < 0.875D && pieceRandom.nextBoolean()) &&
                                    (yi != 0 && yi != max - 1);

                    Direction dir = Direction.from2DDataValue(pieceRandom.nextInt(4));

                    Rotation rotation = switch (dir) {
                        case NORTH -> Rotation.COUNTERCLOCKWISE_90;
                        case EAST -> Rotation.NONE;
                        case SOUTH -> Rotation.CLOCKWISE_90;
                        default -> Rotation.CLOCKWISE_180;
                    };

                    int xOffset = switch (dir) {
                        case NORTH, SOUTH -> 6;
                        case EAST -> 12;
                        default -> -10;
                    };

                    int yOffset = switch (dir) {
                        case NORTH -> 13;
                        case EAST -> 23;
                        case SOUTH -> 22;
                        default -> 15;
                    };

                    int zOffset = switch (dir) {
                        case NORTH -> -10;
                        case SOUTH -> 12;
                        default -> 6;
                    };

                    BlockPos offsetPos = towerPos.offset(xOffset, yOffset, zOffset);

                    if (isTower) {
                        RandomSource chunkRandom = RandomSource.create(
                                region.getSeed() + LimlibHelper.blockSeed(pos.getX(), pos.getZ(), 50)
                        );

                        boolean tower = chunkRandom.nextDouble() < 0.01 && chunkRandom.nextDouble() < 0.4;

                        if ((tower && (towerPos.getX() != 0 && towerPos.getZ() != 0)) ||
                                (towerPos.getX() == 0 && towerPos.getZ() == 0)) {

                            if (yi == 0) {
                                generateNbtClipped(region, chunk, towerPos, nbtGroup.pick("yearning_canal_bottom", pieceRandom));
                                continue;
                            } else if (yi == max - 1) {
                                generateNbtClipped(region, chunk, towerPos, nbtGroup.pick("yearning_canal_top", pieceRandom));
                                continue;
                            } else {
                                if (hallwaySpawnsAtHeight && !tower) {
                                    generateNbtClipped(region, chunk, towerPos, nbtGroup.pick("yearning_canal_with_hallway", pieceRandom));
                                    generateNbtClipped(region, chunk, offsetPos, nbtGroup.pick("yearning_canal_hallway_connected", pieceRandom), Manipulation.of(rotation));
                                } else {
                                    generateNbtClipped(region, chunk, towerPos, nbtGroup.pick("yearning_canal", pieceRandom));
                                }
                            }
                        }
                    }

                    if (isHallAnchor && hallwaySpawnsAtHeight) {
                        if (
                                (dir == Direction.NORTH && towerPos.getX() == 0 && towerPos.getZ() <= 0) ||
                                        (dir == Direction.WEST && towerPos.getZ() == 0 && towerPos.getX() <= 0) ||
                                        (dir == Direction.SOUTH && towerPos.getX() == 0 && towerPos.getZ() >= 1) ||
                                        (dir == Direction.EAST && towerPos.getZ() == 0 && towerPos.getX() >= 1)
                        ) {
                            RandomSource hallRandom = RandomSource.create(
                                    region.getSeed() + LimlibHelper.blockSeed(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ())
                            );

                            String selectedGroup = hallRandom.nextInt(27) == 0
                                    ? "yearning_canal_hallway"
                                    : "yearning_canal_hallway_decorated";

                            generateNbtClipped(
                                    region,
                                    chunk,
                                    offsetPos.offset(0, 4, 0),
                                    nbtGroup.pick(selectedGroup, hallRandom),
                                    Manipulation.of(rotation)
                            );
                        }
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getPlacementRadius() {
        return 2;
    }

    @Override
    public int getGenDepth() {
        return 2032;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos pos) {
    }

}
