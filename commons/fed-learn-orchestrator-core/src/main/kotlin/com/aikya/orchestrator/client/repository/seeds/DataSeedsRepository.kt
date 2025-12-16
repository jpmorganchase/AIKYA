package com.aikya.orchestrator.client.repository.seeds

import com.aikya.orchestrator.client.model.seeds.DataSeedEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DataSeedsRepository : JpaRepository<DataSeedEntity, Long> {
    @Query("from DataSeedEntity d where d.workflowTraceId= ?1")
    fun findDataSeedsByWorkflowTraceId(workflowTraceId: String): DataSeedEntity

    @Query("from DataSeedEntity d where d.workflowTraceId= ?1")
    fun findOptionalDataSeedsByWorkflowTraceId(workflowTraceId: String): DataSeedEntity?

    @Query("from DataSeedEntity d where d.batchId= ?1 and d.workflowTraceId=?2")
    fun findDataSeed(batchId: String, workflowTraceId: String): DataSeedEntity

    @Query("from DataSeedEntity d where d.batchId= ?1")
    fun findDataSeedsByBatchId(batchId: String): DataSeedEntity

    @Query("from DataSeedEntity d where d.status= 'Initial'")
    fun findAllInitialDataSeeds(): List<DataSeedEntity>

    fun findDataSeedsByWorkflowTraceIdIn(workflowTraceIds: List<String>): List<DataSeedEntity>

    fun findByFileName(fileName: String): List<DataSeedEntity>

    @Query("SELECT DISTINCT ds.fileName FROM DataSeedEntity ds WHERE ds.domainType = :domainType ")
    fun findDistinctFileNamesByDomainType(@Param("domainType") domainType: String): List<String>

    @Query("SELECT DISTINCT ds.fileName FROM DataSeedEntity ds WHERE ds.domainType = :domainType AND ds.status <> 'RESET'")
    fun findDistinctFileNamesByDomainTypeAndExcludeReset(@Param("domainType") domainType: String): List<String>

    @Query("FROM DataSeedEntity ds WHERE ds.domainType = :domainType")
    fun getAllDataSeedsByDomainType(@Param("domainType") domainType: String): List<DataSeedEntity>

    @Query("SELECT d FROM DataSeedEntity d WHERE d.fileName = :fileName AND d.domainType = :domainType AND d.createdDate < :currentCreatedDate ORDER BY d.createdDate DESC")
    fun findPreviousByFileNameAndDomainTypeAndCreatedDate(
        @Param("fileName") fileName: String,
        @Param("domainType") domainType: String,
        @Param("currentCreatedDate") currentCreatedDate: Date
    ): List<DataSeedEntity>
}