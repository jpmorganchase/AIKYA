package com.aikya.orchestrator.aggregate.repository

import com.aikya.orchestrator.aggregate.model.FlClient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FlClientRepository: JpaRepository<FlClient, Long> {
    @Query("SELECT f FROM FlClient f WHERE f.clientId = :clientId OR f.clientEmail = :clientEmail")
    fun findByClientIdOrClientEmail(clientId: Int, clientEmail: String): FlClient?
    @Query("SELECT f FROM FlClient f WHERE f.clientId = :clientId")
    fun findByClientId(clientId: Int): FlClient
}