package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*

@Entity
@Table(name = "vendors")
class FLVendor (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Int = 0,

    @Column(name = "name", nullable = false, length = 255)
    val name: String,

    @Column(name = "description", length = 500)
    val description: String? = null,

    @Column(name = "status", nullable = false, length = 15)
    val status: String,

    @Column(name = "vendor_info", length = 255)
    val vendorInfo: String? = null,

    @OneToMany(mappedBy = "vendor", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val aggregateStrategies: List<FLAggregateStrategy> = mutableListOf()
)