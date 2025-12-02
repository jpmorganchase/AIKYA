package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.ModelDefinitionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ModelDefinitionRepository : JpaRepository<ModelDefinitionEntity, Long> {
    @Query("from ModelDefinitionEntity md where md.domain= ?1")
    fun findModelDefinitionByDomain(domain: String): Optional<ModelDefinitionEntity>
    @Query("select count(md) > 0 from ModelDefinitionEntity md")
    fun existsAny(): Boolean
}