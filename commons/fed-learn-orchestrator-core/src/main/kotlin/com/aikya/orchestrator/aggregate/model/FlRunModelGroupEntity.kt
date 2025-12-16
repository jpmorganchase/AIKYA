package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "fl_run_model_group")
data class FlRunModelGroupEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "run_model_id", nullable = false)
    val runModelId: Long,

    @Column(name = "group_hash", nullable = false, length = 500)
    val groupHash: String,

    @Column(name = "client_id", nullable = false, length = 500)
    val clientId: String,

    @Column(name = "seq_num", nullable = false)
    val seqNum: Int,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_model_id", insertable = false, updatable = false)
    val runModel: FlRunModelEntity? = null
) : Serializable