package com.aikya.orchestrator.dto.fedlearn

import com.aikya.orchestrator.dto.common.WebResponse

class FeedbackResponse: WebResponse() {
    var workflowTraceId: String? = null
    var batchId: String? = null
    var domainType: String? = null
}