package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "client")
class FlClient (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "client_id", nullable = false)
    val clientId: Int = 0,
    @Column(name = "client_name", nullable = false)
    var clientName: String,
    @Column(name = "client_email", nullable = false)
    var clientEmail: String,
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registered_at")
    var registeredAt: Date?,
    @Column(name = "consent_record", nullable = false)
    val consentRecord: Boolean,
    @Column(name = "compliance_status", nullable = false)
    val complianceStatus: Boolean,
    @Column(name = "status", nullable = false)
    var status: String = ""
)