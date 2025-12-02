package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class ModelVersionTrackResponse {
    var isModelUsed: Boolean = false
    var version: Long = 0
    var statusCode: Int = 0
    var message: String? = ""
    var success: Boolean? = true
}