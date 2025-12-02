package com.aikya.orchestrator.dto.seeds

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class SeedProcessResponse {
    var success : Boolean? = true
    var message: String? = null
    var filePath: String? = null
}