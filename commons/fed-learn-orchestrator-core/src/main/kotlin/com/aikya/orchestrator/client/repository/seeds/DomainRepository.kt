package com.aikya.orchestrator.client.repository.seeds

import com.aikya.orchestrator.client.model.seeds.DomainEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DomainRepository : JpaRepository<DomainEntity, Long> {
}