package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FLAggregateStrategy
import com.aikya.orchestrator.dto.fedlearn.AggregateStrategyDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FLAggregateStrategyRepository : JpaRepository<FLAggregateStrategy, Long> {
    @Query("SELECT new com.aikya.orchestrator.dto.fedlearn.AggregateStrategyDTO(a.id, a.name, a.description, a.func, a.status) FROM com.aikya.orchestrator.aggregate.model.FLAggregateStrategy a")
    fun findAllFLAggregateStrategies(): List<AggregateStrategyDTO>
}