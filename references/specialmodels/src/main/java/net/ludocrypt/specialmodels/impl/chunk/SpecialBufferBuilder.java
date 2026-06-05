package net.ludocrypt.specialmodels.impl.chunk;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

import com.mojang.blaze3d.AllocationUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.FixedColorVertexConsumer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormats;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.ints.IntConsumer;
import net.ludocrypt.specialmodels.impl.mixin.render.UsageAccessor;
import net.ludocrypt.specialmodels.impl.mixin.render.VertexBufferAccessor;
import net.ludocrypt.specialmodels.impl.render.SpecialVertexFormats;
import net.ludocrypt.specialmodels.impl.render.Vec4b;
import net.minecraft.util.math.MathHelper;

public class SpecialBufferBuilder extends FixedColorVertexConsumer implements BufferVertexConsumer {

	private static final Logger LOGGER = LogUtils.getLogger();

	private ByteBuffer buffer;

	private int renderedBufferCount;
	private int renderedBufferPointer;

	private int elementOffset;

	private int vertexCount;

	@Nullable
	private VertexFormatElement currentElement;

	private int elementIndex;

	private VertexFormat format;
	private DrawMode drawMode;

	private boolean textured;
	private boolean hasOverlay;
	private boolean building;

	@Nullable
	private Vector3f[] sortingPoints;

	@Nullable
	private VertexSorting quadSorting;

	private boolean indexOnly;

	@Nullable
	private Supplier<Vec4b> state;

	public SpecialBufferBuilder(int initialCapacity) {
		this.buffer = AllocationUtil.allocateByteBuffer(initialCapacity * 6);
	}

	private void grow() {
		this.grow(this.format.getVertexSize());
	}

	private void grow(int size) {

		if (this.elementOffset + size > this.buffer.capacity()) {
			int oldSize = this.buffer.capacity();
			int newSize = oldSize + roundBufferSize(size);
			LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", oldSize, newSize);
			ByteBuffer byteBuffer = AllocationUtil.resizeByteBuffer(this.buffer, newSize);
			byteBuffer.rewind();
			this.buffer = byteBuffer;
		}

	}

	private static int roundBufferSize(int amount) {
		int size = 2097152;

		if (amount == 0) {
			return size;
		} else {

			if (amount < 0) {
				size *= -1;
			}

			int j = amount % size;
			return j == 0 ? amount : amount + size - j;
		}

	}

	public void setQuadSorting(VertexSorting sorting) {

		if (this.drawMode == VertexFormat.DrawMode.QUADS) {
			this.quadSorting = sorting;

			if (this.sortingPoints == null) {
				this.sortingPoints = this.createSortingPoints();
			}

		}

	}

	public SortState popState() {
		return new SpecialBufferBuilder.SortState(this.drawMode, this.vertexCount, this.sortingPoints, this.quadSorting);
	}

	public void restoreState(SortState state) {
		this.buffer.rewind();
		this.drawMode = state.drawMode;
		this.vertexCount = state.vertexCount;
		this.elementOffset = this.renderedBufferPointer;
		this.sortingPoints = state.sortingPoints;
		this.quadSorting = state.quadSorting;
		this.indexOnly = true;
	}

	public void begin(DrawMode drawMode, VertexFormat format) {

		if (this.building) {
			throw new IllegalStateException("Already building!");
		} else {
			this.building = true;
			this.drawMode = drawMode;
			this.setFormat(format);
			this.currentElement = format.getElements().get(0);
			this.elementIndex = 0;
			this.buffer.rewind();
		}

	}

	private void setFormat(VertexFormat format) {

		if (this.format != format) {
			this.format = format;
			boolean hasTextureAndOverlay = format == VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL;
			boolean hasTexture = format == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL;
			boolean hasTextureAndState = format == SpecialVertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE;
			this.textured = hasTextureAndOverlay || hasTexture || hasTextureAndState;
			this.hasOverlay = hasTextureAndOverlay;
		}

	}

	private IntConsumer createConsumer(int i, IndexType indexType) {
		MutableInt mutableInt = new MutableInt(i);

		return switch (indexType) {
			case SHORT -> value -> this.buffer.putShort(mutableInt.getAndAdd(2), (short) value);
			case INT -> value -> this.buffer.putInt(mutableInt.getAndAdd(4), value);
		};
	}

	private Vector3f[] createSortingPoints() {
		FloatBuffer floatBuffer = this.buffer.asFloatBuffer();
		int pointer = this.renderedBufferPointer / 4;
		int formatSize = this.format.getIntegerSize();
		int drawSize = formatSize * this.drawMode.primitiveStride;
		int expected = this.vertexCount / this.drawMode.primitiveStride;
		Vector3f[] sort = new Vector3f[expected];

		for (int i = 0; i < expected; ++i) {
			float x1 = floatBuffer.get(pointer + i * drawSize + 0);
			float y1 = floatBuffer.get(pointer + i * drawSize + 1);
			float z1 = floatBuffer.get(pointer + i * drawSize + 2);
			float x2 = floatBuffer.get(pointer + i * drawSize + formatSize * 2 + 0);
			float y2 = floatBuffer.get(pointer + i * drawSize + formatSize * 2 + 1);
			float z2 = floatBuffer.get(pointer + i * drawSize + formatSize * 2 + 2);
			float x = (x1 + x2) / 2.0F;
			float y = (y1 + y2) / 2.0F;
			float z = (z1 + z2) / 2.0F;
			sort[i] = new Vector3f(x, y, z);
		}

		return sort;
	}

	private void putSortedIndices(IndexType indexType) {

		if (this.sortingPoints != null && this.quadSorting != null) {
			int[] sorted = this.quadSorting.sort(this.sortingPoints);
			IntConsumer consumer = this.createConsumer(this.elementOffset, indexType);

			for (int i : sorted) {
				consumer.accept(i * this.drawMode.primitiveStride + 0);
				consumer.accept(i * this.drawMode.primitiveStride + 1);
				consumer.accept(i * this.drawMode.primitiveStride + 2);
				consumer.accept(i * this.drawMode.primitiveStride + 2);
				consumer.accept(i * this.drawMode.primitiveStride + 3);
				consumer.accept(i * this.drawMode.primitiveStride + 0);
			}

		} else {
			throw new IllegalStateException("Sorting state uninitialized");
		}

	}

	public boolean isCurrentBatchEmpty() {
		return this.vertexCount == 0;
	}

	@Nullable
	public RenderedBuffer endOrDiscard() {
		this.ensureDrawing();

		if (this.isCurrentBatchEmpty()) {
			this.reset();
			return null;
		} else {
			RenderedBuffer buffer = this.buildBatchParameters();
			this.reset();
			return buffer;
		}

	}

	public RenderedBuffer end() {
		this.ensureDrawing();
		RenderedBuffer buffer = this.buildBatchParameters();
		this.reset();
		return buffer;
	}

	private void ensureDrawing() {

		if (!this.building) {
			throw new IllegalStateException("Not building!");
		}

	}

	private RenderedBuffer buildBatchParameters() {
		int drawCount = this.drawMode.indexCount(this.vertexCount);
		int vertexSize = !this.indexOnly ? this.vertexCount * this.format.getVertexSize() : 0;
		IndexType type = IndexType.getSmallestIndexType(drawCount);
		boolean textured = true;
		int offset = vertexSize;

		if (this.sortingPoints != null) {
			int growth = MathHelper.roundUpToMultiple(drawCount * type.bytes, 4);
			this.grow(growth);
			this.putSortedIndices(type);
			this.elementOffset += growth;
			offset = vertexSize + growth;
			textured = false;
		}

		int pointer = this.renderedBufferPointer;

		this.renderedBufferPointer += offset;
		this.renderedBufferCount++;

		DrawArrayParameters params = new DrawArrayParameters(this.format, this.vertexCount, drawCount, this.drawMode, type,
			this.indexOnly, textured);

		return new RenderedBuffer(pointer, params);
	}

	private void reset() {
		this.building = false;
		this.vertexCount = 0;
		this.currentElement = null;
		this.elementIndex = 0;
		this.sortingPoints = null;
		this.quadSorting = null;
		this.indexOnly = false;
	}

	@Override
	public void putByte(int index, byte value) {
		this.buffer.put(this.elementOffset + index, value);
	}

	@Override
	public void putShort(int index, short value) {
		this.buffer.putShort(this.elementOffset + index, value);
	}

	@Override
	public void putFloat(int index, float value) {
		this.buffer.putFloat(this.elementOffset + index, value);
	}

	@Override
	public void next() {

		if (this.elementIndex != 0) {
			throw new IllegalStateException("Not filled all elements of the vertex");
		} else {
			this.vertexCount++;
			this.grow();

			if (this.drawMode == VertexFormat.DrawMode.LINES || this.drawMode == VertexFormat.DrawMode.LINE_STRIP) {
				int size = this.format.getVertexSize();
				this.buffer.put(this.elementOffset, this.buffer, this.elementOffset - size, size);
				this.elementOffset += size;
				this.vertexCount++;
				this.grow();
			}

		}

	}

	@Override
	public void nextElement() {
		List<VertexFormatElement> elements = this.format.getElements();

		this.elementIndex = (this.elementIndex + 1) % elements.size();
		this.elementOffset += this.currentElement.getByteLength();

		VertexFormatElement element = elements.get(this.elementIndex);
		this.currentElement = element;

		if (element.getType() == VertexFormatElement.Type.PADDING) {
			this.nextElement();
		}

		if (this.colorFixed && this.currentElement.getType() == VertexFormatElement.Type.COLOR) {
			BufferVertexConsumer.super.color(this.fixedRed, this.fixedGreen, this.fixedBlue, this.fixedAlpha);
		}

	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {

		if (this.colorFixed) {
			throw new IllegalStateException();
		} else {
			return BufferVertexConsumer.super.color(red, green, blue, alpha);
		}

	}

	@Override
	public void vertex(float x, float y, float z, float red, float green, float blue, float alpha, float u, float v,
			int overlay, int light, float normalX, float normalY, float normalZ) {

		if (this.colorFixed) {
			throw new IllegalStateException();
		} else if (this.textured) {
			this.putFloat(0, x);
			this.putFloat(4, y);
			this.putFloat(8, z);
			this.putByte(12, (byte) (red * 255.0F));
			this.putByte(13, (byte) (green * 255.0F));
			this.putByte(14, (byte) (blue * 255.0F));
			this.putByte(15, (byte) (alpha * 255.0F));
			this.putFloat(16, u);
			this.putFloat(20, v);
			int o = 24;

			if (this.hasOverlay) {
				this.putShort(24, (short) (overlay & 65535));
				this.putShort(26, (short) (overlay >> 16 & 65535));
				o += 4;
			}

			this.putShort(o + 0, (short) (light & 65535));
			this.putShort(o + 2, (short) (light >> 16 & 65535));
			this.putByte(o + 4, BufferVertexConsumer.packByte(normalX));
			this.putByte(o + 5, BufferVertexConsumer.packByte(normalY));
			this.putByte(o + 6, BufferVertexConsumer.packByte(normalZ));

			if (format == SpecialVertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE) {
				Vec4b state = this.state.get();
				this.putByte(o + 7, state.getX());
				this.putByte(o + 8, state.getY());
				this.putByte(o + 9, state.getZ());
				this.putByte(o + 10, state.getW());
				o += 4;
			}

			this.elementOffset += o + 8;
			this.next();
		} else {
			super.vertex(x, y, z, red, green, blue, alpha, u, v, overlay, light, normalX, normalY, normalZ);
		}

	}

	private void popBatch() {

		if (this.renderedBufferCount > 0 && --this.renderedBufferCount == 0) {
			this.clear();
		}

	}

	public void clear() {

		if (this.renderedBufferCount > 0) {
			LOGGER.warn("Clearing BufferBuilder with unused batches");
		}

		this.discard();
	}

	public void discard() {
		this.renderedBufferCount = 0;
		this.renderedBufferPointer = 0;
		this.elementOffset = 0;
	}

	@Override
	public VertexFormatElement getCurrentElement() {

		if (this.currentElement == null) {
			throw new IllegalStateException("BufferBuilder not started");
		} else {
			return this.currentElement;
		}

	}

	public boolean isBuilding() {
		return this.building;
	}

	ByteBuffer getBuffer(int start, int end) {
		return MemoryUtil.memSlice(this.buffer, start, end - start);
	}

	public Supplier<Vec4b> getState() {
		return state;
	}

	public void setState(Supplier<Vec4b> state) {
		this.state = state;
	}

	public static record DrawArrayParameters(VertexFormat vertexFormat, int vertexCount, int indexCount, DrawMode mode,
			IndexType indexType, boolean indexOnly, boolean textured) {

		public int getVertexBufferSize() {
			return this.vertexCount * this.vertexFormat.getVertexSize();
		}

		public int getVertexBufferEnd() {
			return this.getVertexBufferSize();
		}

		public int getIndexBufferStart() {
			return this.indexOnly ? 0 : this.getVertexBufferEnd();
		}

		public int getIndexBufferEnd() {
			return this.getIndexBufferStart() + this.getIndexBufferSize();
		}

		private int getIndexBufferSize() {
			return this.textured ? 0 : this.indexCount * this.indexType.bytes;
		}

		public int getTotalBufferSize() {
			return this.getIndexBufferEnd();
		}

		public VertexFormat getVertexFormat() {
			return this.vertexFormat;
		}

		public int getVertexCount() {
			return this.vertexCount;
		}

		public int getIndexCount() {
			return this.indexCount;
		}

		public DrawMode getMode() {
			return this.mode;
		}

		public IndexType getIndexType() {
			return this.indexType;
		}

		public boolean getIndexOnly() {
			return this.indexOnly;
		}

		public boolean isTextured() {
			return this.textured;
		}

	}

	public class RenderedBuffer {

		private final int pointer;
		private final DrawArrayParameters parameters;
		private boolean released;

		RenderedBuffer(int pointer, DrawArrayParameters parameters) {
			this.pointer = pointer;
			this.parameters = parameters;
		}

		public ByteBuffer getVertexBuffer() {
			int start = this.pointer;
			int end = this.pointer + this.parameters.getVertexBufferEnd();
			return SpecialBufferBuilder.this.getBuffer(start, end);
		}

		public ByteBuffer getIndexBuffer() {
			int start = this.pointer + this.parameters.getIndexBufferStart();
			int end = this.pointer + this.parameters.getIndexBufferEnd();
			return SpecialBufferBuilder.this.getBuffer(start, end);
		}

		public DrawArrayParameters getParameters() {
			return this.parameters;
		}

		public boolean isEmpty() {
			return this.parameters.vertexCount == 0;
		}

		public void release() {

			if (this.released) {
				throw new IllegalStateException("Buffer has already been released!");
			} else {
				SpecialBufferBuilder.this.popBatch();
				this.released = true;
			}

		}

		public void upload(VertexBuffer buffer) {

			if (!buffer.invalid()) {
				RenderSystem.assertOnRenderThread();

				try {
					DrawArrayParameters params = this.getParameters();
					((VertexBufferAccessor) buffer)
						.setVertexFormat(this.uploadAndBindFormat(buffer, params, this.getVertexBuffer()));
					((VertexBufferAccessor) buffer)
						.setIndexBuffer(this.uploadIndexBuffer(buffer, params, this.getIndexBuffer()));
					((VertexBufferAccessor) buffer).setIndexCount(params.getIndexCount());
					((VertexBufferAccessor) buffer).setIndexType(params.getIndexType());
					((VertexBufferAccessor) buffer).setDrawMode(params.getMode());
				} finally {
					this.release();
				}

			}

		}

		private VertexFormat uploadAndBindFormat(VertexBuffer buffer, DrawArrayParameters parameters, ByteBuffer bytes) {
			boolean rebind = false;

			if (!parameters.getVertexFormat().equals(((VertexBufferAccessor) buffer).getVertexFormat())) {

				if (((VertexBufferAccessor) buffer).getVertexFormat() != null) {
					((VertexBufferAccessor) buffer).getVertexFormat().clearAttribState();
				}

				GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((VertexBufferAccessor) buffer).getVertexBufferId());
				parameters.getVertexFormat().setupAttribState();
				rebind = true;
			}

			if (!parameters.getIndexOnly()) {

				if (!rebind) {
					GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, ((VertexBufferAccessor) buffer).getVertexBufferId());
				}

				RenderSystem
					.glBufferData(34962, bytes,
						((UsageAccessor) (Object) ((VertexBufferAccessor) buffer).getUsage()).getId());
			}

			return parameters.getVertexFormat();
		}

		@Nullable
		private RenderSystem.IndexBuffer uploadIndexBuffer(VertexBuffer buffer, DrawArrayParameters parameters,
				ByteBuffer bytes) {

			if (!parameters.isTextured()) {
				GlStateManager
					._glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ((VertexBufferAccessor) buffer).getIndexBufferId());
				RenderSystem
					.glBufferData(34963, bytes,
						((UsageAccessor) (Object) ((VertexBufferAccessor) buffer).getUsage()).getId());
				return null;
			} else {
				RenderSystem.IndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(parameters.getMode());

				if (indexBuffer != ((VertexBufferAccessor) buffer).getIndexBuffer() || !indexBuffer
					.hasSize(parameters.getIndexCount())) {
					indexBuffer.bindWithSize(parameters.getIndexCount());
				}

				return indexBuffer;
			}

		}

	}

	public static record SortState(DrawMode drawMode, int vertexCount, @Nullable Vector3f[] sortingPoints,
			@Nullable VertexSorting quadSorting) {

	}

}
