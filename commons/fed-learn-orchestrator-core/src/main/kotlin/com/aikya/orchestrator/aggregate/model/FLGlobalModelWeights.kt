package com.aikya.orchestrator.aggregate.model
import jakarta.persistence.*
import java.sql.Blob
import java.util.*

@Entity
@Table(name = "global_model_weights")
class FLGlobalModelWeights (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "model_id", nullable = false)
    val modelId: Long,

    @Column(name = "domain", nullable = false)
    val domain: String,

    @Lob
    @Column(name = "global_model_weights")
    var globalModelWeights: Blob? = null,

    @Column(name = "global_weights_version")
    val globalWeightsVersion: Int? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
)