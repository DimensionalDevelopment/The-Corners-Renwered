package net.ludocrypt.corners.entity.covrus;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum CorvusPose {
	IDLING,
	SITTING,
	FLYING,
	LANDING;
    public static final StreamCodec<? super RegistryFriendlyByteBuf, CorvusPose> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(index -> CorvusPose.values()[index], Enum::ordinal);
}
