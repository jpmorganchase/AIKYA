package com.aikya.orchestrator.dto.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
@JsonIgnoreProperties(ignoreUnknown = true)
class UserResponse {
    var statusCode: Int? = 200
    var success: Boolean? = true
    var user: User? = null
    var message: String? = ""
}