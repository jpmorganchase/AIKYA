package com.aikya.orchestrator.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class DataSeedLabelRequest {
    var domainType: String? = ""
    var fileName: String? = ""
    var label: String? = ""
    var batchId: String? = ""
    var anomalyDesc: String? = ""
}
