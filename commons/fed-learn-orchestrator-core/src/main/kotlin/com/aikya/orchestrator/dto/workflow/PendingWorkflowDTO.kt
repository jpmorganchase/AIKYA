package com.aikya.orchestrator.dto.workflow

import java.util.*

class PendingWorkflowDTO {
    var workflowId: Long = 0
    var currentStep: Int = 0
    var workflowTraceId: String? = ""
    var workflowStatus: String? = ""
    var dataLoadDate: String? = null
    var trainingDate: String? = null
    var version: String? = ""
    var steps: MutableList<WorkflowDetailDTO>? = mutableListOf()
}