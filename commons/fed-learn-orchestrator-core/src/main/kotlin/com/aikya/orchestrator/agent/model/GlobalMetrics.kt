package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "global_metrics")
data class GlobalMetrics (
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