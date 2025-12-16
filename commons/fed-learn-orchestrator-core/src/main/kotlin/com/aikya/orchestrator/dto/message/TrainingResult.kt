package com.aikya.orchestrator.dto.message

data class TrainingResult(
    val clientId: String,
    val parameters: String,
    val metrics: Map<String, Double>?,  // Allow metrics to be null
    val numExamples: Int?
)