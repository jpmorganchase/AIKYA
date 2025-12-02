package com.aikya.orchestrator.dto.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class SignupResponse {
    var statusCode: Int = 0
    var code: String? = ""
    var message: String? = ""
    var username: String = ""
}