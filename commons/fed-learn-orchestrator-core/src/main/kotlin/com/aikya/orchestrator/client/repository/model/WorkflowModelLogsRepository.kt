package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.WorkflowModelLogsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkflowModelLogsRepository  : JpaRepository<WorkflowModelLogsEntity, Long> {
    @Query("from WorkflowModelLogsEntity wml where wml.event= ?1 and wml.status=?2")
    fun findWorkflowModelLogsByEventStatus(event: String, status: String): List<WorkflowModelLogsEntity>

    @Query("from WorkflowModelLogsEntity wml where wml.workflowTraceId = ?1 and wml.event= ?2")
    fun findWorkflowModelLogsByWorkflowTraceId(workflowTraceId: String, event: String): WorkflowModelLogsEntity

    @Query("from WorkflowModelLogsEntity wml where wml.workflowTraceId = ?1 and wml.event= ?2")
    fun findOptionalWorkflowModelLogsByWorkflowTraceId(workflowTraceId: String, event: String): Optional<WorkflowModelLogsEntity>
}