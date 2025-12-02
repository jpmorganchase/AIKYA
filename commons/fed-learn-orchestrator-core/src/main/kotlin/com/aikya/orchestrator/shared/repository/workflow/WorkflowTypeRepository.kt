package com.aikya.orchestrator.shared.repository.workflow

import com.aikya.orchestrator.shared.model.workflow.WorkflowTypeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WorkflowTypeRepository : JpaRepository<WorkflowTypeEntity, Long> {
    @Query("from WorkflowTypeEntity wf where wf.name= ?1")
    fun findWorkflowTypeByName(name: String): Optional<WorkflowTypeEntity>
}