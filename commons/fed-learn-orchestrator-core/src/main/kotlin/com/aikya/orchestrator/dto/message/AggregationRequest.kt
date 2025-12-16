package com.aikya.orchestrator.dto.message

class AggregationRequest (
    val clientId: String,
    val version: Long,
    val size: Int,
    val model: String,
)