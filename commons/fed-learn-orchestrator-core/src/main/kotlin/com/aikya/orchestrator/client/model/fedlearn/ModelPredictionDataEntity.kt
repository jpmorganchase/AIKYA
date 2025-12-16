package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "model_predict_data")
class ModelPredictionDataEntity {
    @Column(name = "batch_id")
    var batchId: String? = null

    @Column(name = "item_id")
    var itemId: String? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    var domain: String = ""

    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    var result: String = ""

    @Column(name = "confidence_score")
    val confidenceScore: Double? = null

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    var id: Long? = null
}