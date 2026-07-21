package org.dimdev.corners.packet;

import org.dimdev.corners.TheCorners;

public class ClientToServerPackets {
	public static void manageClientToServerPackets() {
        TheCorners.getSided().registerClientPacket(PlayRadio.TYPE, PlayRadio.STREAM_CODEC, packert -> ServerToClientPackets.playRadio(packert));
	}
}
