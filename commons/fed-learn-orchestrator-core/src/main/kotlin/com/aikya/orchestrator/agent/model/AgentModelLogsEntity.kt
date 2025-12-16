package com.aikya.orchestrator.agent.model

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@Entity
@Table(name = "agent_model_logs")
class AgentModelLogsEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "model_id")
    var modelId: Long? = null

    @Column(name = "local_weights_version")
    var localWeightsVersion: Long? = null

    @Column(name = "global_weights_version")
    var globalWeightsVersion: Long? = null

}