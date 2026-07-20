package org.dimdev.limlib.client.specialmodels.mixin;

import net.minecraft.client.resources.model.SimpleBakedModel;
import org.dimdev.limlib.client.specialmodels.SpecialModelLoadingPlugin.SpecialModelPart;
import org.dimdev.limlib.client.specialmodels.SpecialModelPartsHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements SpecialModelPartsHolder {

    @Unique
    private List<SpecialModelPart> corners$specialModelParts = List.of();

    @Override
    public List<SpecialModelPart> corners$getSpecialModelParts() {
        return this.corners$specialModelParts;
    }

    @Override
    public void corners$setSpecialModelParts(List<SpecialModelPart> specialModelParts) {
        this.corners$specialModelParts = specialModelParts.isEmpty() ? List.of() : List.copyOf(specialModelParts);
    }
}
