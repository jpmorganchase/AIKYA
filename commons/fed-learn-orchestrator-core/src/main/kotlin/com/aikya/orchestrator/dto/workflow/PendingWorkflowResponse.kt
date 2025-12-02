package com.aikya.orchestrator.dto.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class PendingWorkflowResponse {
    var version: String = ""
    val result = mutableListOf<PendingWorkflowDTO>()

}