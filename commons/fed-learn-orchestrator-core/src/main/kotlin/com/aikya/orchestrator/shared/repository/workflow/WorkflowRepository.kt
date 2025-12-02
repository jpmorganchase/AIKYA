package com.aikya.orchestrator.shared.repository.workflow

import com.aikya.orchestrator.shared.model.workflow.WorkflowEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkflowRepository : JpaRepository<WorkflowEntity, Long> {

    @Query("from WorkflowEntity wf where wf.workflowTraceId= ?1")
    fun findWorkflowByTraceId(batchId: String): Optional<WorkflowEntity>

    fun findByWorkflowTraceIdIn(workflowTraceIds: List<String>): List<WorkflowEntity>

    @Query("FROM WorkflowEntity wf WHERE wf.status in ('Pending', 'Initial') and wf.modelId= ?1")
    fun findAllPendingWorkflows(modelId: Long): List<WorkflowEntity>

    @Query("from WorkflowEntity wf where wf.workflowTraceId =?1 and (wf.status in ('Complete', 'Fail') or wf.currentStep =3)")
    fun isFeedbackSubmissionDisabled(batchId: String): Optional<WorkflowEntity>

    @Query("FROM WorkflowEntity wf WHERE wf.status = 'Pending' AND wf.currentStep IN (4, 5) AND wf.lastUpdateDate <= :someMinutesAgo")
    fun findPendingStalledWorkflow(@Param("someMinutesAgo") someMinutesAgo: Date): List<WorkflowEntity>

    @Query("from WorkflowEntity wf where wf.status in ('Pending') and wf.currentStep in (6) and wf.modelId= ?1")
    fun findAwaitingRemoteGlobalAggregateWorkflows(modelId: Long):List<WorkflowEntity>

    @Query("from WorkflowEntity wf where wf.status in ('Pending') and wf.currentStep in (5) and wf.modelId= ?1")
    fun findAwaitingPausedWorkflows(modelId: Long):List<WorkflowEntity>

    @Query("from WorkflowEntity wf where wf.status in ('Pending') and wf.currentStep = ?1")
    fun findAllAwaitingCurrentPendingWorkflows(currentStep: Int):List<WorkflowEntity>

    fun findTopByModelIdOrderByCreatedDateDesc(modelId: Long): Optional<WorkflowEntity>

    @Query("SELECT wf FROM WorkflowEntity wf WHERE wf.workflowTraceId IN :workflowTraceIds AND wf.status IN ('Pending', 'Initial')")
    fun findByWorkflowTraceIdsAndStatus(
        @Param("workflowTraceIds") workflowTraceIds: List<String>
    ): List<WorkflowEntity>
}