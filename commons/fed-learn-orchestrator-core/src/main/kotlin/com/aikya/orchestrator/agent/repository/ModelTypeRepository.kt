package com.aikya.orchestrator.agent.repository

import com.aikya.orchestrator.agent.model.ModelTypesEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModelTypeRepository: JpaRepository<ModelTypesEntity, Long> {

}