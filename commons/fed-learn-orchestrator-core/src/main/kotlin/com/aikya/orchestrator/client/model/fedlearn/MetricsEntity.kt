package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "metrics")
data class Metrics(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = "",

    @Column(name = "source")
    var source: String? = "",

    @Column(name = "name", nullable = false)
    val key: String,

    @Column(name = "value", nullable = false)
    val value: Double
) : Serializable