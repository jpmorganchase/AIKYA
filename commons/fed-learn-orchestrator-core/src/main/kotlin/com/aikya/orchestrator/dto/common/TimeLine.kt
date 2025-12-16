package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class TimeLine {
    var name: String? = ""
    var versions = mutableListOf<String>()
    var values = mutableListOf<String>()
}

