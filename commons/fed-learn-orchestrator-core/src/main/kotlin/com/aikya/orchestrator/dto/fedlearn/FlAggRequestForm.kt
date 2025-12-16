package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty

data class FlAggRequestForm (
    val domainType: String,

    @JsonProperty("num_rounds")
    val numRounds: Int,

    val strategy: List<String>,
    val vendor: String,
    val workflowTraceId: String,
    val modelId: Long,
    val groupHash: String,
    val clients: List<FlAggClientForm>
)