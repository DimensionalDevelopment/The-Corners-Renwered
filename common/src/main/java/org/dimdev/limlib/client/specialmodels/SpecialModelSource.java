package org.dimdev.limlib.client.specialmodels;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface SpecialModelSource {

    Map<ResourceLocation, ResourceLocation> corners$getSpecialModels();

    void corners$setSpecialModels(Map<ResourceLocation, ResourceLocation> specialModels);
}
