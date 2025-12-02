package com.aikya.orchestrator.dto.fedlearn

data class Parameters(
    val tensors: List<ByteArray>,
    val tensorType: String
)