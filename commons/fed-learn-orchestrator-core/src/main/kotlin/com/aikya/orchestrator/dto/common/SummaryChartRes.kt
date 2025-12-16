package com.aikya.orchestrator.dto.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
class SummaryChartRes {
    var performance:MultipleTimeLine? = null
    var contributions: ContributionsRes? = null
}