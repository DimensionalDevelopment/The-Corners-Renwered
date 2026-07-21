package org.dimdev.corners;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.dimdev.limlib.api.ISided;

public interface TheCornersSided<T extends TheCornersSided<T>> extends ISided<T> {

    <P extends CustomPacketPayload> void sendPacketTracking(Level world, BlockPos pos, P packet);
}
