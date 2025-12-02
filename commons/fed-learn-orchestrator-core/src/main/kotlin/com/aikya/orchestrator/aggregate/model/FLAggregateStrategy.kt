package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*

@Entity
@Table(name = "aggregate_strategy")
class FLAggregateStrategy (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int = 0,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @Column(name = "description", length = 500)
    val description: String? = null,

    @Column(name = "func", nullable = false, length = 255)
    val func: String,

    @Column(name = "status", nullable = false, length = 15)
    val status: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    val vendor: FLVendor? = null
)