package net.ludocrypt.corners.mixin;

import net.ludocrypt.corners.client.model.SpecialModelSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Mixin(BlockModel.class)
public class BlockModelMixin implements SpecialModelSource {

    @Unique
    private Map<ResourceLocation, ResourceLocation> corners$specialModels = Map.of();

    @Override
    public Map<ResourceLocation, ResourceLocation> corners$getSpecialModels() {
        return this.corners$specialModels;
    }

    @Override
    public void corners$setSpecialModels(Map<ResourceLocation, ResourceLocation> specialModels) {
        this.corners$specialModels = specialModels.isEmpty() ? Map.of() : Map.copyOf(specialModels);
    }

    @Inject(method = "getDependencies", at = @At("RETURN"), cancellable = true)
    private void corners$addSpecialModelDependencies(CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        if (this.corners$specialModels.isEmpty()) {
            return;
        }

        Set<ResourceLocation> dependencies = new LinkedHashSet<>(cir.getReturnValue());
        dependencies.addAll(this.corners$specialModels.values());
        cir.setReturnValue(Set.copyOf(dependencies));
    }
}
