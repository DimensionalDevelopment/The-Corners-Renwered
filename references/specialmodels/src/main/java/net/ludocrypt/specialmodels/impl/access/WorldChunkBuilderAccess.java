package net.ludocrypt.specialmodels.impl.access;

import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBufferBuilderStorage;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBuiltChunkStorage;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface WorldChunkBuilderAccess {

	public SpecialChunkBuilder getSpecialChunkBuilder();

	public Future<?> getLastFullSpecialBuiltChunkUpdate();

	public BlockingQueue<SpecialChunkBuilder.BuiltChunk> getRecentlyCompiledSpecialChunks();

	public AtomicReference<SpecialChunkBuilder.RenderableChunks> getRenderableSpecialChunks();

	public ObjectArrayList<SpecialChunkBuilder.ChunkInfo> getSpecialChunkInfoList();

	public SpecialBuiltChunkStorage getSpecialChunks();

	public SpecialBufferBuilderStorage getSpecialBufferBuilderStorage();

	public boolean shouldNeedsFullSpecialBuiltChunkUpdate();

	public AtomicBoolean shouldNeedsSpecialFrustumUpdate();

	public AtomicLong getNextFullSpecialUpdateMilliseconds();

	public void setWorldSpecial(ClientWorld world);

	public void reloadSpecial();

	public void setupSpecialTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator);

	public void addSpecialChunksToBuild(Camera camera, Queue<SpecialChunkBuilder.ChunkInfo> chunkInfoQueue);

	public void addSpecialBuiltChunk(SpecialChunkBuilder.BuiltChunk builtChunk);

	public void updateSpecialBuiltChunks(LinkedHashSet<SpecialChunkBuilder.ChunkInfo> builtChunks,
			SpecialChunkBuilder.ChunkInfoListMap builtChunkMap, Vec3d cameraPos,
			Queue<SpecialChunkBuilder.ChunkInfo> chunksToBuild, boolean chunkCullingEnabled);

	@Nullable
	public SpecialChunkBuilder.BuiltChunk getAdjacentSpecialChunk(BlockPos pos, SpecialChunkBuilder.BuiltChunk chunk,
			Direction direction);

	public boolean isSpecialChunkNearMaxViewDistance(BlockPos blockPos, SpecialChunkBuilder.BuiltChunk builtChunk);

	public void applySpecialFrustum(Frustum frustum);

	public void findSpecialChunksToRebuild(Camera camera);

}
