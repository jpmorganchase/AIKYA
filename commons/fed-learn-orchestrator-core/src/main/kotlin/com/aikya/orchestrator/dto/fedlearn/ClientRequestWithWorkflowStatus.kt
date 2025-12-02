package com.aikya.orchestrator.dto.fedlearn

data class ClientRequestWithWorkflowStatus(
    val clientId: Long,
    val workflowTraceId: String?,
    val workflowStatus: String
)