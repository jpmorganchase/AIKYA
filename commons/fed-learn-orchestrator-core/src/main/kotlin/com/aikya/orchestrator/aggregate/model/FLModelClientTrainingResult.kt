package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.sql.Blob
import java.util.*

@Entity
@Table(name = "model_client_training_result")
class FLModelClientTrainingResult {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""

    @Column(name = "client_workflow_trace_id")
    var clientWorkflowTraceId: String? = ""

    @Column(name = "client_id")
    var clientId: Long? = null

    @Column(name = "domain")
    var domain: String? = null

    @Column(name = "model_id")
    var modelId: Long? = null
    @Lob
    @Column(name = "parameters", nullable = false)
    var parameters: Blob? = null
    @Column(name = "checksum", length = 64)
    var checksum: String? = null

    @Column(name = "loss", nullable = false, precision = 15, scale = 10)
    var loss: BigDecimal? = null

    @Column(name = "num_examples", nullable = false)
    var numExamples: Int? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}