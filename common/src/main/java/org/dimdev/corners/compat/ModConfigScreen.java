package org.dimdev.corners.compat;

import org.dimdev.corners.compat.cloth_config.ClothConfigCompat;
import org.dimdev.corners.TheCorners;
import net.minecraft.client.gui.screens.Screen;

public class ModConfigScreen {
	public static Screen createScreen(Screen screen) {
		return ClothConfigCompat.create(screen);
	}

}
