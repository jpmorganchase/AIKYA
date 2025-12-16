package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlRunModelAggregationEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlRunModelAggregationRepository : JpaRepository<FlRunModelAggregationEntity, Long> {
    fun findByModelWeightsIdAndModelId(modelWeightsId: Long, modelId: Long): FlRunModelAggregationEntity
    fun findAllByModelId(modelId: Long): List<FlRunModelAggregationEntity>
}