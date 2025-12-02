package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty

data class PredictResponse(
    val status: String,
    @JsonProperty("workflow_trace_id")
    val workflowTraceId: String,
    val items: Int
)