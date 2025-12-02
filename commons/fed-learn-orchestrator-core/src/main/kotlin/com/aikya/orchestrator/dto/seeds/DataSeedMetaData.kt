package com.aikya.orchestrator.dto.seeds

data class DataSeedMetaData(
    val id: Long,
    var fileName: String,
    var label: String,
    var anomalyDesc: String,
    var domainType: String
)