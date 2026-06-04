package net.ludocrypt.corners.client.model;

import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public interface SpecialModelSource {

    Map<ResourceLocation, ResourceLocation> corners$getSpecialModels();

    void corners$setSpecialModels(Map<ResourceLocation, ResourceLocation> specialModels);
}
