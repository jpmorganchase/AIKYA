package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*

@Entity
@Table(name = "run_model")
class FlRunModelEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "model_id", nullable = false)
    val modelId: Long? = null

    @Column(name = "name", nullable = false, length = 50)
    val name: String? = null

    @Column(name = "description", length = 500)
    val description: String? = null

    @Column(name = "participants_number")
    val participantsNumber: Int? = null

    @OneToMany(mappedBy = "runModel", cascade = [CascadeType.ALL], orphanRemoval = true)
    val runModelGroups: List<FlRunModelGroupEntity> = mutableListOf()
}