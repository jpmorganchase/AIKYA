package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.Metrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MetricsRepository : JpaRepository<Metrics, Long> {
    fun findByWorkflowTraceIdAndSource(workflowTraceId: String, source: String): List<Metrics>
    fun findByWorkflowTraceId(workflowTraceId: String): List<Metrics>
    @Query("SELECT m FROM Metrics m WHERE m.source = :source AND m.workflowTraceId IN :workflowTraceIds")
    fun findByWorkflowTraceIdsAndSource(@Param("workflowTraceIds") workflowTraceIds: List<String>, @Param("source") source: String): List<Metrics>
}