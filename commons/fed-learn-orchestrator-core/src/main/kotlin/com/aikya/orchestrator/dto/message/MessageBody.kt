package com.aikya.orchestrator.dto.message

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class MessageBody {
    var data: List<out Any?>? = null
}