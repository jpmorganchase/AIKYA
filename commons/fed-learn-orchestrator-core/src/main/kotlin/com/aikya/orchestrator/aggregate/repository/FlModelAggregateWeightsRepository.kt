package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlModelAggregateWeights
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FlModelAggregateWeightsRepository : JpaRepository<FlModelAggregateWeights, Long> {
    fun findByModelIdAndVersion(modelId: Long, version: Long): Optional<FlModelAggregateWeights>
    fun findByWorkflowTraceId(workflowTraceId: String): Optional<FlModelAggregateWeights>
    fun findByWorkflowTraceIdAndModelId(workflowTraceId: String, modelId: Long): Optional<FlModelAggregateWeights>
    @Query("SELECT f FROM FlModelAggregateWeights f WHERE f.modelId = :modelId AND f.version = (SELECT MAX(f2.version) FROM FlModelAggregateWeights f2 WHERE f2.modelId = :modelId)")
    fun findMaxVersionEntityByModelId(@Param("modelId") modelId: Long): FlModelAggregateWeights?
    @Query("SELECT f FROM FlModelAggregateWeights f WHERE f.modelId = :modelId AND f.version BETWEEN :startVersion AND :endVersion")
    fun findVersionsBetween(@Param("modelId") modelId: Long, @Param("startVersion") startVersion: Long, @Param("endVersion") endVersion: Long): List<FlModelAggregateWeights>
}