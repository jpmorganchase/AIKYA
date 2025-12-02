package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import java.sql.Blob
import java.util.*

@Entity
@Table(name = "model_client_record_history")
class ModelClientRecordHistoryEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
    @Column(name = "workflow_trace_id")
    val workflowTraceId: String? = "",

    @Column(name = "name", nullable = false)
    val name: String,

    @Lob
    @Column(name = "model_weights")
    val modelWeights: Blob? = null,

    @Column(name = "version")
    val version: Int = 0,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date", nullable = false)
    val lastUpdateDate: Date?
)