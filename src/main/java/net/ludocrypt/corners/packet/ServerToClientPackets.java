package org.dimdev.corners.packet;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import org.dimdev.corners.access.MusicTrackerAccess;
import org.dimdev.corners.client.sound.LoopingPositionedSoundInstance;
import org.dimdev.corners.init.CornerBlocks;
import org.dimdev.corners.init.CornerPaintings;
import org.dimdev.corners.init.CornerRadioRegistry;
import org.dimdev.corners.mixin.SoundManagerAccessor;
import org.dimdev.corners.util.DimensionalPaintingTeleportLogic;
import org.dimdev.corners.util.RadioSoundTable;
import net.ludocrypt.limlib.impl.access.SoundSystemAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public class ServerToClientPackets {

	public static void manageServerToClientPackets() {
		ClientPlayNetworking.registerGlobalReceiver(PlayRadio.TYPE, (buf, handler) -> {
            BlockPos pos = buf.pos();
            boolean start = buf.active();
            var client = handler.client();
            client.execute(() -> {
                RadioSoundTable id = client.level.getEntitiesOfClass(Painting.class, AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos)).inflate(16.0D),
                                (entity) -> entity.getVariant().unwrap().left().map(CornerPaintings.LOGICS::containsKey).orElse(false))
                        .stream().min(Comparator.comparing((entity) -> entity.distanceToSqr(Vec3.atLowerCornerOf(pos))))
                        .map(Painting::getVariant)
                        .map(Holder::unwrap)
                        .map(either -> either.left())
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(CornerPaintings.LOGICS::containsKey)
                        .map(CornerPaintings.LOGICS::get)
                        .map(DimensionalPaintingTeleportLogic::radioRedirect)
                        .map(CornerRadioRegistry::getCurrent)
                        .orElseGet(() -> CornerRadioRegistry.getCurrent(client));

                SoundSystemAccess
                        .get(((SoundManagerAccessor) client.getSoundManager()).getSoundEngine())
                        .stopSoundsAtPosition(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, null,
                                SoundSource.RECORDS);
                ((MusicTrackerAccess) (client.getMusicManager())).getRadioPositions().remove(pos);

                if (start) {
                    ((MusicTrackerAccess) (client.getMusicManager())).getRadioPositions().add(pos);
                    SoundEvent soundEvent = id.getStaticSound().value();

                    if (client.level.getBlockState(pos).is(CornerBlocks.WOODEN_RADIO)) {
                        soundEvent = id.getRadioSound().value();
                    } else if (client.level.getBlockState(pos).is(CornerBlocks.TUNED_RADIO)) {
                        soundEvent = id.getMusicSound().value();
                    }

                    LoopingPositionedSoundInstance
                            .play(client.level, pos, soundEvent, SoundSource.RECORDS, 1.0F, 1.0F,
                                    RandomSource.create(), pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
                }

            });
        });
	}

}
