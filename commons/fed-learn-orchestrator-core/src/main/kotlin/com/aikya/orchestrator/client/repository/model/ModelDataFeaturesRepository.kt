package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelDataFeaturesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ModelDataFeaturesRepository : JpaRepository<ModelDataFeaturesEntity, Long> {
    @Query("SELECT DISTINCT m.domain FROM ModelDataFeaturesEntity m")
    fun findDistinctDomains(): List<String>
}