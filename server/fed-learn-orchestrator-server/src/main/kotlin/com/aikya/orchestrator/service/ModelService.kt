package com.aikya.orchestrator.service

import com.aikya.orchestrator.aggregate.model.*
import com.aikya.orchestrator.aggregate.repository.*
import com.aikya.orchestrator.dto.common.DomainDTO
import com.aikya.orchestrator.dto.fedlearn.AggregateStrategyDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
/**
 * Service class responsible for managing federated learning model-related data.
 *
 * This includes:
 * - Model definition and metadata retrieval
 * - Client training results and collaboration run management
 * - Aggregation results, metrics, weights versioning
 * - CRUD operations on metrics and training entities
 */
@Service
class ModelService @Autowired constructor(
    private val modelClientTrainingResultRepository: FLModelClientTrainingResultRepository,
    private val modelDefinitionRepository: FlModelDefinitionRepository,
    private val metricsRepository: FlMetricsRepository,
    private val runModelReposity: FlRunModelReposity,
    private val modelCollaborationRunRepository: FLModelCollaborationRunRepository,
    private val collaborationRunClientRepository: FlCollaborationRunClientRepository,
    private val flAggregateStrategyRepository: FLAggregateStrategyRepository,
    private val clientRepository: FlClientRepository,
    private val domainRepository: FLDomainRepository,
    private val globalModelWeightsRepository: FLGlobalModelWeightsRepository,
    private val runModelAggregationRepository: FlRunModelAggregationRepository,
    private val modelAggregateWeightsRepository: FlModelAggregateWeightsRepository
    ) {
    private val logger: Logger = LoggerFactory.getLogger(ModelService::class.java)
    /**
     * Retrieves all domain definitions, sorted with "payment_fraud" (if available) first.
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
     * Finds a client by their integer client ID.
     */
    fun findByClientId(clientId: Int): FlClient {
        return clientRepository.findByClientId(clientId)
    }
    /**
     * Retrieves all available aggregation strategies.
     */
    fun getAggregateStrategies(): List<AggregateStrategyDTO> {
        return flAggregateStrategyRepository.findAllFLAggregateStrategies()
    }
    /**
     * Finds a model definition by domain name.
     */
    fun findModelDefinitionByDomain(domain: String): FlModelDefinitionEntity {
        return modelDefinitionRepository.findFlModelDefinitionByDomain(domain).orElse(null)
    }
    /**
     * Returns an optional model definition by model name.
     */
    fun findOptionalModelDefinitionByName(modelName: String): FlModelDefinitionEntity? {
        return modelDefinitionRepository.findFlModelDefinitionByName(modelName).orElse(null)
    }
    /**
     * Finds a model definition by model name.
     */
    fun findModelDefinitionByName(modelName: String): FlModelDefinitionEntity {
        return modelDefinitionRepository.findFlModelDefinitionByName(modelName).orElse(null)
    }
    /**
     * Finds collaboration run clients by client ID and workflow trace ID.
     */
    fun findCollaborationRunClientByClientId(clientId: Long, workflowTraceId: String): List<FlCollaborationRunClient> {
        return collaborationRunClientRepository.findByClientIdAndWorkflowTraceId(clientId, workflowTraceId)
    }
    /**
     * Retrieves all client training results by workflow trace ID.
     */
    fun findModelClientTrainingsByWorkflowTraceId(workflowTraceId: String): List<FLModelClientTrainingResult> {
        return modelClientTrainingResultRepository.findByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Retrieves collaboration run clients by workflow trace ID.
     */
    fun getModelClientsByWorkflowTraceId(workflowTraceId: String): List<FlCollaborationRunClient> {
        return collaborationRunClientRepository.findByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Finds training results by a list of client workflow trace IDs.
     */
    fun findModelClientTrainingResults(clientWorkflowTraceIds: List<String>): List<FLModelClientTrainingResult> {
       return modelClientTrainingResultRepository.findAllByClientWorkflowTraceIdIn(clientWorkflowTraceIds)
    }
    /**
     * Finds all training results for a given model name.
     */
    fun findAllModelClientByModel(modelName: String): List<FLModelClientTrainingResult> {
        val modelDefinition = findModelDefinitionByName(modelName)
        val modelId = modelDefinition.id
        return modelClientTrainingResultRepository.findAllByModelId(modelId)
    }
    /**
     * Retrieves all model aggregations for a given model name.
     */
    fun findAllModelAggregationsByModelName(modelName: String): List<FlRunModelAggregationEntity> {
        val modelDefinition = findModelDefinitionByName(modelName)
        val modelId = modelDefinition.id
        return runModelAggregationRepository.findAllByModelId(modelId)
    }
    /**
     * Finds a federated run model entity by name.
     */
    fun findFlRunModel(name: String): FlRunModelEntity {
        return runModelReposity.findFlRunModel(name)
    }
    /**
     * Returns a list of all FL clients.
     */
    fun findAllClients(): List<FlClient> {
        return clientRepository.findAll()
    }
    /**
     * Retrieves collaboration clients by workflow trace ID.
     */
    fun findCollaborationRunClient(workflowTraceId: String): List<FlCollaborationRunClient> {
        return collaborationRunClientRepository.findByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Finds a model collaboration run by ID.
     */
    fun findModelCollaborationRunById(id: Long): Optional<FLModelCollaborationRun> {
        return modelCollaborationRunRepository.findById(id)
    }
    /**
     * Finds collaboration runs by run model ID and model ID, limited to PENDING or INITIAL.
     */
    fun findModelCollaborationRunByRunModel(runModelId: Long, modelId: Long): List<FLModelCollaborationRun> {
        return modelCollaborationRunRepository.findPendingOrInitialByRunModelIdAndModelId(runModelId, modelId)
    }
    /**
     * Finds specific aggregate weights by model ID and version.
     */
    fun findModelAggregateWeights(modelId: Long, version: Long): Optional<FlModelAggregateWeights> {
        return modelAggregateWeightsRepository.findByModelIdAndVersion(modelId, version)
    }
    /**
     * Retrieves the max version weights available for a model.
     */
    fun getMaxWeightsVersionForModelId(modelId: Long): FlModelAggregateWeights? {
        return modelAggregateWeightsRepository.findMaxVersionEntityByModelId(modelId)
    }
    /**
     * Finds weight versions in a given version range.
     */
    fun findWeightsVersionsBetween(modelId: Long, startVersion: Long, endVersion: Long): List<FlModelAggregateWeights> {
        return modelAggregateWeightsRepository.findVersionsBetween(modelId, startVersion, endVersion)
    }
    /**
     * Finds aggregate weights by workflow trace ID and model ID.
     */
    fun findModelAggregateWeightsByWorkflowTraceId(workflowTraceId: String, modelId: Long): Optional<FlModelAggregateWeights> {
        return modelAggregateWeightsRepository.findByWorkflowTraceIdAndModelId(workflowTraceId, modelId)
    }
    /**
     * Finds aggregate weights by workflow trace ID only.
     */
    fun findModelAggregateWeightsByWorkflowTraceId(workflowTraceId: String): Optional<FlModelAggregateWeights> {
        return modelAggregateWeightsRepository.findByWorkflowTraceId(workflowTraceId)
    }
    /**
     * Finds optional global model weights by model ID.
     */
    fun findOptionalGlobalModelWeightsByModelId(modelId: Long): FLGlobalModelWeights? {
        return globalModelWeightsRepository.findOptionalByModelId(modelId).orElse(null)
    }
    /**
     * Retrieves metrics for a given workflow trace ID and source.
     */
    fun findFlMetricsByWorkflowTraceIdAndSource(workflowTraceId: String, source: String): List<FlMetrics> {
        return metricsRepository.findByWorkflowTraceIdAndSource(workflowTraceId, source)
    }
    /**
     * Retrieves a map of workflow trace ID -> metrics list for the given IDs and source.
     */
    fun getMetricsMapByWorkflowTraceIdsAndSource(workflowTraceIds: List<String>, source: String): HashMap<String, List<FlMetrics>> {
        val metricsMap = HashMap<String, List<FlMetrics>>()
        val metricsList = metricsRepository.findByWorkflowTraceIdsAndSource(workflowTraceIds, source)
        workflowTraceIds.forEach { workflowTraceId ->
            val filteredMetrics = metricsList.filter { it.workflowTraceId == workflowTraceId }
            metricsMap[workflowTraceId] = filteredMetrics
        }
        return metricsMap
    }
    /**
     * Finds aggregation record by model weights ID and model ID.
     */
    fun runModelAggregationRepository(modelWeightsId: Long, modelId: Long): FlRunModelAggregationEntity {
        return runModelAggregationRepository.findByModelWeightsIdAndModelId(modelWeightsId, modelId)
    }
    /**
     * Retrieves all other collaboration clients from a run excluding one workflow trace.
     */
    fun findAllOtherCollaborationRunClients(runId: Long, workflowTraceIds: String, clientId: Long): List<FlCollaborationRunClient> {
        return collaborationRunClientRepository.findByRunIdAndExcludeWorkflowTraceIdOrderByRounds(runId, workflowTraceIds, clientId)
    }
    /**
     * Finds stalled clients based on inactivity.
     */
    fun findPendingStalledClients(someMinutesAgo: Date): List<FlCollaborationRunClient> {
        return collaborationRunClientRepository.findPendingStalledClients(someMinutesAgo)
    }
    /**
     * Finds a specific collaboration run client by run ID, client ID, and round.
     */
    fun findByRunIdAndClientIdAndRound(runId: Long, clientId: Long, round: Int): FlCollaborationRunClient? {
        return collaborationRunClientRepository.findByRunIdAndClientIdAndRound(runId, clientId, round)
    }
    /**
     * Finds pending or initial collaboration runs.
     */
    @Transactional(readOnly = true)
    fun findPendingOrInitialRuns(runModelId: Long, modelId: Long): List<FLModelCollaborationRun> {
        return modelCollaborationRunRepository.findPendingOrInitialByRunModelIdAndModelId(runModelId, modelId)
    }
    /**
     * Persists a model client training result to the DB.
     */
    @Transactional
    fun saveModelClientTrainingResult(modelClientTrainingResult: FLModelClientTrainingResult): FLModelClientTrainingResult {
        return modelClientTrainingResultRepository.save(modelClientTrainingResult)
    }

    /**
     * Persists a collaboration run client.
     */
    @Transactional
    fun saveCollaborationRunClient(collaborationRunClient: FlCollaborationRunClient): FlCollaborationRunClient {
        return collaborationRunClientRepository.save(collaborationRunClient)
    }
    /**
     * Persists a model collaboration run.
     */
    @Transactional
    fun saveModelCollaborationRun(modelCollaborationRun: FLModelCollaborationRun): FLModelCollaborationRun {
        return modelCollaborationRunRepository.save(modelCollaborationRun)
    }
    /**
     * Persists a metrics entry.
     */
    @Transactional
    fun saveMetrics(metrics: FlMetrics): FlMetrics {
        return metricsRepository.save(metrics)
    }
    /**
     * Persists multiple metrics in batch.
     */
    @Transactional
    fun saveAllMetrics(metricsList: List<FlMetrics>): List<FlMetrics> {
        return metricsRepository.saveAll(metricsList)
    }

}