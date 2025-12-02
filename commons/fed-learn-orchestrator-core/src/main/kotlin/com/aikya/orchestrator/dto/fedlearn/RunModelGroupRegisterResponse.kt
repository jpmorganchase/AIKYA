package com.aikya.orchestrator.dto.fedlearn

import com.aikya.orchestrator.dto.common.WebResponse

class RunModelGroupRegisterResponse: WebResponse() {
    var clientId: String? = null
    var groupHash: String? = null
}