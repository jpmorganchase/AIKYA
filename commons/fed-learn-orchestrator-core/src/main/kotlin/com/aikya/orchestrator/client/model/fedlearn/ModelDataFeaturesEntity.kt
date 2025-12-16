package com.aikya.orchestrator.client.model.fedlearn

import jakarta.persistence.*

@Entity
@Table(name = "model_data_features")
data class ModelDataFeaturesEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,

    @Column(name = "domain", nullable = false, length = 20)
    var domain: String,

    @Column(name = "model", nullable = false, length = 50)
    var model: String,

    @Column(name = "db_table", nullable = false, length = 50)
    var dbTable: String,

    @Column(name = "id_field", nullable = false, length = 50)
    var idField: String,

    @Column(name = "feature_field", nullable = false, length = 50)
    var featureField: String,

    @Column(name = "seq_num", nullable = false)
    var seqNum: Int,

    @Column(name = "status", nullable = false, length = 15)
    var status: String
)