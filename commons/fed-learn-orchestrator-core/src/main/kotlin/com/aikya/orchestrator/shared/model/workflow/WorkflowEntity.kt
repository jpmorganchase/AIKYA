package com.aikya.orchestrator.shared.model.workflow

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Entity
@Getter
@Setter
@Table(name = "workflow")
class WorkflowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    var id: Long? = null
    var status: String? = ""
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    @Column(name = "current_step")
    var currentStep: Int = 0
    @Column(name = "workflow_type_id")
    var workflowTypeId: Long = 0
    @Column(name = "model_id")
    var modelId: Long = 0
    @Column(name = "model_version")
    var modelVersion: Long = 0
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

}