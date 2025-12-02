package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "model_training_result")
data class ModelTrainingResultEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "workflow_trace_id", nullable = false, length = 50)
    val workflowTraceId: String,

    @Column(name = "num_examples", nullable = false)
    val numExamples: Int,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    val createdDate: Date? = null
) : Serializable