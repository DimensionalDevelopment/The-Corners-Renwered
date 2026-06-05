package net.ludocrypt.specialmodels.impl.mixin.render;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.ludocrypt.specialmodels.impl.access.WorldChunkBuilderAccess;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBufferBuilderStorage;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBuiltChunkStorage;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.BuiltChunk;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.ChunkData;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.ChunkInfo;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.RenderableChunks;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.ChunkUpdatesPrioritization;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.chunk.ChunkRenderRegionCache;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ThreadedChunkManager;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.chunk.light.LightingProvider;

@Mixin(WorldRenderer.class)
public class WorldRendererChunkMixin implements WorldChunkBuilderAccess {

	@Shadow
	private ClientWorld world;
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	private int viewDistance;
	@Shadow
	@Final
	private static double CEIL_CUBEROOT_3_TIMES_16;
	@Unique
	private SpecialChunkBuilder specialChunkBuilder;
	@Unique
	private Future<?> lastFullSpecialBuiltChunkUpdate;
	@Unique
	private final BlockingQueue<SpecialChunkBuilder.BuiltChunk> recentlyCompiledSpecialChunks = new LinkedBlockingQueue<BuiltChunk>();
	@Unique
	private final AtomicReference<SpecialChunkBuilder.RenderableChunks> renderableSpecialChunks = new AtomicReference<RenderableChunks>();
	@Unique
	private final ObjectArrayList<SpecialChunkBuilder.ChunkInfo> specialChunkInfoList = new ObjectArrayList<>(10000);
	@Unique
	private SpecialBuiltChunkStorage specialChunks;
	@Unique
	private SpecialBufferBuilderStorage specialBufferBuilderStorage = new SpecialBufferBuilderStorage();
	@Unique
	private boolean needsFullSpecialBuiltChunkUpdate = true;
	@Unique
	private final AtomicBoolean needsSpecialFrustumUpdate = new AtomicBoolean(false);
	@Unique
	private final AtomicLong nextFullSpecialUpdateMilliseconds = new AtomicLong(0L);
	@Unique
	private int cameraSpecialChunkX = Integer.MIN_VALUE;
	@Unique
	private int cameraSpecialChunkY = Integer.MIN_VALUE;
	@Unique
	private int cameraSpecialChunkZ = Integer.MIN_VALUE;
	@Unique
	private double lastSpecialCameraX = Double.MIN_VALUE;
	@Unique
	private double lastSpecialCameraY = Double.MIN_VALUE;
	@Unique
	private double lastSpecialCameraZ = Double.MIN_VALUE;
	@Unique
	private double lastSpecialCameraPitch = Double.MIN_VALUE;
	@Unique
	private double lastSpecialCameraYaw = Double.MIN_VALUE;

	@Inject(method = "setWorld", at = @At("TAIL"))
	private void specialModels$setWorld(ClientWorld world, CallbackInfo ci) {
		this.setWorldSpecial(world);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;reload()V", at = @At("HEAD"))
	private void specialModels$reload(CallbackInfo ci) {
		this.reloadSpecial();
	}

	@Inject(method = "updateBlock", at = @At("TAIL"))
	private void specialModels$updateBlock(BlockView world, BlockPos pos, BlockState oldState, BlockState newState,
			int flags, CallbackInfo ci) {
		this.scheduleSpecialSectionRender(pos, (flags & 8) != 0);
	}

	@Inject(method = "scheduleBlockRender", at = @At("TAIL"))
	private void specialModels$scheduleBlockRender(int x, int y, int z, CallbackInfo ci) {
		this.scheduleSpecialBlockRender(x, y, z);
	}

	@Inject(method = "scheduleBlockRerenderIfNeeded", at = @At("TAIL"))
	private void specialModels$scheduleBlockRerenderIfNeeded(BlockPos pos, BlockState old, BlockState updated,
			CallbackInfo ci) {

		if (this.client.getBakedModelManager().shouldRerender(old, updated)) {
			this.scheduleSpecialBlockRenders(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
		}

	}

	@Inject(method = "scheduleTerrainUpdate", at = @At("TAIL"))
	private void specialModels$scheduleBlockRerenderIfNeeded(CallbackInfo ci) {
		this.needsFullSpecialBuiltChunkUpdate = true;
	}

	private void scheduleSpecialSectionRender(BlockPos pos, boolean important) {

		for (int i = pos.getZ() - 1; i <= pos.getZ() + 1; ++i) {

			for (int j = pos.getX() - 1; j <= pos.getX() + 1; ++j) {

				for (int k = pos.getY() - 1; k <= pos.getY() + 1; ++k) {
					this
						.scheduleSpecialChunkRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k),
							ChunkSectionPos.getSectionCoord(i), important);
				}

			}

		}

	}

	public void scheduleSpecialBlockRenders(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

		for (int i = minZ - 1; i <= maxZ + 1; ++i) {

			for (int j = minX - 1; j <= maxX + 1; ++j) {

				for (int k = minY - 1; k <= maxY + 1; ++k) {
					this
						.scheduleSpecialBlockRender(ChunkSectionPos.getSectionCoord(j), ChunkSectionPos.getSectionCoord(k),
							ChunkSectionPos.getSectionCoord(i));
				}

			}

		}

	}

	public void scheduleSpecialBlockRenders(int x, int y, int z) {

		for (int i = z - 1; i <= z + 1; ++i) {

			for (int j = x - 1; j <= x + 1; ++j) {

				for (int k = y - 1; k <= y + 1; ++k) {
					this.scheduleSpecialBlockRender(j, k, i);
				}

			}

		}

	}

	public void scheduleSpecialBlockRender(int x, int y, int z) {
		this.scheduleSpecialChunkRender(x, y, z, false);
	}

	private void scheduleSpecialChunkRender(int x, int y, int z, boolean important) {
		this.specialChunks.scheduleRebuild(x, y, z, important);
	}

	@Override
	public void setWorldSpecial(ClientWorld world) {

		if (world == null) {

			if (this.specialChunks != null) {
				this.specialChunks.clear();
				this.specialChunks = null;
			}

			if (this.specialChunkBuilder != null) {
				this.specialChunkBuilder.stop();
			}

			this.specialChunkBuilder = null;
			this.renderableSpecialChunks.set(null);
			this.specialChunkInfoList.clear();
		} else {
			this.reloadSpecial();
		}

	}

	@Override
	public void reloadSpecial() {

		if (this.world != null) {

			if (this.specialChunkBuilder == null) {
				this.specialChunkBuilder = new SpecialChunkBuilder(this.world, ((WorldRenderer) (Object) this),
					Util.getMainWorkerExecutor(), this.client.is64Bit(), this.specialBufferBuilderStorage);
			} else {
				this.specialChunkBuilder.setWorld(this.world);
			}

			this.needsFullSpecialBuiltChunkUpdate = true;
			this.recentlyCompiledSpecialChunks.clear();
			RenderLayers.setFancyGraphicsOrBetter(MinecraftClient.isFancyGraphicsOrBetter());
			this.viewDistance = this.client.options.getEffectiveViewDistance();

			if (this.specialChunks != null) {
				this.specialChunks.clear();
			}

			this.specialChunkBuilder.reset();
			this.specialChunks = new SpecialBuiltChunkStorage(this.specialChunkBuilder, this.world,
				this.client.options.getEffectiveViewDistance(), ((WorldRenderer) (Object) this));

			if (this.lastFullSpecialBuiltChunkUpdate != null) {

				try {
					this.lastFullSpecialBuiltChunkUpdate.get();
					this.lastFullSpecialBuiltChunkUpdate = null;
				} catch (Exception var3) {
				}

			}

			this.renderableSpecialChunks.set(new SpecialChunkBuilder.RenderableChunks(this.specialChunks.chunks.length));
			this.specialChunkInfoList.clear();
			Entity entity = this.client.getCameraEntity();

			if (entity != null) {
				this.specialChunks.updateCameraPosition(entity.getX(), entity.getZ());
			}

		}

	}

	@Override
	public void setupSpecialTerrain(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator) {
		Vec3d vec3d = camera.getPos();

		if (this.client.options.getEffectiveViewDistance() != this.viewDistance) {
			this.reloadSpecial();
		}

		this.world.getProfiler().push("camera");
		double d = this.client.player.getX();
		double e = this.client.player.getY();
		double f = this.client.player.getZ();
		int i = ChunkSectionPos.getSectionCoord(d);
		int j = ChunkSectionPos.getSectionCoord(e);
		int k = ChunkSectionPos.getSectionCoord(f);

		if (this.cameraSpecialChunkX != i || this.cameraSpecialChunkY != j || this.cameraSpecialChunkZ != k) {
			this.cameraSpecialChunkX = i;
			this.cameraSpecialChunkY = j;
			this.cameraSpecialChunkZ = k;
			this.specialChunks.updateCameraPosition(d, f);
		}

		this.specialChunkBuilder.setCameraPosition(vec3d);
		this.world.getProfiler().swap("cull");
		this.client.getProfiler().swap("culling");
		BlockPos blockPos = camera.getBlockPos();
		double g = Math.floor(vec3d.x / 8.0);
		double h = Math.floor(vec3d.y / 8.0);
		double l = Math.floor(vec3d.z / 8.0);
		this.needsFullSpecialBuiltChunkUpdate = this.needsFullSpecialBuiltChunkUpdate || g != this.lastSpecialCameraX || h != this.lastSpecialCameraY || l != this.lastSpecialCameraZ;
		this.nextFullSpecialUpdateMilliseconds.updateAndGet(lx -> {

			if (lx > 0L && System.currentTimeMillis() > lx) {
				this.needsFullSpecialBuiltChunkUpdate = true;
				return 0L;
			} else {
				return lx;
			}

		});
		this.lastSpecialCameraX = g;
		this.lastSpecialCameraY = h;
		this.lastSpecialCameraZ = l;
		this.client.getProfiler().swap("update");
		boolean bl = this.client.chunkCullingEnabled;

		if (spectator && this.world.getBlockState(blockPos).isOpaqueFullCube(this.world, blockPos)) {
			bl = false;
		}

		if (!hasForcedFrustum) {

			if (this.needsFullSpecialBuiltChunkUpdate && (this.lastFullSpecialBuiltChunkUpdate == null || this.lastFullSpecialBuiltChunkUpdate
				.isDone())) {
				this.client.getProfiler().push("full_update_schedule");
				this.needsFullSpecialBuiltChunkUpdate = false;
				boolean bl2 = bl;
				this.lastFullSpecialBuiltChunkUpdate = Util.getMainWorkerExecutor().submit(() -> {
					Queue<SpecialChunkBuilder.ChunkInfo> queue = Queues.<SpecialChunkBuilder.ChunkInfo>newArrayDeque();
					this.addSpecialChunksToBuild(camera, queue);
					SpecialChunkBuilder.RenderableChunks renderableChunksx = new SpecialChunkBuilder.RenderableChunks(
						this.specialChunks.chunks.length);
					this
						.updateSpecialBuiltChunks(renderableChunksx.builtChunks, renderableChunksx.builtChunkMap, vec3d,
							queue, bl2);
					this.renderableSpecialChunks.set(renderableChunksx);
					this.needsSpecialFrustumUpdate.set(true);
				});
				this.client.getProfiler().pop();
			}

			SpecialChunkBuilder.RenderableChunks renderableChunks = (SpecialChunkBuilder.RenderableChunks) this.renderableSpecialChunks
				.get();

			if (!this.recentlyCompiledSpecialChunks.isEmpty()) {
				this.client.getProfiler().push("partial_update");
				Queue<SpecialChunkBuilder.ChunkInfo> queue = Queues.<SpecialChunkBuilder.ChunkInfo>newArrayDeque();

				while (!this.recentlyCompiledSpecialChunks.isEmpty()) {
					SpecialChunkBuilder.BuiltChunk builtChunk = (SpecialChunkBuilder.BuiltChunk) this.recentlyCompiledSpecialChunks
						.poll();
					SpecialChunkBuilder.ChunkInfo chunkInfo = renderableChunks.builtChunkMap.getInfo(builtChunk);

					if (chunkInfo != null && chunkInfo.chunk == builtChunk) {
						queue.add(chunkInfo);
					}

				}

				this
					.updateSpecialBuiltChunks(renderableChunks.builtChunks, renderableChunks.builtChunkMap, vec3d, queue,
						bl);
				this.needsSpecialFrustumUpdate.set(true);
				this.client.getProfiler().pop();
			}

			double m = Math.floor((double) (camera.getPitch() / 2.0F));
			double n = Math.floor((double) (camera.getYaw() / 2.0F));

			if (this.needsSpecialFrustumUpdate
				.compareAndSet(true, false) || m != this.lastSpecialCameraPitch || n != this.lastSpecialCameraYaw) {
				this.applySpecialFrustum(new Frustum(frustum).offsetToIncludeCamera(8));
				this.lastSpecialCameraPitch = m;
				this.lastSpecialCameraYaw = n;
			}

		}

		this.client.getProfiler().pop();
	}

	@Override
	public void addSpecialChunksToBuild(Camera camera, Queue<SpecialChunkBuilder.ChunkInfo> chunkInfoQueue) {
		Vec3d vec3d = camera.getPos();
		BlockPos blockPos = camera.getBlockPos();
		SpecialChunkBuilder.BuiltChunk builtChunk = this.specialChunks.getRenderedChunk(blockPos);

		if (builtChunk == null) {
			boolean bl = blockPos.getY() > this.world.getBottomY();
			int j = bl ? this.world.getTopY() - 8 : this.world.getBottomY() + 8;
			int k = MathHelper.floor(vec3d.x / 16.0) * 16;
			int l = MathHelper.floor(vec3d.z / 16.0) * 16;
			List<SpecialChunkBuilder.ChunkInfo> list = Lists.<SpecialChunkBuilder.ChunkInfo>newArrayList();

			for (int m = -this.viewDistance; m <= this.viewDistance; ++m) {

				for (int n = -this.viewDistance; n <= this.viewDistance; ++n) {
					SpecialChunkBuilder.BuiltChunk builtChunk2 = this.specialChunks
						.getRenderedChunk(
							new BlockPos(k + ChunkSectionPos.getOffsetPos(m, 8), j, l + ChunkSectionPos.getOffsetPos(n, 8)));

					if (builtChunk2 != null) {
						list.add(new SpecialChunkBuilder.ChunkInfo(builtChunk2, null, 0));
					}

				}

			}

			list
				.sort(Comparator
					.comparingDouble(chunkInfo -> blockPos.getSquaredDistance(chunkInfo.chunk.getOrigin().add(8, 8, 8))));
			chunkInfoQueue.addAll(list);
		} else {
			chunkInfoQueue.add(new SpecialChunkBuilder.ChunkInfo(builtChunk, null, 0));
		}

	}

	@Override
	public void addSpecialBuiltChunk(SpecialChunkBuilder.BuiltChunk builtChunk) {
		this.recentlyCompiledSpecialChunks.add(builtChunk);
	}

	@Override
	public void updateSpecialBuiltChunks(LinkedHashSet<ChunkInfo> builtChunks,
			SpecialChunkBuilder.ChunkInfoListMap builtChunkMap, Vec3d cameraPos, Queue<ChunkInfo> chunksToBuild,
			boolean chunkCullingEnabled) {
		BlockPos blockPos = new BlockPos(MathHelper.floor(cameraPos.x / 16.0) * 16,
			MathHelper.floor(cameraPos.y / 16.0) * 16, MathHelper.floor(cameraPos.z / 16.0) * 16);
		BlockPos blockPos2 = blockPos.add(8, 8, 8);
		Entity
			.setRenderDistanceMultiplier(MathHelper
				.clamp((double) this.client.options.getEffectiveViewDistance() / 8.0, 1.0,
					2.5) * this.client.options.getEntityDistanceScaling().get());

		while (!chunksToBuild.isEmpty()) {
			ChunkInfo chunkInfo = chunksToBuild.poll();
			SpecialChunkBuilder.BuiltChunk builtChunk = chunkInfo.chunk;
			builtChunks.add(chunkInfo);
			boolean bl = Math.abs(builtChunk.getOrigin().getX() - blockPos.getX()) > 60 || Math
				.abs(builtChunk.getOrigin().getY() - blockPos.getY()) > 60 || Math
					.abs(builtChunk.getOrigin().getZ() - blockPos.getZ()) > 60;
			Direction[] DIRECTIONS = Direction.values();

			for (Direction direction : DIRECTIONS) {
				SpecialChunkBuilder.BuiltChunk builtChunk2 = this.getAdjacentSpecialChunk(blockPos, builtChunk, direction);

				if (builtChunk2 != null && (!chunkCullingEnabled || !chunkInfo.canCull(direction.getOpposite()))) {

					if (chunkCullingEnabled && chunkInfo.hasAnyDirection()) {
						ChunkData chunkData = builtChunk.getData();
						boolean bl2 = false;

						for (int j = 0; j < DIRECTIONS.length; ++j) {

							if (chunkInfo.hasDirection(j) && chunkData
								.isVisibleThrough(DIRECTIONS[j].getOpposite(), direction)) {
								bl2 = true;
								break;
							}

						}

						if (!bl2) {
							continue;
						}

					}

					if (chunkCullingEnabled && bl) {
						BlockPos blockPos3;
						byte var10001;

						label126: {

							label125: {
								blockPos3 = builtChunk2.getOrigin();

								if (direction.getAxis() == Direction.Axis.X) {

									if (blockPos2.getX() > blockPos3.getX()) {
										break label125;
									}

								} else if (blockPos2.getX() < blockPos3.getX()) {
									break label125;
								}

								var10001 = 0;
								break label126;
							}

							var10001 = 16;
						}

						byte var10002;

						label118: {

							label117: {

								if (direction.getAxis() == Direction.Axis.Y) {

									if (blockPos2.getY() > blockPos3.getY()) {
										break label117;
									}

								} else if (blockPos2.getY() < blockPos3.getY()) {
									break label117;
								}

								var10002 = 0;
								break label118;
							}

							var10002 = 16;
						}

						byte var10003;

						label110: {

							label109: {

								if (direction.getAxis() == Direction.Axis.Z) {

									if (blockPos2.getZ() > blockPos3.getZ()) {
										break label109;
									}

								} else if (blockPos2.getZ() < blockPos3.getZ()) {
									break label109;
								}

								var10003 = 0;
								break label110;
							}

							var10003 = 16;
						}

						BlockPos blockPos4 = blockPos3.add(var10001, var10002, var10003);
						Vec3d vec3d = new Vec3d((double) blockPos4.getX(), (double) blockPos4.getY(),
							(double) blockPos4.getZ());
						Vec3d vec3d2 = cameraPos.subtract(vec3d).normalize().multiply(CEIL_CUBEROOT_3_TIMES_16);
						boolean bl3 = true;

						while (cameraPos.subtract(vec3d).lengthSquared() > 3600.0) {
							vec3d = vec3d.add(vec3d2);

							if (vec3d.y > (double) this.world.getTopY() || vec3d.y < (double) this.world.getBottomY()) {
								break;
							}

							SpecialChunkBuilder.BuiltChunk builtChunk3 = this.specialChunks
								.getRenderedChunk(BlockPos.create(vec3d.x, vec3d.y, vec3d.z));

							if (builtChunk3 == null || builtChunkMap.getInfo(builtChunk3) == null) {
								bl3 = false;
								break;
							}

						}

						if (!bl3) {
							continue;
						}

					}

					ChunkInfo chunkInfo2 = builtChunkMap.getInfo(builtChunk2);

					if (chunkInfo2 != null) {
						chunkInfo2.addDirection(direction);
					} else if (!builtChunk2.shouldBuild()) {

						if (!this.isSpecialChunkNearMaxViewDistance(blockPos, builtChunk)) {
							this.nextFullSpecialUpdateMilliseconds.set(System.currentTimeMillis() + 500L);
						}

					} else {
						ChunkInfo chunkInfo3 = new ChunkInfo(builtChunk2, direction, chunkInfo.propagationLevel + 1);
						chunkInfo3.updateCullingState(chunkInfo.cullingState, direction);
						chunksToBuild.add(chunkInfo3);
						builtChunkMap.setInfo(builtChunk2, chunkInfo3);
					}

				}

			}

		}

	}

	@Nullable
	@Override
	public SpecialChunkBuilder.BuiltChunk getAdjacentSpecialChunk(BlockPos pos, SpecialChunkBuilder.BuiltChunk chunk,
			Direction direction) {
		BlockPos blockPos = chunk.getNeighborPosition(direction);

		if (MathHelper.abs(pos.getX() - blockPos.getX()) > this.viewDistance * 16) {
			return null;
		} else if (MathHelper.abs(pos.getY() - blockPos.getY()) > this.viewDistance * 16 || blockPos.getY() < this.world
			.getBottomY() || blockPos.getY() >= this.world.getTopY()) {
			return null;
		} else {
			return MathHelper.abs(pos.getZ() - blockPos.getZ()) > this.viewDistance * 16 ? null
					: this.specialChunks.getRenderedChunk(blockPos);
		}

	}

	@Override
	public boolean isSpecialChunkNearMaxViewDistance(BlockPos blockPos, SpecialChunkBuilder.BuiltChunk builtChunk) {
		int i = ChunkSectionPos.getSectionCoord(blockPos.getX());
		int j = ChunkSectionPos.getSectionCoord(blockPos.getZ());
		BlockPos blockPos2 = builtChunk.getOrigin();
		int k = ChunkSectionPos.getSectionCoord(blockPos2.getX());
		int l = ChunkSectionPos.getSectionCoord(blockPos2.getZ());
		return !ThreadedChunkManager.isWithinDistance(k, l, i, j, this.viewDistance - 2);
	}

	@Override
	public void applySpecialFrustum(Frustum frustum) {

		if (!MinecraftClient.getInstance().isOnThread()) {
			throw new IllegalStateException("applyFrustum called from wrong thread: " + Thread.currentThread().getName());
		} else {
			this.client.getProfiler().push("apply_frustum");
			this.specialChunkInfoList.clear();

			for (SpecialChunkBuilder.ChunkInfo chunkInfo : ((SpecialChunkBuilder.RenderableChunks) this.renderableSpecialChunks
				.get()).builtChunks) {

				if (frustum.isVisible(chunkInfo.chunk.getBoundingBox())) {
					this.specialChunkInfoList.add(chunkInfo);
				}

			}

			this.client.getProfiler().pop();
		}

	}

	@Override
	public void findSpecialChunksToRebuild(Camera camera) {
		this.client.getProfiler().push("populate_chunks_to_compile");
		LightingProvider lightingProvider = this.world.getLightingProvider();
		ChunkRenderRegionCache chunkRenderRegionCache = new ChunkRenderRegionCache();
		BlockPos blockPos = camera.getBlockPos();
		List<SpecialChunkBuilder.BuiltChunk> list = Lists.<SpecialChunkBuilder.BuiltChunk>newArrayList();

		for (SpecialChunkBuilder.ChunkInfo chunkInfo : this.specialChunkInfoList) {
			SpecialChunkBuilder.BuiltChunk builtChunk = chunkInfo.chunk;
			ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(builtChunk.getOrigin());

			if (builtChunk.needsRebuild() && lightingProvider.isLightingEnabled(chunkSectionPos)) {
				boolean bl = false;

				if (this.client.options.getPrioritizeChunkUpdates().get() == ChunkUpdatesPrioritization.NEARBY) {
					BlockPos blockPos2 = builtChunk.getOrigin().add(8, 8, 8);
					bl = blockPos2.getSquaredDistance(blockPos) < 768.0 || builtChunk.needsImportantRebuild();
				} else if (this.client.options
					.getPrioritizeChunkUpdates()
					.get() == ChunkUpdatesPrioritization.PLAYER_AFFECTED) {
					bl = builtChunk.needsImportantRebuild();
				}

				if (bl) {
					this.client.getProfiler().push("build_near_sync");
					this.specialChunkBuilder.rebuild(builtChunk, chunkRenderRegionCache);
					builtChunk.cancelRebuild();
					this.client.getProfiler().pop();
				} else {
					list.add(builtChunk);
				}

			}

		}

		this.client.getProfiler().swap("upload");
		this.specialChunkBuilder.upload();
		this.client.getProfiler().swap("schedule_async_compile");

		for (SpecialChunkBuilder.BuiltChunk builtChunk2 : list) {
			builtChunk2.scheduleRebuild(this.specialChunkBuilder, chunkRenderRegionCache);
			builtChunk2.cancelRebuild();
		}

		this.client.getProfiler().pop();
	}

	@Override
	public SpecialChunkBuilder getSpecialChunkBuilder() {
		return specialChunkBuilder;
	}

	@Override
	public Future<?> getLastFullSpecialBuiltChunkUpdate() {
		return lastFullSpecialBuiltChunkUpdate;
	}

	@Override
	public BlockingQueue<BuiltChunk> getRecentlyCompiledSpecialChunks() {
		return recentlyCompiledSpecialChunks;
	}

	@Override
	public AtomicReference<RenderableChunks> getRenderableSpecialChunks() {
		return renderableSpecialChunks;
	}

	@Override
	public ObjectArrayList<ChunkInfo> getSpecialChunkInfoList() {
		return specialChunkInfoList;
	}

	@Override
	public SpecialBuiltChunkStorage getSpecialChunks() {
		return specialChunks;
	}

	@Override
	public SpecialBufferBuilderStorage getSpecialBufferBuilderStorage() {
		return specialBufferBuilderStorage;
	}

	@Override
	public boolean shouldNeedsFullSpecialBuiltChunkUpdate() {
		return needsFullSpecialBuiltChunkUpdate;
	}

	@Override
	public AtomicBoolean shouldNeedsSpecialFrustumUpdate() {
		return needsSpecialFrustumUpdate;
	}

	@Override
	public AtomicLong getNextFullSpecialUpdateMilliseconds() {
		return nextFullSpecialUpdateMilliseconds;
	}

}
