package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter
import java.util.*

@Getter
@Setter
@Entity
@Table(name = "agent_local_model_track")
class AgentModelTrackEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "workflow_trace_id")
    var workflowTraceId: String? = ""

    @Column(name = "model_id")
    val modelId: Int? = null

    @Lob
    @Column(name = "model_weights", nullable = false)
    var modelWeights: ByteArray? = null

    @Column(name = "version")
    val version: Int? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    var createdDate: Date? = null
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_update_date")
    var lastUpdateDate: Date? = null

}