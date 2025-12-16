package com.aikya.orchestrator.dto.message

class Message (
    var id: String? = null,
    val header: MessageHeader? = null,
    val body: MessageBody? = null
)