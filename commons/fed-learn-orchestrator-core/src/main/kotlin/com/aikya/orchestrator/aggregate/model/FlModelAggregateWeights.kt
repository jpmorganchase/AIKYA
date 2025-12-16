package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.sql.Blob
import java.util.*

@Entity
@Table(name = "model_aggregate_weights")
class FlModelAggregateWeights {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "workflow_trace_id", nullable = false)
    val workflowTraceId: String = ""

    @Column(name = "model_id", nullable = false)
    val modelId: Int? = null

    @Column(name = "version", nullable = false)
    val version: Long? = null

    @Lob
    @Column(name = "parameters", nullable = false)
    val parameters: Blob? = null


    @Column(name = "checksum", length = 64)
    val checksum: String? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

}