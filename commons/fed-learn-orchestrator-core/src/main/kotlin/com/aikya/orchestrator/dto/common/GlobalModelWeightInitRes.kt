package com.aikya.orchestrator.dto.common

class GlobalModelWeightInitRes {
    var success: Boolean = false
    var message: String? = null
    var status: Status? = null

    enum class Status {
        SUCCESS,
        REMOTE_CALL_FAILED,
        LOCAL_PROCESSING_ERROR,
        NO_RESULT_FOUND,
        ALREADY_INITIALIZED
    }
}