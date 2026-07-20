package net.ludocrypt.corners;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.ludocrypt.corners.client.render.StrongPostEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.dimdev.limlib.FabricSided;

import java.util.Collection;
import java.util.function.Consumer;

public class TheCornersFabric extends FabricSided<TheCornersFabric, TheCorners> implements TheCornersSided<TheCornersFabric>, DedicatedServerModInitializer {
	public TheCornersFabric() {
		super(new TheCorners());
	}

	@Override
	public String getModId() {
		return "corners";
	}

	@Override
	public void onInitializeServer() {
		StrongPostEffect.getType();
	}

	@Override
	public <P extends CustomPacketPayload> void sendPacketTracking(Level world, BlockPos pos, P packet) {
		for(var player : PlayerLookup.tracking((ServerLevel) world, pos)) {
			sendPacket(player, packet);
		}
	}
}
