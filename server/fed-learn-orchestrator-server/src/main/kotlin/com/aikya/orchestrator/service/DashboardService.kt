package com.aikya.orchestrator.service

import com.aikya.orchestrator.aggregate.model.FlRunModelGroupEntity
import com.aikya.orchestrator.aggregate.repository.FlRunModelGroupRepository
import com.aikya.orchestrator.dto.common.*
import com.aikya.orchestrator.dto.fedlearn.AggregateStrategyDTO
import com.aikya.orchestrator.dto.fedlearn.FlRunModelGroupRequest
import com.aikya.orchestrator.dto.fedlearn.RunModelGroupRegisterResponse
import com.aikya.orchestrator.dto.workflow.Node
import com.aikya.orchestrator.dto.workflow.WorkflowNetworkSummary
import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.utils.AppConstants.SERVER
import com.aikya.orchestrator.utils.AppUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
/**
 * Service class providing dashboard-related features, including domain listing,
 * strategy listing, run model group registration, workflow summary generation,
 * contribution analysis, and performance statistics for federated learning models.
 */
@Service
class DashboardService @Autowired constructor(
    private val modelService: ModelService,
    private val workflowService: WorkflowService,
    private val flRunModelGroupRepository: FlRunModelGroupRepository
) {
    private val DEFAULTTAG = "fl-0.png"
    private val logger: Logger = LoggerFactory.getLogger(DashboardService::class.java)
    /**
     * Retrieves all domain metadata from the model service.
     *
     * @return List of [DomainDTO] representing available model domains.
     */
    fun getAllDomains(): List<DomainDTO> {
        return modelService.getAllDomains()
    }
    /**
     * Retrieves all configured aggregate strategies.
     *
     * @return List of [AggregateStrategyDTO] used in model evaluation/aggregation.
     */
    fun getAggregateStrategies(): List<AggregateStrategyDTO> {
        return modelService.getAggregateStrategies()
    }
    /**
     * Registers a client to participate in a federated learning run model group.
     * Prevents duplicate registrations and assigns appropriate sequence numbers.
     *
     * @param runModelGroupRequest The client registration request.
     * @return [RunModelGroupRegisterResponse] indicating success, group hash, and message.
     */
    @Transactional
    fun registerRunModelGroup(runModelGroupRequest: FlRunModelGroupRequest): RunModelGroupRegisterResponse {
        val response = RunModelGroupRegisterResponse()
        val name = runModelGroupRequest.name!!
        val clientId = runModelGroupRequest.clientId!!
        val runModel = modelService.findFlRunModel(name)
        val numberOfParticipants = runModel.participantsNumber!!
        val runModelId = runModel.id
        val existingGroup = flRunModelGroupRepository.findByRunModelIdAndClientId(runModelId, clientId)
        val runModelGroup: FlRunModelGroupEntity?

        if (existingGroup != null) {
            logger.warn("Group already exists! skip register")
            response.success = false
            response.message = "Group already exists! skip register"
            runModelGroup = existingGroup
        } else {
            runModelGroup = if (!flRunModelGroupRepository.existsByRunModelId(runModelId)) {
                createNewFlRunModelGroup(runModelId, clientId)
            } else {
                val runModelGroups = flRunModelGroupRepository
                    .findAllByRunModelIdAndSeqNumLessThan(runModelId, numberOfParticipants)

                if (runModelGroups.isNotEmpty()) {
                    val existing = runModelGroups[0]
                    val seqNum = existing.seqNum + 1
                    val groupHash = existing.groupHash

                    val newGroup = FlRunModelGroupEntity(
                        runModelId = runModelId,
                        groupHash = groupHash,
                        clientId = clientId,
                        seqNum = seqNum,
                        createdDate = AppUtils.getCurrent()!!
                    )
                    flRunModelGroupRepository.save(newGroup)
                } else {
                    createNewFlRunModelGroup(runModelId, clientId)
                }
            }

            response.success = true
            response.message = "registered run model group: clientID: $clientId"
        }
        response.groupHash = runModelGroup.groupHash
        response.clientId = clientId
        return response
    }
    /**
     * Creates a new run model group entry for a client, assigning a new group hash and seq number = 1.
     *
     * @param runModelId The ID of the federated learning model run.
     * @param clientId The ID of the registering client.
     * @return The created [FlRunModelGroupEntity].
     */
    @Transactional
    fun createNewFlRunModelGroup(runModelId: Long, clientId: String): FlRunModelGroupEntity {
        // Create a new groupHash
        val now = AppUtils.getCurrent()!!
        val newGroupHash = AppUtils.getUuid8()
        logger.warn("create new RunModelGroup: runModelId: $runModelId, clientId: $clientId groupHash: $newGroupHash")
        val newGroup = FlRunModelGroupEntity(
            runModelId = runModelId,
            groupHash = newGroupHash,
            clientId = clientId,
            seqNum = 1,
            createdDate = now
        )
        flRunModelGroupRepository.save(newGroup)
        return newGroup
    }
    /**
     * Generates a [WorkflowNetworkSummary] for a given model name, visualizing the
     * current workflow state, participating nodes, and model version/tag info.
     *
     * @param modelName The name of the federated model.
     * @return A [WorkflowNetworkSummary] with graph and version metadata.
     */
    fun getLatestNetworkSummary(modelName: String): WorkflowNetworkSummary {
        val workflowNetworkSummary = WorkflowNetworkSummary()

        val modelDefinition = modelService.findModelDefinitionByName(modelName)
        val modelId = modelDefinition.id
        val workflowEntity = workflowService.findLastestWorkflowByModelId(modelId)
        val clients = modelService.findAllClients()

        val nodes = clients.map { client ->
            Node().apply {
                id = client.id
                name = client.clientName
            }
        }

        workflowNetworkSummary.nodes = nodes

        if (workflowEntity.isPresent) {
            val workflow = workflowEntity.get()
            val currentStep = workflow.currentStep
            val workflowTraceId = workflow.workflowTraceId!!

            val modelClientTrainingResults = modelService.getModelClientsByWorkflowTraceId(workflowTraceId)
            if (modelClientTrainingResults.isNotEmpty()) {
                val modelClientTrainingResult = modelClientTrainingResults.first()
                workflowNetworkSummary.latestTrainingDate = AppUtils.dateToYYYYMMDDHHMMSS(modelClientTrainingResult.createdDate)

                val activeClient = modelClientTrainingResult.client!!.id.toLong()

                workflowNetworkSummary.actionNode = activeClient

                val size = modelClientTrainingResults.size

                val tag = when {
                    currentStep == 1 || currentStep == 2 -> {
                        when {
                            size == 1 -> {
                                when (activeClient to currentStep) {
                                    1L to 1 -> "fl-1-1.png"
                                    1L to 2 -> "fl-1-2.png"
                                    2L to 1 -> "fl-2-1.png"
                                    2L to 2 -> "fl-2-2.png"
                                    else -> DEFAULTTAG
                                }
                            }
                            size == 2 -> {
                                val newModelClientTrainingResult = modelClientTrainingResults[1]
                                val newActiveClient = newModelClientTrainingResult.client!!.id.toLong()
                                when (newActiveClient to currentStep) {
                                    1L to 1 -> "fl-1-1.png"
                                    1L to 2 -> "fl-1-2.png"
                                    2L to 1 -> "fl-2-1.png"
                                    2L to 2 -> "fl-2-2.png"
                                    else -> DEFAULTTAG
                                }
                            }
                            else -> DEFAULTTAG
                        }
                    }
                    (modelName == "payment_fraud" && size == 2) || (size == nodes.size) -> {
                        when (currentStep) {
                            3 -> "fl-agg-1.png"
                            4 -> "fl-agg-final.png"
                            else -> DEFAULTTAG
                        }
                    }
                    else -> DEFAULTTAG
                }
                workflowNetworkSummary.tag = tag
                workflowNetworkSummary.tagSeq = tag.removeSuffix(".png")

                val modelAggregateWeights = modelService.findModelAggregateWeightsByWorkflowTraceId(workflowTraceId, modelId)
                if (modelAggregateWeights.isPresent) {
                    val modelAggregate = modelAggregateWeights.get()
                    workflowNetworkSummary.version = modelAggregate.version
                    workflowNetworkSummary.versionDisplay = "v-${modelAggregate.version}"
                    return workflowNetworkSummary
                } else {
                    val maxModelAggregateWeights = modelService.getMaxWeightsVersionForModelId(modelId)
                    if (maxModelAggregateWeights!=null) {
                        workflowNetworkSummary.version = maxModelAggregateWeights.version
                        workflowNetworkSummary.versionDisplay = "v-${maxModelAggregateWeights.version}"
                        return workflowNetworkSummary
                    }
                }
            }
        } else {
            setDefault(workflowNetworkSummary)
        }

        return workflowNetworkSummary
    }

    /**
     * Sets default placeholder values for the given [WorkflowNetworkSummary]
     * in case no active workflow is found.
     */
    fun setDefault(workflowNetworkSummary: WorkflowNetworkSummary) {
        workflowNetworkSummary.tag= DEFAULTTAG
        workflowNetworkSummary.tagSeq= "fl-0"
        workflowNetworkSummary.version = 1
        workflowNetworkSummary.versionDisplay = "v-0"
    }
    /**
     * Calculates contribution percentages for each client based on their number of training examples.
     *
     * @param modelName The name of the federated model.
     * @return [ContributionsRes] containing formatted pie chart data per client.
     */
    fun getContributions(modelName: String): ContributionsRes {
        val res = ContributionsRes()
        val modelClients = modelService.findAllModelClientByModel(modelName)
        // Group results by clientId and sum the numExamples for each client
        val clientExamples = modelClients.groupBy { it.clientId }
            .mapValues { entry -> entry.value.sumOf { it.numExamples ?: 0 } }
        // Calculate the total number of examples
        val totalExamples = clientExamples.values.sum()
        // Calculate the contribution percentage for each client
        val contributions = clientExamples.mapValues { entry ->
            (entry.value.toDouble() / totalExamples) * 100
        }
        val clients = modelService.findAllClients()
        // Map each client contribution to PieElementData and add to res.contribution
        clients.forEach { client ->
            val contributionPercentage = contributions[client.id] ?: 0.0
            val pieElementData = PieElementData().apply {
                name = client.clientName
                value = String.format("%.2f", contributionPercentage).toDouble()
                dbValue = contributionPercentage.toString()
            }
            res.contribution.add(pieElementData)
        }
        res.name = "Model Contributions"
        return  res
    }

    /**
     * Computes and returns model performance timeline data for the given metric.
     * The average performance is grouped by day and limited to the last 15 entries.
     *
     * @param modelName The name of the model.
     * @param metricKey The metric to analyze (e.g., "accuracy").
     * @return [PerformancesRes] containing performance trend over time.
     */
    @Transactional(readOnly = true)
    fun getPerformances(modelName: String, metricKey: String): PerformancesRes {
        val res = PerformancesRes()
        val modelAggregations = modelService.findAllModelAggregationsByModelName(modelName)
        // Extract workflowTraceIds from modelAggregations
        val workflowTraceIds = modelAggregations.mapNotNull { it.workflowTraceId }
        // Get metrics map by workflowTraceIds and source
        val metricsMap = modelService.getMetricsMapByWorkflowTraceIdsAndSource(workflowTraceIds, SERVER)

        // Group by truncated date
        val performanceMap = modelAggregations
            .filter { it.createdDate != null }
            .groupBy { AppUtils.truncateTime(it.createdDate!!) }

        // Calculate average performance by date
        val averagePerformanceMap: Map<Date, Double> = performanceMap.mapValues { (_, aggregations) ->
            val (total, count) = aggregations.fold(0.0 to 0) { acc, agg ->
                val value = metricsMap[agg.workflowTraceId]?.find { it.key == metricKey }?.value ?: 0.0
                (acc.first + value) to (acc.second + 1)
            }
            if (count > 0) total / count else 0.0
        }

        val sortedAveragePerformanceMap = averagePerformanceMap.toSortedMap()

        // Take the last 15 entries
        val lastFifteenEntries = sortedAveragePerformanceMap.entries.toList().takeLast(15)

        val timeLine = TimeLine().apply {
            name = "Model Performance"
            lastFifteenEntries.forEach { (date, averagePerformance) ->
                versions.add(AppUtils.dateToMMDD(date))
                values.add(String.format("%.2f", averagePerformance))
            }
        }

        res.performance = timeLine
        res.name = "Model Performance"
        return res
    }

}