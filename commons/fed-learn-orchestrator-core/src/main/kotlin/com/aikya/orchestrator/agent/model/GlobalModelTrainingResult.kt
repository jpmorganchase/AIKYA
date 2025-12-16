package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "global_model_training_result")
class GlobalModelTrainingResult {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""

    @Column(name = "model_id")
    var modelId: Long? = null

    @Column(name = "loss", nullable = false, precision = 15, scale = 10)
    var loss: BigDecimal? = null

    @Column(name = "num_examples", nullable = false)
    var numExamples: Int? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
}