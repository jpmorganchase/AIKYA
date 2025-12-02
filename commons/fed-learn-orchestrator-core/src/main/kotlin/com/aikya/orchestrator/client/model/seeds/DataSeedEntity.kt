package com.aikya.orchestrator.client.model.seeds

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "data_seed")
class DataSeedEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    @Column(name = "batch_id")
    var batchId: String? = ""
    @Column(name = "file_path")
    var filePath: String? = ""
    @Column(name = "file_name")
    var fileName: String? = ""
    var label: String? = ""
    var model: String? = ""
    @Column(name = "domain_type", nullable = false)
    var domainType: String? = ""
    @Column(name = "is_mock_data", nullable = false)
    var isMockData: String? = ""
    @Column(name = "status", nullable = false)
    var status: String? = ""

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}