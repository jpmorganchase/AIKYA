package com.aikya.orchestrator.dto.common

data class Mapping(
    val table: String,
    val dbColumns: List<String>,
    val webLabels: List<String>,
    val flagDbColumn: String,
)
