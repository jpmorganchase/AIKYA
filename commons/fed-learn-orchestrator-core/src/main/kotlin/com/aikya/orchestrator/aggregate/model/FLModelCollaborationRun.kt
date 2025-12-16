package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "model_collaboration_run")
class FLModelCollaborationRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    var model: FlModelDefinitionEntity? = null

    @Column(name = "run_model_id")
    var runModelId: Long? = null

    @Column(name = "group_hash", nullable = false)
    var groupHash: String = ""

    @Column(name = "rounds", nullable = false)
    var rounds: Int = 0

    @Column(name = "current_round", nullable = true)
    var currentRound: Int = 1

    @Column(name = "min_clients", nullable = false)
    var minClients: Int = 0

    @Column(name = "status", nullable = false)
    var status: String = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at")
    var startedAt: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_at")
    var completedAt: Date? = null

    @OneToMany(mappedBy = "run", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var clients: List<FlCollaborationRunClient> = mutableListOf()

}