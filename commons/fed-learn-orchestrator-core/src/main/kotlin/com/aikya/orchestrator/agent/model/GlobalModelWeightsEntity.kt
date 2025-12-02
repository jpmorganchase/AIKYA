package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.sql.Blob
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "global_model_weights")
class GlobalModelWeightsEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "model_id")
    var modelId: Long? = null
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""
    @Column(name = "is_self")
    var isSelf: String? = ""
    @Column(name = "version")
    var version: Long? = null
    @Lob
    @Column(name = "parameters", nullable = false)
    var parameters: Blob? = null

    @Column(name = "checksum", length = 64)
    var checksum: String? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null
}