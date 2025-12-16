package com.aikya.orchestrator.dto.fedlearn

class GlobalVersionRequest (
    var domain: String,
    var version: Long,
    var modelName: String,
    var modelId: Long
)