package com.aikya.orchestrator.shared.model.workflow

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
class PendingWorkflow  {
    @Id
    @Column(name = "workflow_detail_id")
    var workflowDetailId = 0L
    @Column(name = "workflow_id")
    var workflowId = 0L
    @Column(name = "model_id")
    var modelId = 0L
    @Column(name = "current_step")
    var currentStep: Int = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    @Column(name = "workflow_status")
    var workflowStatus: String? = ""
    var step: Int = 0
    @Column(name = "step_desc")
    var stepDesc: String? = ""
    var source: String? = ""
    var target: String? = ""
    @Column(name = "step_status")
    var stepStatus: String? = ""
    var label: String? = ""
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

}