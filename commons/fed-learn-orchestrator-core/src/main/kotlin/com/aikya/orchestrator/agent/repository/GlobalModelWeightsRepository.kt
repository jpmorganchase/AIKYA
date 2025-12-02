package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.GlobalModelWeightsEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GlobalModelWeightsRepository : JpaRepository<GlobalModelWeightsEntity, Long> {
    @Query("from GlobalModelWeightsEntity mw where mw.modelId= ?1")
    fun findModelWeightsByModelId(modelId: Long): List<GlobalModelWeightsEntity>
    @Query("from GlobalModelWeightsEntity mw where mw.workflowTraceId= ?1")
    fun findModelWeightsByWorkflowTraceId(workflowTraceId: String): Optional<GlobalModelWeightsEntity>
    @Query("from GlobalModelWeightsEntity mw where mw.workflowTraceId in ?1 order by mw.version desc")
    fun findModelWeightsByWorkflowTraceIds(workflowTraceIds: List<String>): List<GlobalModelWeightsEntity>
    @Query("select count(mw) > 0 from GlobalModelWeightsEntity mw")
    fun existsAny(): Boolean
    @Query("select max(mw.version) from GlobalModelWeightsEntity mw where mw.modelId = ?1")
    fun findMaxVersionByModelId(modelId: Long): Long?
}