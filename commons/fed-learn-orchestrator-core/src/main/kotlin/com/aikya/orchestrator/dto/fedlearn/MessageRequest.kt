package com.aikya.orchestrator.dto.fedlearn


import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import com.fasterxml.jackson.annotation.JsonProperty

data class MessageRequest(
    @field:NotBlank(message = "Message ID must not be blank")
    @JsonProperty("message_id")
    val messageId: String,

    @field:NotBlank(message = "Client ID must not be blank")
    @JsonProperty("client_id")
    val clientId: String,

    @field:NotBlank(message = "Strategy must not be blank")
    val strategy: String,

    @field:NotNull(message = "Parameters must not be null")
    val parameters: Map<String, Any>, // 'Any' can be a complex object or simple types like String, Int, etc.

    @field:NotNull(message = "Metrics must not be null")
    val metrics: Map<String, Float>,

    @field:NotNull(message = "Number of examples must not be null")
    @JsonProperty("num_examples")
    val numExamples: Int,

    val properties: Map<String, Any>
)