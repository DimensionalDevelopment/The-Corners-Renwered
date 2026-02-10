package net.ludocrypt.corners.packet;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.ludocrypt.corners.TheCorners;
import net.minecraft.resources.ResourceLocation;

public class ClientToServerPackets {

	// S2C
	public static final ResourceLocation PLAY_RADIO = TheCorners.id("play_radio");

	public static void manageClientToServerPackets() {
        PayloadTypeRegistry.playS2C().register(PlayRadio.TYPE, PlayRadio.STREAM_CODEC);
	}

}
