package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "collaboration_run_client")
class FlCollaborationRunClient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    var run: FLModelCollaborationRun? = null

    @Column(name = "group_hash", nullable = false)
    var groupHash: String = ""

    @Column(name = "rounds", nullable = false)
    var rounds: Int = 1

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    var client: FlClient? = null

    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""

    @Column(name = "client_workflow_trace_id")
    var clientWorkflowTraceId: String? = ""

    @Column(name = "min_clients_per_round", nullable = false)
    var minClientsPerRound: Int = 0

    @Column(name = "is_submitted", nullable = false)
    var isSubmitted: Boolean = false

    @Column(name = "is_round_complete", nullable = false)
    var isRoundComplete: Boolean = false

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "submission_date")
    var submissionDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

}