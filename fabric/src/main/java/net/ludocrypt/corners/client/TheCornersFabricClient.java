package net.ludocrypt.corners.client;

import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.renderer.v1.model.WrapperBakedModel;
import net.minecraft.client.resources.model.BakedModel;
import org.dimdev.limlib.client.FabricClientSided;
import org.dimdev.limlib.client.specialmodels.SpecialModelLoadingPlugin;
import org.jetbrains.annotations.Nullable;

public class TheCornersFabricClient extends FabricClientSided<TheCornersFabricClient, TheCornersClient> implements CornersClientSided<TheCornersFabricClient> {

    public TheCornersFabricClient() {
        super(new TheCornersClient());
    }

    @Override
    public void registerSpecialModelLoadingPlugin() {
        PreparableModelLoadingPlugin.register(SpecialModelLoadingPlugin::loadSpecialModelIds, (specialModelIds, context) -> {
            SpecialModelLoadingPlugin.prepareModelLoading();
            context.addModels(specialModelIds);
        });
    }

    @Override
    public @Nullable BakedModel getWrappedBakedModel(BakedModel model) {
        if (model instanceof WrapperBakedModel wrapper) {
            return wrapper.getWrappedModel();
        }

        return null;
    }
}
