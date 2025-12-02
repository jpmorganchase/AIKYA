package com.aikya.orchestrator.dto.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class Node {
    var id: Long = 0
    var name: String? = null
}