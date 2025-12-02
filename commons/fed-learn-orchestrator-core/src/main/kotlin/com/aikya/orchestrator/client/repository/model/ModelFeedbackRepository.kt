package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelFeedbackEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ModelFeedbackRepository  : JpaRepository<ModelFeedbackEntity, Long> {
    @Query("from ModelFeedbackEntity d where d.workflowTraceId= ?1")
    fun findModelFeedbackByWorkflowTraceId(workflowTraceId: String): Optional<ModelFeedbackEntity>
    @Query("from ModelFeedbackEntity mf where mf.modelDataId = ?1")
    fun findModelFeedbackByModelDataId(modelDataId: Long): Optional<ModelFeedbackEntity>
}