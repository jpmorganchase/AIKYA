package com.aikya.orchestrator.service

import com.aikya.orchestrator.client.model.seeds.DataSeedEntity
import com.aikya.orchestrator.client.model.seeds.DataSeedMetaDataEntity
import com.aikya.orchestrator.client.repository.seeds.DataSeedMetaDataRepository
import com.aikya.orchestrator.client.repository.seeds.DataSeedsRepository
import com.aikya.orchestrator.dto.seeds.DataSeedMetaData
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
/**
 * Service responsible for managing data seeds and associated metadata
 * for anomaly detection or model training in a federated learning context.
 *
 * Provides methods to retrieve, group, and persist seed data based on domain, file name,
 * workflow trace ID, and batch ID.
 *
 * @property dataSeedsRepository Repository to access and manipulate `DataSeedEntity` records.
 * @property dataSeedMetaDataRepository Repository to manage `DataSeedMetaDataEntity` records.
 */
@Service
class DataSeedsService @Autowired constructor(
    private val dataSeedsRepository: DataSeedsRepository,
    private val dataSeedMetaDataRepository: DataSeedMetaDataRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(DataSeedsService::class.java)

    /**
     * Retrieves initial metadata entries for data seeds that have not yet been loaded.
     *
     * @param domainType The domain type to filter data seeds by (e.g., "payment_fraud").
     * @return A list of `DataSeedMetaData` entries that are not yet loaded.
     */
    fun getInitialDataSeeds(domainType: String): List<DataSeedMetaData> {
        val allDataSeedMetaDatas = dataSeedMetaDataRepository.findAllDataSeedMetaData(domainType)
        val loadedFiles = getNoResetDistinctFileNamesByDomainType(domainType)
        val displayDataSeedList = mutableListOf<DataSeedMetaData>()
        for (row in allDataSeedMetaDatas) {
            if (row.fileName !in loadedFiles) {
                val dataSeedMetaData = DataSeedMetaData(
                    row.id, row.fileName!!, row.label!!, row.anomalyDesc!!, row.domainType!!,
                )
                displayDataSeedList.add(dataSeedMetaData)
            }
        }
        return displayDataSeedList
    }
    /**
     * Finds the metadata associated with a given batch ID.
     *
     * @param batchId The batch ID of the seed.
     * @return An optional result containing the metadata if found.
     */
    fun findDataSeedMetaDataByBatchId(batchId: String): Optional<DataSeedMetaDataEntity> {
        return dataSeedMetaDataRepository.findDataSeedMetaDataByBatchId(batchId)
    }
    /**
     * Retrieves a list of distinct seed file names for the specified domain.
     *
     * @param domainType The domain type to filter by.
     * @return List of distinct file names.
     */
    fun getDistinctFileNamesByDomainType(domainType: String): List<String> {
        return dataSeedsRepository.findDistinctFileNamesByDomainType(domainType)
    }
    /**
     * Retrieves distinct seed file names excluding reset batches.
     *
     * @param domainType The domain type to filter by.
     * @return List of non-reset file names.
     */
    fun getNoResetDistinctFileNamesByDomainType(domainType: String): List<String> {
        return dataSeedsRepository.findDistinctFileNamesByDomainTypeAndExcludeReset(domainType)
    }
    /**
     * Retrieves all data seeds matching the given file name.
     *
     * @param fileName The name of the file to retrieve seeds for.
     * @return List of `DataSeedEntity` records.
     */
    fun getAllDataSeedsByFileName(fileName: String): List<DataSeedEntity> {
        return dataSeedsRepository.findByFileName(fileName)
    }
    /**
     * Retrieves all data seeds for a given domain type.
     *
     * @param domainType The domain type to filter by.
     * @return List of `DataSeedEntity` records.
     */
    fun getAllDataSeedsByDomainType(domainType: String): List<DataSeedEntity> {
        return dataSeedsRepository.getAllDataSeedsByDomainType(domainType)
    }
    /**
     * Groups all data seeds by file name for the given domain type.
     *
     * @param domainType The domain type to filter by.
     * @return A map of file name to list of seeds.
     */
    fun getDataSeedsGroupedByFileName(domainType: String): Map<String, List<DataSeedEntity>> {
        val dataSeeds = dataSeedsRepository.getAllDataSeedsByDomainType(domainType)
        return dataSeeds.groupBy { it.fileName ?: "" }
    }
    /**
     * Persists a single `DataSeedEntity` record.
     *
     * @param dataSeedEntity The data seed to save.
     */
    @Transactional
    fun save(dataSeedEntity: DataSeedEntity) {
        dataSeedsRepository.save(dataSeedEntity)
    }
    /**
     * Persists a `DataSeedMetaDataEntity` record.
     *
     * @param dataSeedMetaDataEntity The metadata record to save.
     */
    @Transactional
    fun saveDataSeedMetaData(dataSeedMetaDataEntity: DataSeedMetaDataEntity) {
        dataSeedMetaDataRepository.save(dataSeedMetaDataEntity)
    }
    /**
     * Finds all data seeds whose workflow trace ID is in the given list.
     *
     * @param workflowTraceIds List of workflow trace IDs.
     * @return List of matching `DataSeedEntity` records.
     */
    fun findDataSeedsByWorkflowTraceIdIn(workflowTraceIds: List<String>): List<DataSeedEntity> {
        return dataSeedsRepository.findDataSeedsByWorkflowTraceIdIn(workflowTraceIds)
    }
    /**
     * Finds a data seed by its batch ID.
     *
     * @param batchId The batch ID.
     * @return The `DataSeedEntity` if found.
     */
    fun findDataSeedsByBatchId(batchId: String): DataSeedEntity {
        return dataSeedsRepository.findDataSeedsByBatchId(batchId)
    }
    /**
     * Finds a data seed matching a specific batch ID and workflow trace ID.
     *
     * @param batchId The batch ID.
     * @param workflowTraceId The workflow trace ID.
     * @return The matching `DataSeedEntity`.
     */
    fun findDataSeed(batchId: String, workflowTraceId: String): DataSeedEntity {
        return dataSeedsRepository.findDataSeed(batchId, workflowTraceId)
    }
    /**
     * Finds a data seed by its workflow trace ID.
     *
     * @param workflowTraceId The workflow trace ID.
     * @return The matching `DataSeedEntity`.
     */
    fun findDataSeedByWorkflowTraceId(workflowTraceId: String): DataSeedEntity {
        return dataSeedsRepository.findDataSeedsByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Optionally finds a data seed by its workflow trace ID.
     *
     * @param workflowTraceId The workflow trace ID.
     * @return The matching `DataSeedEntity` or null.
     */
    fun findOptionalDataSeedByWorkflowTraceId(workflowTraceId: String): DataSeedEntity? {
        return dataSeedsRepository.findOptionalDataSeedsByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Finds the previous data seed in time before the given batch,
     * based on the same file name and domain type.
     *
     * @param batchId The current batch ID.
     * @return The previous `DataSeedEntity`, or null if not found.
     */
    fun findPreviousDataSeed(batchId: String): DataSeedEntity? {
        val currentDataSeed = dataSeedsRepository.findDataSeedsByBatchId(batchId)
        // Find the previous DataSeedEntity based on fileName, domainType, and createdDate
        val previousEntities = dataSeedsRepository.findPreviousByFileNameAndDomainTypeAndCreatedDate(
            currentDataSeed.fileName!!, currentDataSeed.domainType!!, currentDataSeed.createdDate!!
        )
        return previousEntities.firstOrNull()
    }
}