package org.dimdev.corners;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import org.dimdev.corners.client.render.StrongPostEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.dimdev.limlib.FabricSided;

public class TheCornersFabric extends FabricSided<TheCornersFabric, TheCorners> implements TheCornersSided<TheCornersFabric>, DedicatedServerModInitializer {
	public TheCornersFabric() {
		super(new TheCorners());
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
