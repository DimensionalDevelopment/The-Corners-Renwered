package net.ludocrypt.corners.mixin;

import net.ludocrypt.corners.client.TheCornersModelPlugin;
import net.ludocrypt.corners.client.model.SpecialModelPartsHolder;
import net.minecraft.client.resources.model.SimpleBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(SimpleBakedModel.class)
public class SimpleBakedModelMixin implements SpecialModelPartsHolder {

    @Unique
    private List<TheCornersModelPlugin.SpecialModelPart> corners$specialModelParts = List.of();

    @Override
    public List<TheCornersModelPlugin.SpecialModelPart> corners$getSpecialModelParts() {
        return this.corners$specialModelParts;
    }

    @Override
    public void corners$setSpecialModelParts(List<TheCornersModelPlugin.SpecialModelPart> specialModelParts) {
        this.corners$specialModelParts = specialModelParts.isEmpty() ? List.of() : List.copyOf(specialModelParts);
    }
}
