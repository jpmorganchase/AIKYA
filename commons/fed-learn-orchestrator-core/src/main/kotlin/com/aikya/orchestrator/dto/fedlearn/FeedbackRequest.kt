package com.aikya.orchestrator.dto.fedlearn

class FeedbackRequest {
    val feedbacks = mutableListOf<FeedbackForm>()
    var batchId: String? = null
}