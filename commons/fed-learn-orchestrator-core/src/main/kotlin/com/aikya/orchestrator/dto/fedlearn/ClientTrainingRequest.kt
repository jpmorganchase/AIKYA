package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank

class ClientTrainingRequest (
    @field:NotBlank(message = "Message ID must not be blank")
    @JsonProperty("message_id")
    val messageId: String,

    @field:NotBlank(message = "Client ID must not be blank")
    @JsonProperty("client_id")
    val clientId: String,

    @field:NotBlank(message = "Data Path must not be blank")
    @JsonProperty("data_path")
    val dataPath: String,
)