package net.ludocrypt.specialmodels.impl.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexFormats;

public class SpecialVertexFormats {

	public static final VertexFormatElement STATE_ELEMENT = new VertexFormatElement(0, VertexFormatElement.DataType.BYTE,
		VertexFormatElement.Type.GENERIC, 4);
	public static final VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL_STATE = new VertexFormat(ImmutableMap
		.of("Position", VertexFormats.POSITION_ELEMENT, "Color", VertexFormats.COLOR_ELEMENT, "UV0",
			VertexFormats.TEXTURE_0_ELEMENT, "UV2", VertexFormats.LIGHT_ELEMENT, "Normal", VertexFormats.NORMAL_ELEMENT,
			"State", STATE_ELEMENT, "Padding", VertexFormats.PADDING_ELEMENT));

}
