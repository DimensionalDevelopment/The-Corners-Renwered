package org.dimdev.limlib.client.specialmodels.mixin.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.RenderSection;
import org.dimdev.limlib.client.specialmodels.compat.sodium.SodiumSpecialModelMeshRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSection.class, remap = false)
public class RenderSectionMixin {

    @Inject(method = "delete", at = @At("HEAD"))
    private void corners$removeSpecialModelMeshes(CallbackInfo ci) {
        SodiumSpecialModelMeshRegistry.remove(((RenderSection) (Object) this).getPosition());
    }
}
