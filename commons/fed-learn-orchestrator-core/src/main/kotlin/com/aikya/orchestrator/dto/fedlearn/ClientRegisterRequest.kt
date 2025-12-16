package com.aikya.orchestrator.dto.fedlearn

class ClientRegisterRequest {
    var clientName: String? = null
    var clientId: Int? = null
    var email: String? = null
    var consentRecord: Boolean = false
    var complianceStatus: Boolean = true
}