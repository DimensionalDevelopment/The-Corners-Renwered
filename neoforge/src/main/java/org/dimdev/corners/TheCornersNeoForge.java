package org.dimdev.corners;

import org.dimdev.corners.compat.ModConfigScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.dimdev.limlib.NeoForgeSided;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mod("corners")
public class TheCornersNeoForge extends NeoForgeSided<TheCornersNeoForge, TheCorners> implements TheCornersSided<TheCornersNeoForge> {

	public TheCornersNeoForge(IEventBus bus) {
		super(bus, new TheCorners());
		if (TheCorners.getSided().isModLoaded("cloth-config")) {
			ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (minecraft, parent) -> ModConfigScreen.createScreen(parent));
		}
	}


	@Override
	public <P extends CustomPacketPayload> void sendPacketTracking(Level world, BlockPos pos, P packet) {
		PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) world, new ChunkPos(pos), packet);
	}
}
