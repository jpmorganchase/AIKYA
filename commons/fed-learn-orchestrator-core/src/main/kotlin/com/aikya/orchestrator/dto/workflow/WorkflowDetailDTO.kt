package com.aikya.orchestrator.dto.workflow

import java.util.*

class WorkflowDetailDTO {
    var id: Long = 0
    var step: Int = 0
    var stepDesc: String? = ""
    var source: String? = ""
    var target: String? = ""
    var stepStatus: String? = ""
    var label: String? = ""
    var createdDate: String? = null
    var lastUpdateDate: String? = null
}