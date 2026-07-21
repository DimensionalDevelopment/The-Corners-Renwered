package net.ludocrypt.corners.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.dimdev.limlib.client.NeoForgeClientSided;

@Mod(value = "corners", dist = Dist.CLIENT)
public class TheCornerNeoForgeClient extends NeoForgeClientSided<TheCornerNeoForgeClient, TheCornersClient> implements CornersClientSided<TheCornerNeoForgeClient> {

    public TheCornerNeoForgeClient(IEventBus bus, ModContainer container) {
        super(bus, container, new TheCornersClient());
    }
}
