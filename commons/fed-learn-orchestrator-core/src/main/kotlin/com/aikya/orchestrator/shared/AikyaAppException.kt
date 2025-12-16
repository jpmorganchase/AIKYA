package com.aikya.orchestrator.shared

class AikyaAppException(name: String, cause: Throwable?) :
    RuntimeException("Failed to truncate table: $name", cause)