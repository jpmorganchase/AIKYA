package com.aikya.orchestrator.dto.fedlearn

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
class ModelVersionLog {
    var name: String? = null
    var definition: String? = null
    var domain: String? = null
    @JsonProperty("model_version")
    var modelVersion: Int? = null
    @JsonProperty("local_model_weights")
    var localModelWeights: String? = null
    @JsonProperty("local_weights_version")
    var localModelVersion: Int? = null

    override fun toString(): String {
        return "ModelVersionLog{" +
                "name='" + name + '\'' +
                ", domain='" + domain + '\'' +
                ", modelVersion=" + modelVersion +
                ", localModelVersion=" + localModelVersion +
                '}'
    }
}