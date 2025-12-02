package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*

@Entity
class MLResult(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long,
    @Column(name = "result")
    open var result: Double?
) {
    constructor() : this(0, null) // Default constructor for Hibernate
}