package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.ClientRunModeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ClientRunModeRepository : JpaRepository<ClientRunModeEntity, Long> {
    fun findByModeAndName(mode: String, name: String): List<ClientRunModeEntity>
    fun findByDomainAndName(domain: String, name: String): ClientRunModeEntity
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN TRUE ELSE FALSE END FROM ClientRunModeEntity w WHERE w.domain = :domain AND w.name = :name AND w.mode = 'auto'")
    fun isAutoRunModel(@Param("domain") domain: String, @Param("name") name: String): Boolean
}