package com.aikya.orchestrator.agent.model

import jakarta.persistence.*

@Entity
@Table(name = "client_run_mode")
class ClientRunModeEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(name = "mode", length = 20)
    var mode: String,
    @Column(name = "domain", length = 50)
    val domain: String,
    @Column(name = "name", length = 50)
    val name: String
)