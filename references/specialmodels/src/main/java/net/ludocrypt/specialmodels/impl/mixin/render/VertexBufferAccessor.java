package net.ludocrypt.specialmodels.impl.mixin.render;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.blaze3d.systems.RenderSystem.IndexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexBuffer.Usage;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode;
import com.mojang.blaze3d.vertex.VertexFormat.IndexType;

@Mixin(VertexBuffer.class)
public interface VertexBufferAccessor {

	@Accessor
	int getIndexCount();

	@Accessor
	void setIndexCount(int indexCount);

	@Accessor
	DrawMode getDrawMode();

	@Accessor
	void setDrawMode(DrawMode drawMode);

	@Accessor
	Usage getUsage();

	@Mutable
	@Accessor
	void setUsage(Usage usage);

	@Accessor
	int getVertexBufferId();

	@Accessor
	void setVertexBufferId(int vertexBufferId);

	@Accessor
	int getIndexBufferId();

	@Accessor
	void setIndexBufferId(int indexBufferId);

	@Accessor
	VertexFormat getVertexFormat();

	@Accessor
	void setVertexFormat(VertexFormat vertexFormat);

	@Accessor
	IndexBuffer getIndexBuffer();

	@Accessor
	void setIndexBuffer(IndexBuffer indexBuffer);

	@Accessor
	IndexType getIndexType();

	@Accessor
	void setIndexType(IndexType indexType);

}
