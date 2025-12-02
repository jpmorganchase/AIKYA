package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FLModelClientTrainingResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FLModelClientTrainingResultRepository : JpaRepository<FLModelClientTrainingResult, Long> {

    @Query("from FLModelClientTrainingResult mr where mr.workflowTraceId= ?1")
    fun findByWorkflowTraceId(workflowTraceId: String): List<FLModelClientTrainingResult>

    fun findAllByModelId(modelId: Long): List<FLModelClientTrainingResult>

    fun findAllByClientWorkflowTraceIdIn(clientWorkflowTraceIds: List<String>): List<FLModelClientTrainingResult>
}