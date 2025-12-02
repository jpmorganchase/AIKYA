package com.aikya.orchestrator.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class ShapleyValues constructor(featureName: String, shapValues: ArrayList<Float>) {
    var name: String? = featureName;
    var values: ArrayList<Float> = ArrayList();
}