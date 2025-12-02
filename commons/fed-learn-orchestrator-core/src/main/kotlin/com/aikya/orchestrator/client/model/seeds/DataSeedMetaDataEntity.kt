package com.aikya.orchestrator.client.model.seeds

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "data_seed_metadata")
class DataSeedMetaDataEntity {
    public constructor() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(name = "domain_type")
    var domainType: String? = ""

    @Column(name = "batch_id")
    var batchId: String? = ""

    @Column(name = "file_name")
    var fileName: String? = ""

    @Column(name = "anomaly_desc")
    var anomalyDesc: String? = ""

    @Column(name = "label")
    var label: String? = ""

    @Column(name = "is_mock_data")
    var isMockData: String? = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}