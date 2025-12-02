package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty

class FlAggClientDataForm (
    val parameters: String,
    val metrics: Map<String, Any> = emptyMap(),
    @JsonProperty("num_examples")
    val numExamples: Int
)