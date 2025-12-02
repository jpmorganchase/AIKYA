package com.aikya.orchestrator.dto.message

class AggregationResult  (
    val version: Long,
    val parameters: String,
    val metrics: Map<String, Double>,
    val numExamples: Int,
    val workflowTraceId: String,
    val isSelf: Boolean,
    var historicResults: List<AggregationResult> = mutableListOf()
)