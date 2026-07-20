package org.dimdev.limlib.client.specialmodels;

import org.dimdev.limlib.client.specialmodels.SpecialModelLoadingPlugin.SpecialModelPart;

import java.util.List;

public interface SpecialModelPartsHolder {

    List<SpecialModelPart> corners$getSpecialModelParts();

    void corners$setSpecialModelParts(List<SpecialModelPart> specialModelParts);
}
