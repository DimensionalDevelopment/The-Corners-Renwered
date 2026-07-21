package org.dimdev.corners.compat;

import org.dimdev.corners.compat.cloth_config.ClothConfigCompat;
import org.dimdev.corners.TheCorners;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {
	public static Screen createScreen(Screen screen) {
		if (TheCorners.getSided().isModLoaded("cloth-config")) {
			return ClothConfigCompat.create(screen);
		} else {
			return new MissingClothConfigScreen(screen);
		}
	}

	private static class MissingClothConfigScreen extends Screen {
		private final Screen parent;

		private MissingClothConfigScreen(Screen parent) {
			super(Component.literal("Cloth Config Missing"));
			this.parent = parent;
		}

		@Override
		protected void init() {
			this.addRenderableWidget(Button
				.builder(Component.translatable("gui.back"), button -> this.onClose())
				.bounds(this.width / 2 - 100, this.height / 2 + 12, 200, 20)
				.build());
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
			this.renderBackground(guiGraphics, mouseX, mouseY, delta);
			guiGraphics.drawCenteredString(
				this.font,
				Component.literal("Config screen needs Cloth Config installed"),
				this.width / 2,
				this.height / 2 - 18,
				0xFFFFFF);
			super.render(guiGraphics, mouseX, mouseY, delta);
		}

		@Override
		public void onClose() {
			this.minecraft.setScreen(this.parent);
		}
	}
}
