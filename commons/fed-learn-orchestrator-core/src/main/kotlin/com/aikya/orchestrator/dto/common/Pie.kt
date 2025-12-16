package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Pie {
    var name: String? = ""
    val children = mutableListOf<PieElementData>()
}