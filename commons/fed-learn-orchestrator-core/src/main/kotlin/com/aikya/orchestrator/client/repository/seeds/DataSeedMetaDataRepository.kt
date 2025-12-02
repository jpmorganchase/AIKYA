package com.aikya.orchestrator.client.repository.seeds

import com.aikya.orchestrator.client.model.seeds.DataSeedMetaDataEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DataSeedMetaDataRepository: JpaRepository<DataSeedMetaDataEntity, Long> {
    @Query("from DataSeedMetaDataEntity d where d.domainType= ?1")
    fun findAllDataSeedMetaData(domain: String): List<DataSeedMetaDataEntity>

    @Query("from DataSeedMetaDataEntity d where d.batchId= ?1")
    fun findDataSeedMetaDataByBatchId(batchId: String): Optional<DataSeedMetaDataEntity>
}