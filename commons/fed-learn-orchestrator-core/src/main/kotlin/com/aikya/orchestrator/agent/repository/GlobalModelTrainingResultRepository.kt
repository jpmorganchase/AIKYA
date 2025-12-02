package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.GlobalModelTrainingResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GlobalModelTrainingResultRepository : JpaRepository<GlobalModelTrainingResult, Long> {
    @Query("SELECT f FROM GlobalModelTrainingResult f WHERE f.workflowTraceId = :workflowTraceId")
    fun findByWorkflowTraceId(@Param("workflowTraceId") workflowTraceId: String): GlobalModelTrainingResult
}