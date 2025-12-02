package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.WorkflowRunModeDetailEntity
import com.aikya.orchestrator.agent.model.ClientRunModeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkflowRunModeDetailRepository : JpaRepository<WorkflowRunModeDetailEntity, Long> {
    fun findByWorkflowTraceId(workflowTraceId: String): List<WorkflowRunModeDetailEntity>
    fun findByWorkflowRunMode(workflowRunMode: ClientRunModeEntity): List<WorkflowRunModeDetailEntity>
}