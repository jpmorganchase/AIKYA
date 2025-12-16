package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelPredictionDataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ModelPredictionDataRepository: JpaRepository<ModelPredictionDataEntity, Long> {
    fun findFirstByWorkflowTraceId(workflowTraceId: String): ModelPredictionDataEntity
    @Query("from ModelPredictionDataEntity mpd where mpd.id in ?1 and mpd.batchId= ?2")
    fun findModelPredictionDataByIds(modelDataIds: List<Long>, batchId: String): List<ModelPredictionDataEntity>
    @Query("select count(*) from model_predict_data where workflow_trace_id =?1", nativeQuery = true)
    fun predictionItemCounts(workflowTraceId: String): Long
}