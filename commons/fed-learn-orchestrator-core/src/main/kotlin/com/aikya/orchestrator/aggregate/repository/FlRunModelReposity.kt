package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlRunModelEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FlRunModelReposity: JpaRepository<FlRunModelEntity, Long> {
    @Query("from FlRunModelEntity rm where rm.name= ?1")
    fun findFlRunModel(name: String): FlRunModelEntity

    @Query("from FlRunModelEntity rm where rm.modelId= ?1")
    fun findFlRunModelByModelId(modelId: Long): FlRunModelEntity
}