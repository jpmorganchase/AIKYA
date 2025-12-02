package com.aikya.orchestrator.dto.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class SigninResponse {
    var statusCode: Int = 0
    var message: String? = ""
    var accessToken: String = ""
    var tokenType: String? = ""
    var refreshToken: String? = ""
    var idToken: String? = ""
    var expiresIn: Int = 0
}