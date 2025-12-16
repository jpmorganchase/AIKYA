package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty

class TrainingResponse (
    val status: String,
    @JsonProperty("workflow_trace_id")
    val workflowTraceId: String,
    @JsonProperty("num_examples")
    val numExamples: Int
)