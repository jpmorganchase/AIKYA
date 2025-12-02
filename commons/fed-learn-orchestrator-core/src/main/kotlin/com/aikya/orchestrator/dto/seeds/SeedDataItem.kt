package com.aikya.orchestrator.dto.seeds
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class SeedDataItem {
    var batchId: String? = null
    var itemId: String? = null

    override fun toString(): String {
        return "SeedDataItem(batchId=$batchId, itemId=$itemId)"
    }
}