package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@Entity
@Table(name = "workflow_model_logs")
class WorkflowModelLogsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    var status: String? = ""
    var event: String? = ""
}