package com.aikya.orchestrator.client.model.seeds

import jakarta.persistence.*

@MappedSuperclass
open class DataEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long = 0,
    @Column(name = "batch_id", nullable = false)
    open val batchId: String
)