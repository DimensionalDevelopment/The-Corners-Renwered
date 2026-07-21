package org.dimdev.corners.client;

import org.dimdev.limlib.client.FabricClientSided;

public class TheCornersFabricClient extends FabricClientSided<TheCornersFabricClient, TheCornersClient> implements CornersClientSided<TheCornersFabricClient> {
    public TheCornersFabricClient() {
        super(new TheCornersClient());
    }
}
