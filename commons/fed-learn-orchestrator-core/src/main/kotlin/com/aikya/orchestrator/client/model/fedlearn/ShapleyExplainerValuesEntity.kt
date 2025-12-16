package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "model_predict_shap_data")
data class ShapleyExplainerValuesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "workflow_trace_id", nullable = false)
    var workflowTraceId: String? = "",

    @Column(name = "domain", nullable = false)
    var domain: String? = "",

    @Column(name = "batch_id", nullable = false)
    val batchId: String,

    @Column(name = "shapley_values", nullable = false, columnDefinition = "json")
    val shapleyValues: String
) : Serializable