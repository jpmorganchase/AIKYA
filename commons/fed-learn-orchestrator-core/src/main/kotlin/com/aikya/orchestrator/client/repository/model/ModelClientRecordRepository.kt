package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelClientRecordEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ModelClientRecordRepository : JpaRepository<ModelClientRecordEntity, Long> {
    fun findByDomainAndName(domain: String, name: String): ModelClientRecordEntity
    fun getByDomainAndName(domain: String, name: String): Optional<ModelClientRecordEntity>
    fun getByDomain(domain: String): Optional<ModelClientRecordEntity>
    fun findByName(name: String): Optional<ModelClientRecordEntity>
}