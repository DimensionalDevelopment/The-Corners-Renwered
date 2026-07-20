package net.ludocrypt.corners;

import net.ludocrypt.corners.packet.PlayRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.BreezeDebugPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.dimdev.limlib.api.ISided;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public interface TheCornersSided<T extends TheCornersSided<T>> extends ISided<T> {

    <P extends CustomPacketPayload> void sendPacketTracking(Level world, BlockPos pos, P packet);
}
