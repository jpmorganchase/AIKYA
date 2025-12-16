package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlModelDefinitionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FlModelDefinitionRepository : JpaRepository<FlModelDefinitionEntity, Long> {
    @Query("from FlModelDefinitionEntity md where md.domain= ?1")
    fun findFlModelDefinitionByDomain(domain: String): Optional<FlModelDefinitionEntity>
    @Query("from FlModelDefinitionEntity md where md.modelName= ?1")
    fun findFlModelDefinitionByName(name: String): Optional<FlModelDefinitionEntity>
}