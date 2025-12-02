package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.GlobalMetrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface GlobalMetricsRepository : JpaRepository<GlobalMetrics, Long> {
    fun findByWorkflowTraceIdAndSource(workflowTraceId: String, source: String): List<GlobalMetrics>
    fun findByWorkflowTraceId(workflowTraceId: String): List<GlobalMetrics>
    @Query("SELECT m FROM GlobalMetrics m WHERE m.source = :source AND m.workflowTraceId IN :workflowTraceIds")
    fun findByWorkflowTraceIdsAndSource(@Param("workflowTraceIds") workflowTraceIds: List<String>, @Param("source") source: String): List<GlobalMetrics>
}