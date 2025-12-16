package com.aikya.orchestrator.shared.model.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*


@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Getter
@Setter
@Table(name = "workflow_detail")
class WorkflowDetailEntity {

    public constructor() {}

    @Column(name = "workflow_id")
    var workflowId: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    var step: Int = 0
    @Column(name = "step_desc")
    var stepDesc: String? = ""
    var event: String? = ""
    var source: String? = ""
    var target: String? = ""
    var status: String? = ""
    var label: String? = ""
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

}