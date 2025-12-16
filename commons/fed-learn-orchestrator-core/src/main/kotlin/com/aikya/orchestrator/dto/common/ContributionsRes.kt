package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class ContributionsRes {
    var name: String? = ""
    var contribution = mutableListOf<PieElementData>()
}