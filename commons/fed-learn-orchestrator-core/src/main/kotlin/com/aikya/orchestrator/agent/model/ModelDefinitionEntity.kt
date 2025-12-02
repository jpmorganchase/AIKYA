package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "model_definition")
class ModelDefinitionEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "model_name", nullable = false)
    var modelName: String? = ""

    @Column(name = "model_desc", nullable = false)
    var modelDesc: String? = ""

    @Column(name = "model_definition", columnDefinition = "TEXT", nullable = false)
    var modelDefinition: String? = ""

    @Column(name = "model_version")
    var modelVersion: Int? = null

    @Column(name = "model_type_id")
    var modelTypeId: Int? = null

    @Column(name = "domain", nullable = false)
    var domain: String? = ""

    @Column(name = "status", nullable = false)
    var status: String? = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}