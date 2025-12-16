package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FLDomainEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FLDomainRepository : JpaRepository<FLDomainEntity, Long> {
}