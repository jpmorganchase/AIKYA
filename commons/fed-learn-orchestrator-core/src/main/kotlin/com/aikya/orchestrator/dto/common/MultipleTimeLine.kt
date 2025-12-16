package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class MultipleTimeLine {
    var name: String? = ""
    var series = mutableListOf<Serie>()
    var xAxis = mutableListOf<String>()

    fun addSerie(serie: Serie) {
        series.add(serie)
    }
}