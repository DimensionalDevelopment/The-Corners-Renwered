package net.ludocrypt.specialmodels.impl.chunk;

import java.util.Map;
import java.util.stream.Collectors;

import net.ludocrypt.specialmodels.api.SpecialModelRenderer;
import net.minecraft.client.render.RenderLayer;

public class SpecialBufferBuilderStorage {

	private final Map<SpecialModelRenderer, SpecialBufferBuilder> specialModelBuffers = SpecialModelRenderer.SPECIAL_MODEL_RENDERER
		.getEntries()
		.stream()
		.collect(Collectors
			.toMap(entry -> entry.getValue(),
				entry -> new SpecialBufferBuilder(RenderLayer.getSolid().getExpectedBufferSize())));

	public SpecialBufferBuilder get(SpecialModelRenderer renderer) {
		return this.specialModelBuffers.get(renderer);
	}

	public void clear() {
		this.specialModelBuffers.values().forEach(SpecialBufferBuilder::clear);
	}

	public void reset() {
		this.specialModelBuffers.values().forEach(SpecialBufferBuilder::discard);
	}

	public Map<SpecialModelRenderer, SpecialBufferBuilder> getSpecialModelBuffers() {
		return specialModelBuffers;
	}

}
