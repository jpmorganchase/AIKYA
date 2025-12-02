package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelTrainingResultEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ModelTrainingResultRepository : JpaRepository<ModelTrainingResultEntity, Long> {
    fun findByWorkflowTraceId(workflowTraceId: String): Optional<ModelTrainingResultEntity>
}