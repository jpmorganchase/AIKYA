package com.aikya.orchestrator.dto.common

import java.util.*

class DashboardSummary {
    var batch_id: String? = null
    var workflowTraceId: String? = null
    var totalRecordCount: Int? = null
    var anomalousRecordCount: Int? = null
    var anomalousPercentage: String? = null
    var modelVersion: String? = null
    var createdDate: Date? = null
    var name: String = ""
    var fileName: String = ""
    var id: Long = 0
    var status: String? = ""
}