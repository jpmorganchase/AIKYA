package com.aikya.orchestrator.service

import com.aikya.orchestrator.agent.model.*
import com.aikya.orchestrator.agent.repository.*
import com.aikya.orchestrator.client.model.fedlearn.*
import com.aikya.orchestrator.client.model.seeds.DomainEntity
import com.aikya.orchestrator.client.repository.model.*
import com.aikya.orchestrator.client.repository.seeds.DomainRepository
import com.aikya.orchestrator.dto.common.DomainDTO
import com.aikya.orchestrator.dto.fedlearn.ClientGlobalModelVersion
import com.aikya.orchestrator.dto.fedlearn.GlobalVersionRequest
import com.aikya.orchestrator.repository.client.FLClientQueryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
/**
 * Service class responsible for managing operations related to model definitions,
 * training results, prediction data, feedback, metrics, and versioning.
 */
@Service
class ModelService @Autowired constructor(
    private val modelDefinitionRepository: ModelDefinitionRepository,
    private val agentModelLogsRepository: AgentModelLogsRepository,
    private val modelFeedbackRepository: ModelFeedbackRepository,
    private val globalModelWeightsRepository: GlobalModelWeightsRepository,
    private val globalModelTrainingResultRepository: GlobalModelTrainingResultRepository,
    private val globalMetricsRepository: GlobalMetricsRepository,
    private val metricsRepository: MetricsRepository,
    private val modelTrainingResultRepository: ModelTrainingResultRepository,
    private val modelPredictionDataRepository: ModelPredictionDataRepository,
    private val modelClientRecordRepository: ModelClientRecordRepository,
    private val modelClientRecordHistoryRepository: ModelClientRecordHistoryRepository,
    private val domainRepository: DomainRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(ModelService::class.java)

    @Autowired
    @Qualifier("clientQueryRepository")
    private val clientQueryRepository: FLClientQueryRepository? = null
    /**
     * Retrieves the model definition entity for a given domain.
     * @throws NoSuchElementException if not found.
     */
    fun getModelDefinition(domain: String): ModelDefinitionEntity {
        return modelDefinitionRepository.findModelDefinitionByDomain(domain).orElseThrow()
    }
    /**
     * Finds a model definition for a given domain, or returns null if not found.
     */
    fun findModelDefinition(domain: String): ModelDefinitionEntity {
        return modelDefinitionRepository.findModelDefinitionByDomain(domain).orElse(null)
    }
    /**
     * Retrieves all model definitions.
     */
    fun getAllModelDefinition(): MutableList<ModelDefinitionEntity> {
        return modelDefinitionRepository.findAll()
    }
    /**
     * Retrieves all global model weights associated with a given domain.
     */
    fun findGlobalModelWeightsByModelId(domain: String): List<GlobalModelWeightsEntity> {
        val modelDef = getModelDefinition(domain)
        val modelId = modelDef.id
        return globalModelWeightsRepository.findModelWeightsByModelId(modelId)
    }
    /**
     * Returns all client model records that are from the initial version (version 0).
     */
    fun findInitialModelClientRecordHistory(): List<ModelClientRecordHistoryEntity> {
        return modelClientRecordHistoryRepository.findAll()
            .filter { it.version == 0 }
    }
    /**
     * Finds all model client records for a given name.
     */
    fun findAllModelClientRecordHistoryByName(name: String): List<ModelClientRecordHistoryEntity> {
        return modelClientRecordHistoryRepository.findAllByNameOrderByNameAsc(name)
    }
    /**
     * Retrieves all model client record histories sorted by version.
     */
    fun findAllModelClientRecordHistory(): List<ModelClientRecordHistoryEntity> {
        return modelClientRecordHistoryRepository.findAllByOrderByVersionAsc()
    }
    /**
     * Retrieves all client record history entries for a specific version.
     */
    fun findAllModelClientRecordHistoryByVersion(version: Int): List<ModelClientRecordHistoryEntity> {
        return modelClientRecordHistoryRepository.findByVersion(version)
    }
    /**
     * Finds a specific record by name and version.
     */
    fun findAllModelClientRecordHistoryByNameVersion(
        name: String,
        version: Int
    ): Optional<ModelClientRecordHistoryEntity> {
        return modelClientRecordHistoryRepository.findByNameAndVersion(name, version)
    }

    /**
     * Gets the maximum global version for a model by modelId.
     */
    fun findMaxGlobalModelVersion(modelId: Long): Long? {
        return globalModelWeightsRepository.findMaxVersionByModelId(modelId) ?: 0L
    }
    /**
     * Finds agent model logs by model name.
     */
    fun findAgentModelLogs(modelName: String): AgentModelLogsEntity? {
        val optionalLog = agentModelLogsRepository.findAgentModelLogsByModelName(modelName)
        return optionalLog.orElse(null)
    }
    /**
     * Retrieves all domain entities.
     */
    fun getDomainEntities(): List<DomainEntity> {
        return domainRepository.findAll()
    }
    /**
     * Returns domain DTOs with payment_fraud placed first.
     */
    fun getAllDomains(): List<DomainDTO> {
        val domains = domainRepository.findAll().map { domain ->
            DomainDTO(domain.name, domain.label)
        }
        val paymentFraudDomain = domains.find { it.name == "payment_fraud" }
        val otherDomains = domains.filter { it.name != "payment_fraud" }

        // Return a list with payment_fraud as the first element
        return if (paymentFraudDomain != null) {
            listOf(paymentFraudDomain) + otherDomains
        } else {
            domains
        }
    }
    /**
     * Retrieves the latest global version info for all models.
     */
    fun getAllModelLatestVersions(): List<GlobalVersionRequest> {
        return getAllModelDefinition().map { model ->
            val modelName = model.modelName!!
            val modelLog = findAgentModelLogs(modelName)

            // Use the elvis operator to provide a default version of 0 if modelLog is null
            val version = modelLog?.globalWeightsVersion ?: 0

            GlobalVersionRequest(model.domain!!, version, modelName, model.id)
        }
    }
    /**
     * Finds the latest version info for a model under a given domain.
     */
    fun findModelLatestVersions(domain: String): GlobalVersionRequest? {
        val model = modelDefinitionRepository.findModelDefinitionByDomain(domain).orElse(null)
        return if (model != null) {
            val modelName = model.modelName
            val modelDomain = model.domain
            val modelLog = findAgentModelLogs(modelName!!)
            if (modelLog != null) {
                val version = modelLog.globalWeightsVersion
                GlobalVersionRequest(modelDomain!!, version!!, modelName, model.id)
            } else {
                null
            }
        } else {
            null
        }
    }
    /**
     * Saves a global model weight entity.
     */
    @Transactional
    fun saveGlobalModelWeight(modelWeights: GlobalModelWeightsEntity) {
        globalModelWeightsRepository.save(modelWeights)
    }
    /**
     * Saves a log entry for a model.
     */
    @Transactional
    fun saveAgentModelLogs(modelLog: AgentModelLogsEntity) {
        agentModelLogsRepository.save(modelLog)
    }
    /**
     * Persists a model definition.
     */
    @Transactional
    fun saveModelDefinition(modelDefinition: ModelDefinitionEntity) {
        modelDefinitionRepository.save(modelDefinition)
    }
    /**
     * Verifies if the total number of agent model logs equals a specific count.
     */
    fun hasAgentModelLogs(num: Int): Boolean {

        return agentModelLogsRepository.countAgentModelLogs() == num.toLong()
    }
    /**
     * Gets the number of prediction items for a workflow trace ID.
     */
    fun getPreditionCouns(workflowTraceId: String): Long {
        return modelPredictionDataRepository.predictionItemCounts(workflowTraceId)
    }

    /**
     * Gets the number of prediction items for a workflow trace ID.
     */
    @Transactional
    fun savePredictions(modelPredictionDatas: List<ModelPredictionDataEntity>) {
        modelPredictionDataRepository.saveAll(modelPredictionDatas)
    }

    /**
     * Finds prediction data for a given workflow trace ID.
     */
    fun findModelPredictionByWorkflowTraceId(workflowTraceId: String): ModelPredictionDataEntity {
        return modelPredictionDataRepository.findFirstByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Finds prediction data by its ID.
     */
    fun findModelPredictionById(id: Long): Optional<ModelPredictionDataEntity> {
        return modelPredictionDataRepository.findById(id)
    }
    /**
     * Finds prediction data by a list of IDs and batch ID.
     */
    fun findModelPredictionDataByIds(modelDataIds: List<Long>, batchId: String): List<ModelPredictionDataEntity> {
        return modelPredictionDataRepository.findModelPredictionDataByIds(modelDataIds, batchId)
    }
    /**
     * Retrieves feedback for a given prediction data ID.
     */
    fun findModelFeedbackByModelDataId(modelDataId: Long): Optional<ModelFeedbackEntity> {
        return modelFeedbackRepository.findModelFeedbackByModelDataId(modelDataId)
    }
    /**
     * Saves a list of model feedback entries using a new transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveFeedbacks(modemlFeedbackEntities: List<ModelFeedbackEntity>) {
        modelFeedbackRepository.saveAll(modemlFeedbackEntities)
    }
    /**
     * Saves a model client record using a new transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun saveModelClientRecord(modelClientRecordEntity: ModelClientRecordEntity) {
        modelClientRecordRepository.save(modelClientRecordEntity)
    }
    /**
     * Saves a model client record history entry.
     */
    @Transactional
    fun saveModelClientRecordHistory(modelClientRecordHistory: ModelClientRecordHistoryEntity) {
        modelClientRecordHistoryRepository.save(modelClientRecordHistory)
    }
    /**
     * Finds training results for a specific workflow trace ID.
     */
    fun findModelTrainingResult(workflowTraceId: String): ModelTrainingResultEntity? {
        return modelTrainingResultRepository.findByWorkflowTraceId(workflowTraceId).orElse(null)
    }
    /**
     * Finds a model client record using domain and name.
     */
    fun findModelClientRecordByDomainAndName(domain: String, name: String): ModelClientRecordEntity {
        return modelClientRecordRepository.findByDomainAndName(domain, name)
    }
    /**
     * Finds a model client record using name.
     */
    fun findModelClientRecordByName(name: String): Optional<ModelClientRecordEntity> {
        return modelClientRecordRepository.findByName(name)
    }
    /**
     * Retrieves the client record for a specific domain.
     */
    fun getModelClientRecordByDomain(domain: String): ModelClientRecordEntity? {
        return modelClientRecordRepository.getByDomain(domain).orElse(null)
    }
    /**
     * Retrieves global model logs by model ID.
     */
    fun findGlobalModelLogs(modelId: Long): AgentModelLogsEntity? {
        return agentModelLogsRepository.findByModelId(modelId).orElse(null)
    }
    /**
     * Retrieves the global model version for a given domain.
     */
    fun findGlobalModelLogsByDomain(domain: String): ClientGlobalModelVersion {
        val modelDef = getModelDefinition(domain)
        return findGlobalModelLogsByModelId(modelDef.id)
    }
    /**
     * Retrieves the global model version for a given model ID.
     */
    fun findGlobalModelLogsByModelId(modelId: Long): ClientGlobalModelVersion {
        val modelLog = findGlobalModelLogs(modelId)
        val version = modelLog?.globalWeightsVersion ?: 1 // Default to 1 if version is null
        return ClientGlobalModelVersion(modelId = modelId, globalWeightsVersion = version)
    }
    /**
     * Retrieves evaluation metrics based on workflow trace ID and source.
     */
    fun findMetricsByWorkflowTraceIdAndSource(workflowTraceId: String, source: String): List<Metrics> {
        return metricsRepository.findByWorkflowTraceIdAndSource(workflowTraceId, source)
    }
    /**
     * Groups metrics by workflow trace ID and source into a map.
     */
    fun getMetricsMapByWorkflowTraceIdsAndSource(
        workflowTraceIds: List<String>,
        source: String
    ): HashMap<String, List<Metrics>> {
        val metricsMap = HashMap<String, List<Metrics>>()
        val metricsList = metricsRepository.findByWorkflowTraceIdsAndSource(workflowTraceIds, source)
        workflowTraceIds.forEach { workflowTraceId ->
            val filteredMetrics = metricsList.filter { it.workflowTraceId == workflowTraceId }
            metricsMap[workflowTraceId] = filteredMetrics
        }
        return metricsMap
    }
    /**
     * Saves global metrics.
     */
    @Transactional
    fun saveGlobalMetrics(metrics: GlobalMetrics): GlobalMetrics {
        return globalMetricsRepository.save(metrics)
    }
    /**
     * Saves training results for a global model.
     */
    @Transactional
    fun saveGlobalModelTrainingResult(globalModelTrainingResult: GlobalModelTrainingResult) {
        globalModelTrainingResultRepository.save(globalModelTrainingResult)
    }
    /**
     * Saves a global model weight and returns the saved entity.
     */
    @Transactional
    fun saveGlobalModelWeights(globalModelWeightsEntity: GlobalModelWeightsEntity): GlobalModelWeightsEntity {
        return globalModelWeightsRepository.save(globalModelWeightsEntity)
    }

}