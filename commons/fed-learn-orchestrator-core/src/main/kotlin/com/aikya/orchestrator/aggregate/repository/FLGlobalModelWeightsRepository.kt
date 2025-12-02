package com.aikya.orchestrator.aggregate.repository
import com.aikya.orchestrator.aggregate.model.FLGlobalModelWeights
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface  FLGlobalModelWeightsRepository: JpaRepository<FLGlobalModelWeights, Long> {
    fun findByModelId(modelId: Long): FLGlobalModelWeights
    fun findOptionalByModelId(modelId: Long): Optional<FLGlobalModelWeights>
}