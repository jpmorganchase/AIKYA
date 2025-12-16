package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonProperty

class FeedbackForm {
    var modelDataId: Long = 0
    //feed back
    var score = 0
    var comment: String? = null
    @JsonProperty("isCorrect")
    var isCorrect: Boolean? = null
}