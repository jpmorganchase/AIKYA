package com.aikya.orchestrator.dto.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class WorkflowNetworkSummary {
    var actionNode: Long? = null
    var latestTrainingDate: String? = null
    var nodes: List<Node?>? = null
    var tag: String? = null
    var version: Long? = null
    var tagSeq: String? = null
    var versionDisplay: String? = null
}