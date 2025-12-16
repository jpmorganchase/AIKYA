package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*
import java.sql.Blob
import java.util.*

@Entity
@Table(name = "model_client_records")
class ModelClientRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null

    @Column(name = "name", nullable = false)
    var name: String? =null

    @Column(name = "definition", nullable = false, columnDefinition = "TEXT")
    var definition: String? =null

    @Column(name = "model_version")
    var modelVersion: Int? = 0

    @Column(name = "domain", nullable = false, length = 50)
    var domain: String? =null

    @Lob
    @Column(name = "local_model_weights")
    var localModelWeights: Blob? = null

    @Column(name = "local_weights_version")
    var localWeightsVersion: Int? = null

    @Lob
    @Column(name = "global_model_weights")
    var globalModelWeights: Blob? = null

    @Column(name = "global_weights_version")
    var globalWeightsVersion: Long? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date", nullable = false)
    var lastUpdateDate: Date?= null
}