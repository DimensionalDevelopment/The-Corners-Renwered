package net.ludocrypt.corners.packet;

import net.ludocrypt.corners.TheCorners;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PlayRadio(BlockPos pos, boolean active) implements CustomPacketPayload {
    public static final Type<PlayRadio> TYPE = new Type<>(TheCorners.id("play_radio"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayRadio> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, PlayRadio::pos, ByteBufCodecs.BOOL, PlayRadio::active, PlayRadio::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}
