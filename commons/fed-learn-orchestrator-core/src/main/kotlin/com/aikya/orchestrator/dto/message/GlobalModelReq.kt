package com.aikya.orchestrator.dto.message

class GlobalModelReq (
    val clientId: String,
    val version: Long,
    val size: Int,
    val model: String
)