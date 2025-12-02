package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlMetrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FlMetricsRepository : JpaRepository<FlMetrics, Long> {
    fun findByWorkflowTraceIdAndSource(workflowTraceId: String, source: String): List<FlMetrics>
    fun findByWorkflowTraceId(workflowTraceId: String): List<FlMetrics>
    @Query("SELECT m FROM FlMetrics m WHERE m.source = :source AND m.workflowTraceId IN :workflowTraceIds")
    fun findByWorkflowTraceIdsAndSource(@Param("workflowTraceIds") workflowTraceIds: List<String>, @Param("source") source: String): List<FlMetrics>
}