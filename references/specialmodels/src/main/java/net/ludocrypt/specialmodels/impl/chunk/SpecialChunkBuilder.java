package net.ludocrypt.specialmodels.impl.chunk;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormats;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.ludocrypt.specialmodels.impl.access.BakedModelAccess;
import net.ludocrypt.specialmodels.impl.access.WorldChunkBuilderAccess;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBufferBuilder.RenderedBuffer;
import net.ludocrypt.specialmodels.impl.chunk.SpecialBufferBuilder.SortState;
import net.ludocrypt.specialmodels.impl.chunk.SpecialChunkBuilder.BuiltChunk.Task;
import net.ludocrypt.specialmodels.impl.render.MutableQuad;
import net.ludocrypt.specialmodels.impl.render.MutableVertice;
import net.ludocrypt.specialmodels.impl.render.SpecialVertexFormats;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.ChunkOcclusionDataBuilder;
import net.minecraft.client.render.chunk.ChunkRenderRegion;
import net.minecraft.client.render.chunk.ChunkRenderRegionCache;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.data.client.model.BlockStateVariantMap.QuadFunction;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.thread.TaskExecutor;
import net.minecraft.world.chunk.ChunkStatus;

@ClientOnly
public class SpecialChunkBuilder {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final PriorityBlockingQueue<Task> highPriorityChunksToBuild = Queues.newPriorityBlockingQueue();
	private final Queue<Task> chunksToBuild = Queues.<Task>newLinkedBlockingDeque();

	private int highPriorityQuota = 2;

	private final Queue<SpecialBufferBuilderStorage> threadBuffers;
	private final Queue<Runnable> uploadQueue = Queues.newConcurrentLinkedQueue();

	private volatile int queuedTaskCount;
	private volatile int bufferCount;

	private final SpecialBufferBuilderStorage buffers;
	private final TaskExecutor<Runnable> mailbox;
	private final Executor executor;

	private MinecraftClient client;
	private WorldRenderer worldRenderer;
	private ClientWorld world;

	private Vec3d cameraPosition = Vec3d.ZERO;

	public SpecialChunkBuilder(ClientWorld world, WorldRenderer renderer, Executor executor, boolean useMaxThreads,
			SpecialBufferBuilderStorage buffers) {
		this.client = MinecraftClient.getInstance();
		this.worldRenderer = renderer;
		this.world = world;
		this.buffers = buffers;

		int layer = Math
			.max(1,
				(int) (Runtime.getRuntime().maxMemory() * 0.3) / (RenderLayer
					.getSolid()
					.getExpectedBufferSize() * SpecialModelRenderer.SPECIAL_MODEL_RENDERER.size() * 4) - 1);

		int avaliable = Runtime.getRuntime().availableProcessors();
		int minThreads = useMaxThreads ? avaliable : Math.min(avaliable, 4);
		int maxThreads = Math.max(1, Math.min(minThreads, layer));

		List<SpecialBufferBuilderStorage> storage = Lists.newArrayListWithExpectedSize(maxThreads);

		try {

			for (int i = 0; i < maxThreads; ++i) {
				storage.add(new SpecialBufferBuilderStorage());
			}

		} catch (OutOfMemoryError e) {
			LOGGER.warn("Allocated only {}/{} buffers", storage.size(), maxThreads);

			int size = Math.min(storage.size() * 2 / 3, storage.size() - 1);

			for (int i = 0; i < size; ++i) {
				storage.remove(storage.size() - 1);
			}

			System.gc();
		}

		this.threadBuffers = Queues.newArrayDeque(storage);
		this.bufferCount = this.threadBuffers.size();

		this.executor = executor;

		this.mailbox = TaskExecutor.create(executor, "Special Chunk Renderer");
		this.mailbox.send(this::scheduleRunTasks);
	}

	public void setWorld(ClientWorld world) {
		this.world = world;
	}

	private void scheduleRunTasks() {

		if (!this.threadBuffers.isEmpty()) {
			Task task = this.getNextBuildTask();

			if (task != null) {
				SpecialBufferBuilderStorage storage = this.threadBuffers.poll();
				this.queuedTaskCount = this.highPriorityChunksToBuild.size() + this.chunksToBuild.size();
				this.bufferCount = this.threadBuffers.size();
				CompletableFuture
					.supplyAsync(Util.debugSupplier(task.name(), () -> task.run(storage)), this.executor)
					.thenCompose(future -> future)
					.whenComplete((result, throwable) -> {

						if (throwable != null) {
							MinecraftClient.getInstance().setCrashReport(CrashReport.create(throwable, "Batching chunks"));
						} else {
							this.mailbox.send(() -> {

								if (result == SpecialChunkBuilder.Result.SUCCESSFUL) {
									storage.clear();
								} else {
									storage.reset();
								}

								this.threadBuffers.add(storage);
								this.bufferCount = this.threadBuffers.size();
								this.scheduleRunTasks();
							});
						}

					});
			}

		}

	}

	@Nullable
	private Task getNextBuildTask() {

		if (this.highPriorityQuota <= 0) {
			Task task = this.chunksToBuild.poll();

			if (task != null) {
				this.highPriorityQuota = 2;
				return task;
			}

		}

		Task task = this.highPriorityChunksToBuild.poll();

		if (task != null) {
			--this.highPriorityQuota;
			return task;
		} else {
			this.highPriorityQuota = 2;
			return this.chunksToBuild.poll();
		}

	}

	public String getDebugString() {
		return String
			.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.queuedTaskCount, this.uploadQueue.size(),
				this.bufferCount);
	}

	public int getToBatchCount() {
		return this.queuedTaskCount;
	}

	public int getChunksToUpload() {
		return this.uploadQueue.size();
	}

	public int getFreeBufferCount() {
		return this.bufferCount;
	}

	public void setCameraPosition(Vec3d cameraPosition) {
		this.cameraPosition = cameraPosition;
	}

	public Vec3d getCameraPosition() {
		return this.cameraPosition;
	}

	public void upload() {
		Runnable poll;

		while ((poll = this.uploadQueue.poll()) != null) {
			poll.run();
		}

	}

	public void rebuild(BuiltChunk chunk, ChunkRenderRegionCache cache) {
		chunk.rebuild(cache);
	}

	public void reset() {
		this.clear();
	}

	public void send(Task task) {
		this.mailbox.send(() -> {

			if (task.highPriority) {
				this.highPriorityChunksToBuild.offer(task);
			} else {
				this.chunksToBuild.offer(task);
			}

			this.queuedTaskCount = this.highPriorityChunksToBuild.size() + this.chunksToBuild.size();
			this.scheduleRunTasks();
		});
	}

	public CompletableFuture<Void> scheduleUpload(SpecialModelRenderer modelRenderer, RenderedBuffer renderedBuffer,
			VertexBuffer buffer) {
		return CompletableFuture.runAsync(() -> {

			if (!buffer.invalid()) {
				buffer.bind();
				renderedBuffer.upload(buffer);
				VertexBuffer.unbind();
			}

		}, this.uploadQueue::add);
	}

	private void clear() {

		while (!this.highPriorityChunksToBuild.isEmpty()) {
			Task task = this.highPriorityChunksToBuild.poll();

			if (task != null) {
				task.cancel();
			}

		}

		while (!this.chunksToBuild.isEmpty()) {
			Task task = this.chunksToBuild.poll();

			if (task != null) {
				task.cancel();
			}

		}

		this.queuedTaskCount = 0;
	}

	public boolean isEmpty() {
		return this.queuedTaskCount == 0 && this.uploadQueue.isEmpty();
	}

	public void stop() {
		this.clear();
		this.mailbox.close();
		this.threadBuffers.clear();
	}

	public class BuiltChunk {

		public final int index;

		public final AtomicReference<ChunkData> data = new AtomicReference<ChunkData>(ChunkData.EMPTY);
		private final AtomicInteger cancelledInitialBuilds = new AtomicInteger(0);

		@Nullable
		private RebuildTask rebuildTask;
		public final Map<SpecialModelRenderer, SortTask> sortTasks = new Reference2ObjectArrayMap<>();

		private Box boundingBox;

		private boolean needsRebuild = true;
		private boolean needsImportantRebuild;

		private final BlockPos.Mutable origin = new BlockPos.Mutable(-1, -1, -1);

		private final BlockPos.Mutable[] neighbours = Util.make(new BlockPos.Mutable[6], pos -> {

			for (int i = 0; i < pos.length; ++i) {
				pos[i] = new BlockPos.Mutable();
			}

		});

		private final Map<SpecialModelRenderer, VertexBuffer> specialModelBuffers = SpecialModelRenderer.SPECIAL_MODEL_RENDERER
			.getEntries()
			.stream()
			.collect(Collectors.toMap(entry -> entry.getValue(), entry -> new VertexBuffer(VertexBuffer.Usage.STATIC)));

		public VertexBuffer getBuffer(SpecialModelRenderer modelRenderer) {
			return specialModelBuffers.get(modelRenderer);
		}

		public Map<SpecialModelRenderer, VertexBuffer> getSpecialModelBuffers() {
			return specialModelBuffers;
		}

		public BuiltChunk(int index, int x, int y, int z) {
			this.index = index;
			this.setOrigin(x, y, z);
		}

		private boolean isChunkNonEmpty(BlockPos pos) {
			return SpecialChunkBuilder.this.world
				.getChunk(ChunkSectionPos.getSectionCoord(pos.getX()), ChunkSectionPos.getSectionCoord(pos.getZ()),
					ChunkStatus.FULL, false) != null;
		}

		public boolean shouldBuild() {

			if (!(this.getSquaredCameraDistance() > 576.0)) {
				return true;
			} else {
				return this.isChunkNonEmpty(this.neighbours[Direction.WEST.ordinal()]) && this
					.isChunkNonEmpty(this.neighbours[Direction.NORTH.ordinal()]) && this
						.isChunkNonEmpty(this.neighbours[Direction.EAST.ordinal()]) && this
							.isChunkNonEmpty(this.neighbours[Direction.SOUTH.ordinal()]);
			}

		}

		public Box getBoundingBox() {
			return this.boundingBox;
		}

		public void setOrigin(int x, int y, int z) {
			this.clear();
			this.origin.set(x, y, z);
			this.boundingBox = new Box(x, y, z, x + 16, y + 16, z + 16);

			for (Direction direction : Direction.values()) {
				this.neighbours[direction.ordinal()].set(this.origin).move(direction, 16);
			}

		}

		protected double getSquaredCameraDistance() {
			Camera camera = client.gameRenderer.getCamera();
			double x = this.boundingBox.minX + 8.0 - camera.getPos().x;
			double y = this.boundingBox.minY + 8.0 - camera.getPos().y;
			double z = this.boundingBox.minZ + 8.0 - camera.getPos().z;
			return x * x + y * y + z * z;
		}

		void beginBufferBuilding(SpecialBufferBuilder buffer) {
			buffer.begin(DrawMode.QUADS, SpecialVertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE);
		}

		public ChunkData getData() {
			return this.data.get();
		}

		private void clear() {
			this.cancel();
			this.data.set(ChunkData.EMPTY);
			this.needsRebuild = true;
		}

		public void delete() {
			this.clear();
			this.specialModelBuffers.values().forEach(VertexBuffer::close);
		}

		public BlockPos getOrigin() {
			return this.origin;
		}

		public void scheduleRebuild(boolean important) {
			boolean neededRebuild = this.needsRebuild;
			this.needsRebuild = true;
			this.needsImportantRebuild = important | (neededRebuild && this.needsImportantRebuild);
		}

		public void cancelRebuild() {
			this.needsRebuild = false;
			this.needsImportantRebuild = false;
		}

		public boolean needsRebuild() {
			return this.needsRebuild;
		}

		public boolean needsImportantRebuild() {
			return this.needsRebuild && this.needsImportantRebuild;
		}

		public BlockPos getNeighborPosition(Direction direction) {
			return this.neighbours[direction.ordinal()];
		}

		public boolean scheduleSort(SpecialModelRenderer renderer, SpecialChunkBuilder chunkRenderer) {
			ChunkData data = this.getData();

			if (this.sortTasks.containsKey(renderer)) {
				this.sortTasks.get(renderer).cancel();
			}

			if (data.isEmpty(renderer)) {
				return false;
			} else {
				SortTask task = new SortTask(this.getSquaredCameraDistance(), data, renderer);
				this.sortTasks.put(renderer, task);
				chunkRenderer.send(task);
				return true;
			}

		}

		protected boolean cancel() {
			boolean cancelled = false;

			if (this.rebuildTask != null) {
				this.rebuildTask.cancel();
				this.rebuildTask = null;
				cancelled = true;
			}

			this.sortTasks.forEach((renderer, task) -> task.cancel());
			this.sortTasks.clear();

			return cancelled;
		}

		public Task createRebuildTask(ChunkRenderRegionCache cache) {
			boolean cancelled = this.cancel();

			BlockPos pos = this.origin.toImmutable();
			ChunkRenderRegion region = cache
				.createRenderRegion(SpecialChunkBuilder.this.world, pos.add(-1, -1, -1), pos.add(16, 16, 16), 1);

			boolean empty = this.data.get() == SpecialChunkBuilder.ChunkData.EMPTY;

			if (empty && cancelled) {
				this.cancelledInitialBuilds.incrementAndGet();
			}

			this.rebuildTask = new SpecialChunkBuilder.BuiltChunk.RebuildTask(this.getSquaredCameraDistance(), region,
				!empty || this.cancelledInitialBuilds.get() > 2);
			return this.rebuildTask;
		}

		public void scheduleRebuild(SpecialChunkBuilder builder, ChunkRenderRegionCache cache) {
			builder.send(this.createRebuildTask(cache));
		}

		public void rebuild(ChunkRenderRegionCache cache) {
			this.createRebuildTask(cache).run(SpecialChunkBuilder.this.buffers);
		}

		public class RebuildTask extends Task {

			@Nullable
			protected ChunkRenderRegion region;

			public RebuildTask(double distance, @Nullable ChunkRenderRegion region, boolean highPriority) {
				super(distance, highPriority);
				this.region = region;
			}

			@Override
			protected String name() {
				return "rend_chk_rebuild";
			}

			@Override
			public CompletableFuture<Result> run(SpecialBufferBuilderStorage buffers) {

				if (this.cancelled.get()) {
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else if (!BuiltChunk.this.shouldBuild()) {
					this.region = null;
					BuiltChunk.this.scheduleRebuild(false);
					this.cancelled.set(true);
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else if (this.cancelled.get()) {
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else {
					Vec3d cameraPos = SpecialChunkBuilder.this.getCameraPosition();
					float x = (float) cameraPos.x;
					float y = (float) cameraPos.y;
					float z = (float) cameraPos.z;
					RenderedChunkData renderedChunkData = this.render(x, y, z, buffers);

					if (this.cancelled.get()) {
						renderedChunkData.renderedBuffers.values().forEach(RenderedBuffer::release);
						return CompletableFuture.completedFuture(Result.CANCELLED);
					} else {
						ChunkData chunkData = new ChunkData();

						chunkData.occlusionGraph = renderedChunkData.occlusionGraph;

						chunkData.bufferStates.clear();
						chunkData.bufferStates.putAll(renderedChunkData.bufferStates);

						List<CompletableFuture<Void>> results = Lists.newArrayList();
						renderedChunkData.renderedBuffers.forEach((modelRenderer, renderedBuffer) -> {

							results
								.add(SpecialChunkBuilder.this
									.scheduleUpload(modelRenderer, renderedBuffer,
										BuiltChunk.this.getBuffer(modelRenderer)));

							if (!renderedBuffer.isEmpty()) {
								chunkData.renderedBuffers.put(modelRenderer, renderedBuffer);
							} else {
								chunkData.renderedBuffers.remove(modelRenderer);
							}

						});
						return Util.combine(results).handle((listx, throwable) -> {

							if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
								MinecraftClient
									.getInstance()
									.setCrashReport(CrashReport.create(throwable, "Rendering chunk"));
							}

							if (this.cancelled.get()) {
								return Result.CANCELLED;
							} else {
								BuiltChunk.this.data.set(chunkData);
								BuiltChunk.this.cancelledInitialBuilds.set(0);

								((WorldChunkBuilderAccess) (SpecialChunkBuilder.this.worldRenderer))
									.addSpecialBuiltChunk(BuiltChunk.this);

								return Result.SUCCESSFUL;
							}

						});
					}

				}

			}

			private RenderedChunkData render(float cameraX, float cameraY, float cameraZ,
					SpecialBufferBuilderStorage buffers) {
				RenderedChunkData renderedChunkData = new RenderedChunkData();

				BlockPos originPos = BuiltChunk.this.origin.toImmutable();
				BlockPos boundingPos = originPos.add(15, 15, 15);

				ChunkOcclusionDataBuilder chunkOcclusionDataBuilder = new ChunkOcclusionDataBuilder();
				ChunkRenderRegion chunkRenderRegion = this.region;
				this.region = null;

				MatrixStack matrixStack = new MatrixStack();

				if (chunkRenderRegion != null) {
					BlockModelRenderer.enableBrightnessCache();
					RandomGenerator randomGenrator = RandomGenerator.createLegacy();
					BlockRenderManager blockRenderManager = MinecraftClient.getInstance().getBlockRenderManager();

					for (BlockPos pos : BlockPos.iterate(originPos, boundingPos)) {
						BlockState state = chunkRenderRegion.getBlockState(pos);

						if (state.isOpaqueFullCube(chunkRenderRegion, pos)) {
							chunkOcclusionDataBuilder.markClosed(pos);
						}

						if (state.getRenderType() != BlockRenderType.INVISIBLE) {
							matrixStack.push();
							matrixStack
								.translate((float) (pos.getX() & 15), (float) (pos.getY() & 15), (float) (pos.getZ() & 15));
							List<Pair<SpecialModelRenderer, BakedModel>> models = ((BakedModelAccess) WrapperBakedModel
								.unwrap(blockRenderManager.getModel(state))).getModels(state);

							if (!models.isEmpty()) {

								for (Pair<SpecialModelRenderer, BakedModel> pair : models) {
									SpecialModelRenderer modelRenderer = pair.getFirst();
									BakedModel model = pair.getSecond();
									long modelSeed = state.getRenderingSeed(pos);
									SpecialBufferBuilder buffer = buffers.get(modelRenderer);
									buffer
										.setState(() -> modelRenderer
											.appendState(chunkRenderRegion, pos, state, model, modelSeed));

									if (!buffer.isBuilding()) {
										buffer
											.begin(VertexFormat.DrawMode.QUADS,
												SpecialVertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE);
									}

									ReconstructableModel constructedModel = new ReconstructableModel(model);
									constructedModel
										.setFunction((quads, blockState, direction, random) -> quads
											.stream()
											.map((quad) -> reconstructBakedQuad(chunkRenderRegion, pos, state, model,
												modelSeed, quad, modelRenderer))
											.toList());
									blockRenderManager
										.getModelRenderer()
										.render(chunkRenderRegion, constructedModel, state, pos, matrixStack, buffer, true,
											randomGenrator, modelSeed, OverlayTexture.DEFAULT_UV);
								}

							}

							matrixStack.pop();
						}

					}

					for (SpecialModelRenderer modelRenderer : buffers.getSpecialModelBuffers().keySet()) {
						SpecialBufferBuilder bufferBuilder = buffers.get(modelRenderer);

						if (!bufferBuilder.isCurrentBatchEmpty()) {
							bufferBuilder
								.setQuadSorting(VertexSorting
									.byDistanceSquared(cameraX - originPos.getX(), cameraY - originPos.getY(),
										cameraZ - originPos.getZ()));
							renderedChunkData.bufferStates.put(modelRenderer, bufferBuilder.popState());
						}

						if (!bufferBuilder.isBuilding()) {
							bufferBuilder
								.begin(VertexFormat.DrawMode.QUADS,
									SpecialVertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE);
						}

						RenderedBuffer renderedBuffer = bufferBuilder.end();

						if (renderedBuffer != null) {
							renderedChunkData.renderedBuffers.put(modelRenderer, renderedBuffer);
						}

					}

					BlockModelRenderer.disableBrightnessCache();
				}

				renderedChunkData.occlusionGraph = chunkOcclusionDataBuilder.build();
				return renderedChunkData;
			}

			private BakedQuad reconstructBakedQuad(ChunkRenderRegion region, BlockPos pos, BlockState state,
					BakedModel model, long modelSeed, BakedQuad quad, SpecialModelRenderer modelRenderer) {
				int[] vertexData = quad.getVertexData();
				int vertexDataLength = 8;

				try (MemoryStack memoryStack = MemoryStack.stackPush()) {
					ByteBuffer byteBuffer = memoryStack
						.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSize());
					IntBuffer intBuffer = byteBuffer.asIntBuffer();
					int[] reconstructed = new int[vertexData.length];
					int uvIndex = 0;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					float x1 = byteBuffer.getFloat(0);
					float y1 = byteBuffer.getFloat(4);
					float z1 = byteBuffer.getFloat(8);
					float u1 = byteBuffer.getFloat(16);
					float v1 = byteBuffer.getFloat(20);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					float x2 = byteBuffer.getFloat(0);
					float y2 = byteBuffer.getFloat(4);
					float z2 = byteBuffer.getFloat(8);
					float u2 = byteBuffer.getFloat(16);
					float v2 = byteBuffer.getFloat(20);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					float x3 = byteBuffer.getFloat(0);
					float y3 = byteBuffer.getFloat(4);
					float z3 = byteBuffer.getFloat(8);
					float u3 = byteBuffer.getFloat(16);
					float v3 = byteBuffer.getFloat(20);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					float x4 = byteBuffer.getFloat(0);
					float y4 = byteBuffer.getFloat(4);
					float z4 = byteBuffer.getFloat(8);
					float u4 = byteBuffer.getFloat(16);
					float v4 = byteBuffer.getFloat(20);
					MutableQuad mutableQuad = modelRenderer
						.modifyQuad(region, pos, state, model, quad, modelSeed,
							new MutableQuad(new MutableVertice(x1, y1, z1, u1, v1), new MutableVertice(x2, y2, z2, u2, v2),
								new MutableVertice(x3, y3, z3, u3, v3), new MutableVertice(x4, y4, z4, u4, v4)));
					uvIndex = 0;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					byteBuffer.putFloat(0, (float) mutableQuad.getV1().getPos().x);
					byteBuffer.putFloat(4, (float) mutableQuad.getV1().getPos().y);
					byteBuffer.putFloat(8, (float) mutableQuad.getV1().getPos().z);
					byteBuffer.putFloat(16, mutableQuad.getV1().getUv().x);
					byteBuffer.putFloat(20, mutableQuad.getV1().getUv().y);
					intBuffer.position(0);
					intBuffer.get(reconstructed, uvIndex, vertexDataLength);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					byteBuffer.putFloat(0, (float) mutableQuad.getV2().getPos().x);
					byteBuffer.putFloat(4, (float) mutableQuad.getV2().getPos().y);
					byteBuffer.putFloat(8, (float) mutableQuad.getV2().getPos().z);
					byteBuffer.putFloat(16, mutableQuad.getV2().getUv().x);
					byteBuffer.putFloat(20, mutableQuad.getV2().getUv().y);
					intBuffer.position(0);
					intBuffer.get(reconstructed, uvIndex, vertexDataLength);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					byteBuffer.putFloat(0, (float) mutableQuad.getV3().getPos().x);
					byteBuffer.putFloat(4, (float) mutableQuad.getV3().getPos().y);
					byteBuffer.putFloat(8, (float) mutableQuad.getV3().getPos().z);
					byteBuffer.putFloat(16, mutableQuad.getV3().getUv().x);
					byteBuffer.putFloat(20, mutableQuad.getV3().getUv().y);
					intBuffer.position(0);
					intBuffer.get(reconstructed, uvIndex, vertexDataLength);
					uvIndex += vertexDataLength;
					intBuffer.clear();
					intBuffer.put(vertexData, uvIndex, vertexDataLength);
					byteBuffer.putFloat(0, (float) mutableQuad.getV4().getPos().x);
					byteBuffer.putFloat(4, (float) mutableQuad.getV4().getPos().y);
					byteBuffer.putFloat(8, (float) mutableQuad.getV4().getPos().z);
					byteBuffer.putFloat(16, mutableQuad.getV4().getUv().x);
					byteBuffer.putFloat(20, mutableQuad.getV4().getUv().y);
					intBuffer.position(0);
					intBuffer.get(reconstructed, uvIndex, vertexDataLength);
					return new BakedQuad(reconstructed, quad.getColorIndex(), quad.getFace(), quad.getSprite(),
						quad.hasShade());
				}

			}

			@Override
			public void cancel() {
				this.region = null;

				if (this.cancelled.compareAndSet(false, true)) {
					BuiltChunk.this.scheduleRebuild(false);
				}

			}

			public static final class RenderedChunkData {

				public final Map<SpecialModelRenderer, RenderedBuffer> renderedBuffers = new Reference2ObjectArrayMap<>();
				public final Map<SpecialModelRenderer, SortState> bufferStates = new Reference2ObjectArrayMap<>();
				public ChunkOcclusionData occlusionGraph = new ChunkOcclusionData();

			}

			public static final class ReconstructableModel extends ForwardingBakedModel {

				private QuadFunction<List<BakedQuad>, BlockState, Direction, RandomGenerator, List<BakedQuad>> function;

				public ReconstructableModel(BakedModel model) {
					this.wrapped = model;
				}

				public void setFunction(
						QuadFunction<List<BakedQuad>, BlockState, Direction, RandomGenerator, List<BakedQuad>> function) {
					this.function = function;
				}

				@Override
				public List<BakedQuad> getQuads(BlockState blockState, Direction face, RandomGenerator rand) {
					return function.apply(super.getQuads(blockState, face, rand), blockState, face, rand);
				}

			}

		}

		public class SortTask extends Task {

			private final ChunkData data;
			private final SpecialModelRenderer renderer;

			public SortTask(double distance, ChunkData data, SpecialModelRenderer renderer) {
				super(distance, true);
				this.data = data;
				this.renderer = renderer;
			}

			@Override
			protected String name() {
				return "rend_chk_sort";
			}

			@Override
			public CompletableFuture<Result> run(SpecialBufferBuilderStorage buffers) {

				if (this.cancelled.get()) {
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else if (!BuiltChunk.this.shouldBuild()) {
					this.cancelled.set(true);
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else if (this.cancelled.get()) {
					return CompletableFuture.completedFuture(Result.CANCELLED);
				} else {
					Vec3d cameraPos = SpecialChunkBuilder.this.getCameraPosition();
					float x = (float) cameraPos.x;
					float y = (float) cameraPos.y;
					float z = (float) cameraPos.z;

					if (this.data.bufferStates.containsKey(renderer) && !this.data.isEmpty(renderer)) {
						SortState sortState = this.data.bufferStates.get(renderer);
						SpecialBufferBuilder bufferBuilder = buffers.get(renderer);

						BuiltChunk.this.beginBufferBuilding(bufferBuilder);
						bufferBuilder.restoreState(sortState);

						bufferBuilder
							.setQuadSorting(VertexSorting
								.byDistanceSquared(x - (float) BuiltChunk.this.origin.getX(),
									y - (float) BuiltChunk.this.origin.getY(), z - (float) BuiltChunk.this.origin.getZ()));

						this.data.bufferStates.put(renderer, bufferBuilder.popState());

						RenderedBuffer renderedBuffer = bufferBuilder.end();

						if (this.cancelled.get()) {
							renderedBuffer.release();
							return CompletableFuture.completedFuture(Result.CANCELLED);
						} else {
							CompletableFuture<Result> completableFuture = SpecialChunkBuilder.this
								.scheduleUpload(renderer, renderedBuffer, BuiltChunk.this.getBuffer(renderer))
								.thenApply(v -> Result.CANCELLED);
							return completableFuture.handle((result, throwable) -> {

								if (throwable != null && !(throwable instanceof CancellationException) && !(throwable instanceof InterruptedException)) {
									MinecraftClient
										.getInstance()
										.setCrashReport(CrashReport.create(throwable, "Rendering chunk"));
								}

								return this.cancelled.get() ? Result.CANCELLED : Result.SUCCESSFUL;
							});
						}

					} else {
						return CompletableFuture.completedFuture(Result.CANCELLED);
					}

				}

			}

			@Override
			public void cancel() {
				this.cancelled.set(true);
			}

		}

		abstract class Task implements Comparable<Task> {

			protected final double distance;
			protected final AtomicBoolean cancelled = new AtomicBoolean(false);
			protected final boolean highPriority;

			public Task(double distance, boolean highPriority) {
				this.distance = distance;
				this.highPriority = highPriority;
			}

			public abstract CompletableFuture<Result> run(SpecialBufferBuilderStorage buffers);

			public abstract void cancel();

			protected abstract String name();

			public int compareTo(Task task) {
				return Doubles.compare(this.distance, task.distance);
			}

		}

	}

	public static class ChunkData {

		public static final SpecialChunkBuilder.ChunkData EMPTY = new SpecialChunkBuilder.ChunkData() {

			@Override
			public boolean isVisibleThrough(Direction from, Direction to) {
				return false;
			}

		};

		public final Map<SpecialModelRenderer, RenderedBuffer> renderedBuffers = new Reference2ObjectArrayMap<>();
		public final Map<SpecialModelRenderer, SortState> bufferStates = new Reference2ObjectArrayMap<>();

		public ChunkOcclusionData occlusionGraph = new ChunkOcclusionData();

		public boolean isEmpty() {
			return this.renderedBuffers.isEmpty();
		}

		public boolean isEmpty(SpecialModelRenderer layer) {
			return !this.renderedBuffers
				.containsKey(
					layer) || (this.renderedBuffers.containsKey(layer) && this.renderedBuffers.get(layer).isEmpty());
		}

		public boolean isVisibleThrough(Direction from, Direction to) {
			return this.occlusionGraph.isVisibleThrough(from, to);
		}

	}

	public static enum Result {
		SUCCESSFUL,
		CANCELLED;
	}

	public static class ChunkInfo {

		public final BuiltChunk chunk;
		private byte direction;
		public byte cullingState;
		public final int propagationLevel;

		public ChunkInfo(BuiltChunk chunk, @Nullable Direction direction, int propagationLevel) {
			this.chunk = chunk;

			if (direction != null) {
				this.addDirection(direction);
			}

			this.propagationLevel = propagationLevel;
		}

		public void updateCullingState(byte parentCullingState, Direction from) {
			this.cullingState = (byte) (this.cullingState | parentCullingState | 1 << from.ordinal());
		}

		public boolean canCull(Direction from) {
			return (this.cullingState & 1 << from.ordinal()) > 0;
		}

		public void addDirection(Direction direction) {
			this.direction = (byte) (this.direction | this.direction | 1 << direction.ordinal());
		}

		public boolean hasDirection(int ordinal) {
			return (this.direction & 1 << ordinal) > 0;
		}

		public boolean hasAnyDirection() {
			return this.direction != 0;
		}

		public boolean isAxisAlignedWith(int i, int j, int k) {
			BlockPos blockPos = this.chunk.getOrigin();
			return i == blockPos.getX() / 16 || k == blockPos.getZ() / 16 || j == blockPos.getY() / 16;
		}

		public int hashCode() {
			return this.chunk.getOrigin().hashCode();
		}

		public boolean equals(Object object) {

			if (!(object instanceof ChunkInfo)) {
				return false;
			} else {
				ChunkInfo chunkInfo = (ChunkInfo) object;
				return this.chunk.getOrigin().equals(chunkInfo.chunk.getOrigin());
			}

		}

	}

	public static class ChunkInfoListMap {

		private final ChunkInfo[] current;

		ChunkInfoListMap(int size) {
			this.current = new ChunkInfo[size];
		}

		public void setInfo(BuiltChunk chunk, ChunkInfo info) {
			this.current[chunk.index] = info;
		}

		@Nullable
		public ChunkInfo getInfo(BuiltChunk chunk) {
			int i = chunk.index;
			return i >= 0 && i < this.current.length ? this.current[i] : null;
		}

	}

	public static class RenderableChunks {

		public final ChunkInfoListMap builtChunkMap;
		public final LinkedHashSet<ChunkInfo> builtChunks;

		public RenderableChunks(int size) {
			this.builtChunkMap = new ChunkInfoListMap(size);
			this.builtChunks = new LinkedHashSet<ChunkInfo>(size);
		}

	}

}
