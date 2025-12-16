package com.aikya.orchestrator.client.repository.model

import com.aikya.orchestrator.client.model.fedlearn.ModelClientRecordHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ModelClientRecordHistoryRepository : JpaRepository<ModelClientRecordHistoryEntity, Long> {
    fun findAllByOrderByVersionAsc(): List<ModelClientRecordHistoryEntity>
    fun findByVersion(version: Int): List<ModelClientRecordHistoryEntity>
    fun findByNameAndVersion(name: String, version: Int): Optional<ModelClientRecordHistoryEntity>
    fun findAllByNameOrderByNameAsc(name: String): List<ModelClientRecordHistoryEntity>
}