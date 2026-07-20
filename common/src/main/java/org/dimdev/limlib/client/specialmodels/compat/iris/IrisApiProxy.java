package org.dimdev.limlib.client.specialmodels.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;

final class IrisApiProxy {

    static boolean shouldDisableSpecialModelRenderTypes() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    private IrisApiProxy() {
    }
}
