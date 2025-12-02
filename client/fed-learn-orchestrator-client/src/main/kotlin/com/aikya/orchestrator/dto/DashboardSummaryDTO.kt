package com.aikya.orchestrator.dto

import java.util.*

class DashboardSummaryDTO {
    var batch_id: String? = null
    var workflowTraceId: String? = null
    var totalRecordCount: Int? = null
    var anomalousRecordCount: Int? = null
    var anomalousPercentage: String? = null
    var actualAnomalousRecordCount: Int? = null
    var actualAnomalousPercentage: String? = null
    var modelVersion: String? = null
    var createdDate: Date? = null
    var name: String = ""
    var fileName: String = ""
    var status: String? = ""
    var anomalyDesc: String? = null
}