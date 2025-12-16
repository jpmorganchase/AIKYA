package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "model_feedback")
class ModelFeedbackEntity (

    @Column(name = "model_data_id")
    val modelDataId: Long = 0,

    @Column(name = "batch_id", nullable = false)
    open val batchId: String? = "",

    @Column(name = "workflow_trace_id")
    val workflowTraceId: String? = "",

    @Column(name = "item_id")
    val itemId: Long = 0,

    @Column(name = "score")
    val score: Int = 0,

    @Column(name = "is_correct")
    var isCorrect: String? = "Y",

    val comment: String? = null,

    var status: Int = 0,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    val createdDate: Date? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)