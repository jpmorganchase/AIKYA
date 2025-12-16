package com.aikya.orchestrator.shared.repository.workflow

import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkflowDetailRepository : JpaRepository<WorkflowDetailEntity, Long> {
    @Query("from WorkflowDetailEntity wd where wd.workflowId =?1 and wd.step= ?2")
    fun findWorkflowDetailByStep(workflowId: Long, step: Int): WorkflowDetailEntity?

    @Query("from WorkflowDetailEntity wd where wd.workflowTraceId =?1 and wd.step= ?2")
    fun findDetailByWorkflowTraceId(workflowTraceId: String, step: Int): Optional<WorkflowDetailEntity>

    @Query("SELECT w FROM WorkflowDetailEntity w WHERE w.step = :step AND w.status = :status")
    fun findByStepAndStatus(@Param("step") step: Int, @Param("status") status: String): List<WorkflowDetailEntity>

    @Query("SELECT w FROM WorkflowDetailEntity w WHERE w.step = :step AND w.status in ('Pending', 'Initial')")
    fun findByStepAndPendingStatus(@Param("step") step: Int): List<WorkflowDetailEntity>

    @Query("SELECT wd FROM WorkflowDetailEntity wd WHERE wd.workflowId = :workflowId AND wd.status IN ('Pending', 'Initial')")
    fun findPendingOrInitialByWorkflowId(@Param("workflowId") workflowId: Long): List<WorkflowDetailEntity>
}