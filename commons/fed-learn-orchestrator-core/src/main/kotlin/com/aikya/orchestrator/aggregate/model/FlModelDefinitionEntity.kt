package com.aikya.orchestrator.aggregate.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "model_definition")
class FlModelDefinitionEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Column(name = "model_name", nullable = false)
    val modelName: String? = ""

    @Column(name = "model_desc", nullable = false)
    val modelDesc: String? = ""

    @Column(name = "model_definition", columnDefinition = "TEXT", nullable = false)
    val modelDefinition: String? = ""

    @Column(name = "model_version")
    val modelVersion: Int? = null

    @Column(name = "model_type_id")
    val modelTypeId: Int? = null

    @Column(name = "domain", nullable = false)
    val domain: String? = ""

    @Column(name = "status", nullable = false)
    val status: String? = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}