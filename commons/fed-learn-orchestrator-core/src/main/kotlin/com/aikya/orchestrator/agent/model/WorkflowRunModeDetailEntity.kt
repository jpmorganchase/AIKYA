package com.aikya.orchestrator.agent.model
import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "workflow_run_mode_detail")
class WorkflowRunModeDetailEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_run_mode_id")
    val workflowRunMode: ClientRunModeEntity,

    @Column(name = "workflow_trace_id", length = 500)
    val workflowTraceId: String,

    @Column(name = "workflow_step")
    val workflowStep: Int,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
)