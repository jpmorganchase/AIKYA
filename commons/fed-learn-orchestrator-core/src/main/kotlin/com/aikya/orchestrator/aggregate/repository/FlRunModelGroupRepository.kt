package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlRunModelGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FlRunModelGroupRepository: JpaRepository<FlRunModelGroupEntity, Long> {
    // Find if any group with the given runModelId exists
    fun existsByRunModelId(runModelId: Long): Boolean
    // Find all groups by runModelId where seqNum is less than participantsNumber
    @Query("SELECT g FROM FlRunModelGroupEntity g WHERE g.runModelId = :runModelId AND g.seqNum < :participantsNumber")
    fun findAllByRunModelIdAndSeqNumLessThan(runModelId: Long, participantsNumber: Int): List<FlRunModelGroupEntity>
    fun findByRunModelIdAndClientId(runModelId: Long, clientId: String): FlRunModelGroupEntity?
    fun findByRunModelIdAndGroupHash(runModelId: Long, groupHash: String): List<FlRunModelGroupEntity>
}