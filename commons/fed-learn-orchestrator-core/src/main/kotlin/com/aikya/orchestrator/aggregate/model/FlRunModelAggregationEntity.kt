package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "run_model_aggregation")
class FlRunModelAggregationEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int = 0,

    @Column(name = "workflow_trace_id", nullable = false, length = 500)
    val workflowTraceId: String?=null,

    @Column(name = "client_id", nullable = false, length = 500)
    val clientId: String?=null,

    @Column(name = "model_id", nullable = false)
    val modelId: Int?=null,

    @Column(name = "group_hash", nullable = false, length = 500)
    val groupHash: String?=null,

    @Column(name = "model_weights_id")
    val modelWeightsId: Int? = null,

    @Column(name = "loss", nullable = false, precision = 15, scale = 10)
    val loss: BigDecimal?=null,

    @Column(name = "num_examples", nullable = false)
    val numExamples: Int?=null,

    @Column(name = "status", nullable = false, length = 15)
    val status: String?=null,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

    ) {
        @ManyToOne
        @JoinColumn(name = "model_id", insertable = false, updatable = false)
        lateinit var modelDefinition: FlModelDefinitionEntity

        @ManyToOne
        @JoinColumn(name = "model_weights_id", insertable = false, updatable = false)
        var modelAggregateWeights: FlModelAggregateWeights? = null
    }