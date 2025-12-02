package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FLModelCollaborationRun
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FLModelCollaborationRunRepository : JpaRepository<FLModelCollaborationRun, Long> {

    @Query("from FLModelCollaborationRun f where f.runModelId = ?1 AND f.model.id = ?2")
    fun findPendingOrInitialByRunModelIdAndModelId(runModelId: Long, modelId: Long):List<FLModelCollaborationRun>
}