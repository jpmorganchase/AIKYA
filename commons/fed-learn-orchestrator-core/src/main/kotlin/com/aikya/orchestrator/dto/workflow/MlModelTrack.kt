package com.aikya.orchestrator.dto.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter
import java.util.*
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
class MlModelTrack (
    var source: String,
    var createdDate: Date,
    var performance: BigDecimal
)