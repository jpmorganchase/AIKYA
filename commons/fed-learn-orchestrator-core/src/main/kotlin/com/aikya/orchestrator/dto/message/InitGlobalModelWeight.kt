package com.aikya.orchestrator.dto.message

class InitGlobalModelWeight (
    val version: Int,
    val parameters: String,
    val modelDefinition: String
)