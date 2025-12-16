package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class PieElementData {
    var name: String? = ""
    var value: Double? = 0.0
    var dbValue: String? = ""
}