package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "weight_run_mode_detail")
class WeightRunModeDetailEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weight_run_mode_id")
    val clientRunMode: ClientRunModeEntity,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
)