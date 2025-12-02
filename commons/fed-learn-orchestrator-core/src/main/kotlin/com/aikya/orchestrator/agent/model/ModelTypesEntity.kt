package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@Entity
@Table(name = "model_types")
class ModelTypesEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "type_name", nullable = false)
    val typeName: String? = ""
    @Column(name = "description", nullable = false)
    val description: String? = ""
}