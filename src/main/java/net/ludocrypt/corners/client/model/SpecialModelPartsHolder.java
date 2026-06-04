package net.ludocrypt.corners.client.model;

import net.ludocrypt.corners.client.TheCornersModelPlugin;

import java.util.List;

public interface SpecialModelPartsHolder {

    List<TheCornersModelPlugin.SpecialModelPart> corners$getSpecialModelParts();

    void corners$setSpecialModelParts(List<TheCornersModelPlugin.SpecialModelPart> specialModelParts);
}
