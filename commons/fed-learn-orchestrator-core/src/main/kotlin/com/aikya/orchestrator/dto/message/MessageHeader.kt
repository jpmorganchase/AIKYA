package com.aikya.orchestrator.dto.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
open class MessageHeader {
    var timestamp: Long = 0
    @JsonProperty("is_encrypted")
    var encrypted: Boolean? = false
    var event_type: String? = null
    var workflow_trace_id: String? = null
    var domain: String? = null
    var status: String? = null
}