package com.aikya.orchestrator.dto.common

class ClientRunModel {
    var id: Long = 0
    var mode: String? = ""
    var name: String? = ""
    var domain: String? = ""
    var modes = mutableListOf<String>()
}