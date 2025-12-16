package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.AgentModelLogsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AgentModelLogsRepository: JpaRepository<AgentModelLogsEntity, Long> {
    @Query("select * from agent_model_logs where model_id = (select id from model_definition where model_name=?1)", nativeQuery = true)
    fun findAgentModelLogsByModelName(modelName: String): Optional<AgentModelLogsEntity>

    @Query("select count(*) from agent_model_logs", nativeQuery = true)
    fun countAgentModelLogs(): Long

    fun findByModelId(modelId: Long): Optional<AgentModelLogsEntity>
}