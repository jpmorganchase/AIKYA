package com.aikya.orchestrator.service

import com.aikya.orchestrator.aggregate.model.*
import com.aikya.orchestrator.dto.fedlearn.ClientRequestWithWorkflowStatus
import com.aikya.orchestrator.dto.fedlearn.FlAggClientDataForm
import com.aikya.orchestrator.dto.fedlearn.FlAggClientForm
import com.aikya.orchestrator.dto.fedlearn.FlAggRequestForm
import com.aikya.orchestrator.dto.message.*
import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.utils.AppConstants.CLIENT
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.FAIL
import com.aikya.orchestrator.utils.AppConstants.Flow_200_20
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_1
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_4
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.SERVER
import com.aikya.orchestrator.utils.AppConstants.WORKFLOW_TRACE_ID_NONE
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.AppUtils.convertBase64StringToBlob
import com.aikya.orchestrator.utils.AppUtils.dateToYYYYMMDDHHMMSS
import com.aikya.orchestrator.utils.AppUtils.display
import com.aikya.orchestrator.utils.AppUtils.getUuid8
import com.aikya.orchestrator.utils.MessageBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.*
/**
 * OrchestrationServerService is responsible for managing the orchestration of
 * federated learning workflows. It processes incoming client training results,
 * coordinates workflow steps, manages collaboration run lifecycle, and handles
 * stalled requests and global model aggregation responses.
 *
 * Core Responsibilities:
 * - Persisting client training results and metrics.
 * - Assigning training submissions to collaboration rounds.
 * - Creating new workflows and collaboration runs as needed.
 * - Completing workflows when sufficient client submissions are received.
 * - Aggregating global model weights and preparing responses.
 * - Detecting and marking stalled client workflows as failed.
 * - Providing inspection utilities for client request status.
 *
 * This service coordinates tightly with ModelService and WorkflowService to
 * maintain the lifecycle and integrity of the federated learning process.
 *
 * Configuration Parameters (via application.yml):
 * - app.run-mode-type
 * - app.expired-client-request-minutes
 * - app.aggregate.vendor
 * - app.aggregate.strategy
 * - app.aggregate.total-round
 *
 * Thread Safety:
 * This class is a Spring-managed singleton with @Transactional boundaries to ensure
 * atomicity and consistency across methods dealing with persistent state.
 *
 */
@Service
class OrchestrationServerService  @Autowired constructor(
    private val modelService: ModelService,
    private val workflowService: WorkflowService,
) {
    private val logger: Logger = LoggerFactory.getLogger(OrchestrationServerService::class.java)
    @Value("\${app.run-mode-type:Multi-2}")
    private lateinit var runModelType: String
    @Value("\${app.expired-client-request-minutes}")
    val expireMinutes = 6
    @Value("\${app.aggregate.vendor:flwr}")
    lateinit var vendor: String
    @Value("\${app.aggregate.strategy:FedAvg}")
    lateinit var strategy: String
    @Value("\${app.aggregate.total-round:1000}")
    var totalRound: Int =1000
    /**
     * Processes an incoming message from a client containing training results. This method
     * orchestrates the entire process of handling a client's submission, including:
     * 1. Parsing the message and extracting training data (parameters, metrics).
     * 2. Persisting client metrics and the training result.
     * 3. Determining the appropriate server workflow, creating a new one if necessary.
     * 4. Associating the client submission with a collaboration run and round.
     * 5. Checking if the round's participation criteria are met to mark the server workflow as complete.
     *
     * @param message The client message containing training results.
     */
    @Transactional
    fun processClientMessage(message: Message) {
        val header = message.header!!
        val clientWorkflowTraceId = header.workflow_trace_id!!
        val domain = header.domain!!
        val modelDefinition = modelService.findModelDefinitionByDomain(domain)
        val modelId = modelDefinition.id
        val workflowType = workflowService.getWorkflowType(domain)
        val body = message.body!!
        val bodyData = body.data
            ?.filterIsInstance<Map<*, *>>()
            ?.mapNotNull { entry ->
                entry.entries
                    .filter { it.key is String && it.value != null }
                    .associate { it.key as String to it.value!! }
                    .takeIf { it.isNotEmpty() }
            } ?: emptyList()
        bodyData.forEach { item ->

            val clientIdReq = item["clientId"] as? String
            val parameters = item["parameters"] as? String
            val metricsRaw = item["metrics"]
            val metrics = if (metricsRaw is Map<*, *>) {
                metricsRaw.entries
                    .filter { it.key is String && it.value is Number }
                    .associate { it.key as String to (it.value as Number).toDouble() }
            } else emptyMap()
            val numExamples = item["numExamples"] as? Int ?: 0

            if (clientIdReq != null && parameters != null) {
                println("clientId: $clientIdReq")
                println("parameters: $parameters")
                println("numExamples: $numExamples")
            } else {
                println("Missing or invalid data in item: $item")
            }
            val trainingResult = TrainingResult(
                clientId = clientIdReq ?: "0",
                parameters = parameters ?: "",
                metrics = metrics,
                numExamples = numExamples
            )
            // Create and save FlMetrics entity
            val now = AppUtils.getCurrent()!!
            val metricsList = trainingResult.metrics?.map { (key, value) ->
                FlMetrics(
                    workflowTraceId = clientWorkflowTraceId,
                    source = CLIENT,
                    key = key,
                    value = value
                )
            } ?: emptyList()  // If metrics is null, return an empty list

            if (metricsList.isNotEmpty()) {
                modelService.saveAllMetrics(metricsList)
            } else {
                logger.info("No metrics to save for client workflowTraceId: $clientWorkflowTraceId")
            }
            val  runModel = modelService.findFlRunModel(runModelType)
            val runModelId = runModel.id
            val participantsNumber = runModel.participantsNumber!!
            val existingRuns = modelService.findModelCollaborationRunByRunModel(runModelId, modelId)
            var isNewGroup = true
            var groupHash = getUuid8()
            if (existingRuns.isNotEmpty()) {
                val existRun = existingRuns[0]
                groupHash = existRun.groupHash
                isNewGroup = false
            }
            var isNewWorkflow = true
            val pendingWorkflows = workflowService.getAllPendingWorkflows(modelDefinition.id)
            var serverWorkflowId = AppUtils.getUuid16()
            if(pendingWorkflows.isNotEmpty()) {
                serverWorkflowId = pendingWorkflows[0].workflowTraceId!!
                isNewWorkflow = false
                logger.info("found existing pending workflow: $serverWorkflowId for incoming ${domain} client: ${clientWorkflowTraceId}")
            } else {
                //make 0 for now, will update when needed
                workflowService.createWorkflowWithTraceId(serverWorkflowId,
                    modelDefinition.id, workflowType.id, Flow_Server_1, INITIAL, 0)
                logger.info("create New workflow: $serverWorkflowId for incoming  ${domain} client: ${clientWorkflowTraceId}")
            }
            val modelClientTrainingResult = FLModelClientTrainingResult()
            modelClientTrainingResult.workflowTraceId = serverWorkflowId
            modelClientTrainingResult.clientWorkflowTraceId = clientWorkflowTraceId
            val clientId = trainingResult.clientId.toLong()
            modelClientTrainingResult.clientId = clientId
            modelClientTrainingResult.modelId = modelId
            modelClientTrainingResult.domain = domain
            val parametersByteArray = convertBase64StringToBlob(trainingResult.parameters)
            modelClientTrainingResult.parameters = parametersByteArray
            modelClientTrainingResult.loss = trainingResult.metrics?.get("loss")?.toBigDecimal() ?: BigDecimal.ZERO ?: BigDecimal.ZERO
            modelClientTrainingResult.numExamples = trainingResult.numExamples
            modelClientTrainingResult.createdDate = now
            modelClientTrainingResult.lastUpdateDate = now
            modelService.saveModelClientTrainingResult(modelClientTrainingResult)
            logger.info("TRACE-ID: {} - received client message, clientId: {}, clientWorkflow: {}, runModelType: {}", serverWorkflowId, trainingResult.clientId, clientWorkflowTraceId, runModelType)

            if(isNewWorkflow) {
                if (isNewGroup) {
                    val newRun = createNewModelCollaborationRun(modelId, runModelId, groupHash, totalRound, participantsNumber)
                    createNewCollaborationRunClient(serverWorkflowId, clientWorkflowTraceId, newRun, clientId, 1,  participantsNumber)
                    logger.info("TRACE-ID: {} - INIT-NEW Workflow and create Collaboration Run, clientId: {}", serverWorkflowId, trainingResult.clientId)
                } else {
                    val existRun = existingRuns[0]
                    val round = existRun.currentRound +1
                    //increase round
                    createNewCollaborationRunClient(serverWorkflowId, clientWorkflowTraceId, existingRuns[0], clientId, round,  participantsNumber)
                    logger.info("TRACE-ID: $serverWorkflowId - INIT-NEW Workflow with exist Collaboration Run, clientId: ${trainingResult.clientId}, round (increase): $round" )
                }
            } else {
                val existClientRecords = modelService.getModelClientsByWorkflowTraceId(serverWorkflowId)
                val currentRound = existClientRecords.firstOrNull()?.rounds ?: 0
                val minClientsPerRound = existClientRecords.firstOrNull()?.minClientsPerRound ?: 0
                // Check if the clientId already exists in the records for this round
                val existingClientRecord = existClientRecords.find { it.client?.clientId == clientId.toInt() }
                if (existingClientRecord != null) {
                    // Client already exists for the current round, create a new record but keep round incomplete
                    if(existingRuns.isNotEmpty()) {
                        addSameClientToExistCollaborationRunClient(serverWorkflowId, clientWorkflowTraceId, existingRuns[0], clientId, currentRound, minClientsPerRound)
                    }
                } else {
                    if(existingRuns.isNotEmpty()) {
                        addNewClientToExistCollaborationRunClient(serverWorkflowId, clientWorkflowTraceId, existingRuns[0], clientId, currentRound, minClientsPerRound)
                    }
                    // Client is new for this round, create a new record and check distinct client count
                    // Get distinct client IDs from existing records and count them
                    val distinctClientIds = existClientRecords.mapNotNull { it.client?.id }.distinct().toMutableSet()
                    distinctClientIds.add(clientId) // Add the new clientId
                    // If distinct client count is equal to or greater than minClientsPerRound, mark round as complete
                    if (distinctClientIds.size >= minClientsPerRound) {
                        // Update the server workflow to complete
                        val workflowEntity = workflowService.findWorkflowByTraceId(serverWorkflowId)
                        if(workflowEntity.isPresent) {
                            val workflow = workflowEntity.get()
                            if(workflow.currentStep==Flow_Server_1.step) {
                                workflowService.updateServerWorkflow(serverWorkflowId, COMPLETE, Flow_Server_1)
                            }
                        }
                    }
                }
                logger.info("TRACE-ID: {} - processed $domain client message with Collaboration Run, clientId: {}", serverWorkflowId, trainingResult.clientId)
            }

        }
        logger.info("Complete process received $domain client message with Collaboration Run, clientWorkflowId: $clientWorkflowTraceId")
    }
    /**
     * Handles a resubmission from a client that is already part of the current collaboration round.
     * This creates a new `FlCollaborationRunClient` record for tracking purposes but does not increment
     * the distinct participant count for the round.
     *
     * @param serverWorkflowTraceId The trace ID of the server-side workflow.
     * @param clientWorkflowTraceId The trace ID of the client's workflow submission.
     * @param run The active `FLModelCollaborationRun`.
     * @param clientId The ID of the submitting client.
     * @param round The current round number.
     * @param minClients The minimum number of clients required for the round.
     */
    @Transactional
    fun addSameClientToExistCollaborationRunClient(
        serverWorkflowTraceId: String,
        clientWorkflowTraceId: String,
        run: FLModelCollaborationRun,
        clientId: Long,
        round: Int,
        minClients: Int,
    ) {
        logger.info("TRACE_ID- $serverWorkflowTraceId add same client to CollaborationRunClient: clientId: $clientId client workflow: $clientWorkflowTraceId to round $round")
        createNewCollaborationRunClient(serverWorkflowTraceId, clientWorkflowTraceId, run, clientId, round,  minClients)
    }
    /**
     * Adds a new, distinct client to an existing collaboration round. This is called when a client
     * that has not yet submitted for the current round sends its results.
     *
     * @param serverWorkflowTraceId The trace ID of the server-side workflow.
     * @param clientWorkflowTraceId The trace ID of the client's workflow submission.
     * @param run The active `FLModelCollaborationRun`.
     * @param clientId The ID of the submitting client.
     * @param round The current round number.
     * @param minClients The minimum number of clients required for the round.
     */
    @Transactional
    fun addNewClientToExistCollaborationRunClient(
        serverWorkflowTraceId: String,
        clientWorkflowTraceId: String,
        run: FLModelCollaborationRun,
        clientId: Long,
        round: Int,
        minClients: Int,
    ) {
        logger.info("TRACE_ID- ${serverWorkflowTraceId} add New client to CollaborationRunClient: clientId: $clientId client workflow: ${clientWorkflowTraceId} to round $round")
        createNewCollaborationRunClient(serverWorkflowTraceId, clientWorkflowTraceId, run, clientId, round,  minClients)
    }
    /**
     * Creates and persists a new `FlCollaborationRunClient` entity, linking a client's
     * submission to a specific collaboration run and round.
     *
     * @param serverWorkflowTraceId The trace ID of the server-side workflow.
     * @param clientWorkflowTraceId The trace ID of the client's workflow submission.
     * @param run The `FLModelCollaborationRun` to associate with.
     * @param clientId The client's ID.
     * @param round The current round number.
     * @param minClients The minimum number of clients required for this round.
     */
    @Transactional
    fun createNewCollaborationRunClient(
        serverWorkflowTraceId: String,
        clientWorkflowTraceId: String,
        run: FLModelCollaborationRun,
        clientId: Long,
        round: Int,
        minClients: Int,
        ) {
        val client = modelService.findByClientId(clientId.toInt())
        val now = AppUtils.getCurrent()
        val collaborationRunClient = FlCollaborationRunClient().apply {
            this.run = run
            this.client = client
            this.rounds = round
            this.minClientsPerRound = minClients
            this.workflowTraceId = serverWorkflowTraceId
            this.clientWorkflowTraceId = clientWorkflowTraceId
            this.isSubmitted = false
            this.isRoundComplete = false
            this.groupHash = run.groupHash
            this.createdDate  = now
            this.lastUpdateDate = now
        }
        logger.info("TRACE_ID- ${serverWorkflowTraceId} create New CollaborationRunClient: clientId: $clientId client workflow: ${clientWorkflowTraceId} to round $round")
        modelService.saveCollaborationRunClient(collaborationRunClient)
    }
    /**
     * Creates and persists a new `FLModelCollaborationRun`, initializing a new federated
     * learning session for a specific model.
     *
     * @param modelId The ID of the model definition.
     * @param runModelId The ID of the run model configuration.
     * @param groupHash A unique hash to identify this collaboration group.
     * @param rounds The total number of rounds planned for this run.
     * @param minClients The minimum number of clients required per round.
     * @return The newly created and persisted `FLModelCollaborationRun` entity.
     */
    @Transactional
    fun createNewModelCollaborationRun(
        modelId: Long,
        runModelId: Long,
        groupHash: String,
        rounds: Int,
        minClients: Int
    ): FLModelCollaborationRun {
        val model = FlModelDefinitionEntity().apply { this.id = modelId }

        val now = AppUtils.getCurrent()
        var newRun = FLModelCollaborationRun().apply {
            this.model = model
            this.runModelId = runModelId // Assigning runModelId correctly as a Long
            this.groupHash = groupHash
            this.rounds = rounds
            this.currentRound = 0
            this.minClients = minClients
            this.startedAt = now
            this.status = INITIAL
        }
        newRun = modelService.saveModelCollaborationRun(newRun)
        logger.info("created New Collaboration Run - round 1, groupHash={}", groupHash)
        return newRun
    }
    /**
     * Retrieves the initial global model weights for a specified model. This is typically
     * called by clients at the beginning of a federated learning process.
     *
     * @param modelName The name of the model.
     * @return A `Message` containing the initial model weights, version, and definition.
     * Returns an empty message if no model is found.
     */
    fun getInitialGlobalModelWeight(modelName: String): Message {
        val modelDef = modelService.findOptionalModelDefinitionByName(modelName)
        if(modelDef!=null) {
            val modelId = modelDef.id
            val domain = modelDef.domain!!
            val globalModelWeights = modelService.findOptionalGlobalModelWeightsByModelId(modelId)
            if (globalModelWeights != null) {
                val weight =  AppUtils.convertBlobToBase64String(globalModelWeights.globalModelWeights)!!
                val initGlobalModelWeight = InitGlobalModelWeight(
                    globalModelWeights.globalWeightsVersion!!,
                    weight,
                    modelDef.modelDefinition!!
                )
                return buildInitialGlobalModelWeightMessage(domain, initGlobalModelWeight)
            }
        }
        logger.warn("No global model weights found for modelName: $modelName")
        return buildEmptyInitialGlobalModelWeightMessage(modelName)
    }
    /**
     * Retrieves the latest aggregated global model based on a client's request.
     * If the client requests a version that is already available, it returns the historical result.
     *
     * @param message The client's request message, containing client ID and requested version.
     * @return A `Message` containing the aggregated model results. Returns an empty message if data is not available.
     */
    fun getAggregateGlobalModel(message: Message): Message {
        val header = message.header!!
        val workflowTraceId = header.workflow_trace_id!!
        val domain = header.domain!!
        val modelDefinition = modelService.findModelDefinitionByDomain(domain)
        val modelId = modelDefinition.id
        val body = message.body ?: throw IllegalArgumentException("Message body is null")
        val bodyData = body.data?.let {
            (it as? Collection<*>)?.filterIsInstance<Map<*, *>>()
        } ?: emptyList()
        bodyData.forEach { item ->
            val globalModelReq = GlobalModelReq(
                clientId = item["clientId"] as String,
                version = (item["version"] as Int).toLong(),
                size = (item["size"] as Int),
                model= item["model"] as String,
            )
            val version = globalModelReq.version
            val reqClientId = globalModelReq.clientId
            val currentMaxVersionWeight = modelService.getMaxWeightsVersionForModelId(modelId)
            val currentMaxVersion = currentMaxVersionWeight?.version ?: 0L
            if(currentMaxVersion>0 && currentMaxVersion>=version) {
                // Find historical results
                logger.info("TRACE-ID: {} - clientId: {}, version: {}", workflowTraceId, globalModelReq.clientId, version)
                val aggregateWeightsEntity = currentMaxVersionWeight!!
                val aggWfTraceId = aggregateWeightsEntity.workflowTraceId
                val metricsList = modelService.findFlMetricsByWorkflowTraceIdAndSource(aggWfTraceId, SERVER)
                val aggregationResult = createAggregationResult(aggregateWeightsEntity, modelId, reqClientId, metricsList, true)
                val endVersion = currentMaxVersion - 1
                logger.info("TRACE-ID: {} - clientId: {}, load historical version, from: {} to: {}", workflowTraceId, globalModelReq.clientId, version, endVersion)
                val historicalResultsEntities = modelService.findWeightsVersionsBetween(modelId, version, endVersion)
                val historicalResults = historicalResultsEntities.map { histMetric ->
                    createAggregationResult(histMetric, modelId, reqClientId, metricsList, false)
                }
                aggregationResult.historicResults = historicalResults
                return buildAggregationMessage(workflowTraceId, domain, aggregationResult);
            }
        }
        return Message()
    }
    /**
     * Helper function to create an `AggregationResult` object from persisted data.
     *
     * @param aggregateWeightsEntity The entity containing the aggregated model weights.
     * @param modelId The ID of the model.
     * @param reqClientId The ID of the client that requested the data.
     * @param metricsList The list of metrics associated with the aggregation.
     * @param includeWeight A boolean to indicate whether to include the model weight parameters.
     * @return An `AggregationResult` object populated with data.
     */
    fun createAggregationResult(aggregateWeightsEntity: FlModelAggregateWeights, modelId: Long, reqClientId: String, metricsList: List<FlMetrics>, includeWeight: Boolean): AggregationResult {
        val modelWeightsId = aggregateWeightsEntity.id
        val modelWeightWfTraceId = aggregateWeightsEntity.workflowTraceId
        val runModelAggregation = modelService.runModelAggregationRepository(modelWeightsId, modelId)
        val numExamples = runModelAggregation.numExamples!!
        val loss = runModelAggregation.loss!!.toDouble()
        var parameters = ""
        if(includeWeight) {
            parameters = AppUtils.convertBlobToBase64String(aggregateWeightsEntity.parameters)!!
        }
        // Map metricsList to a metrics map
        val dynamicMetrics = metricsList.associate { it.key to it.value }
        // Include the runModelAggregation.metrics
        val metrics = dynamicMetrics + mapOf("loss" to loss)
        val modelClientResults = modelService.findCollaborationRunClientByClientId(reqClientId.toString().toLong(), modelWeightWfTraceId)
        var clientWorkflowTraceId = WORKFLOW_TRACE_ID_NONE
        var isSelf = false
        if (modelClientResults.isNotEmpty()) {
            isSelf = true
            clientWorkflowTraceId = modelWeightWfTraceId
        }
        return AggregationResult(aggregateWeightsEntity.version!!, parameters, metrics, numExamples, clientWorkflowTraceId, isSelf)
    }
    /**
     * Builds a `Message` object to send an aggregation result to a client.
     *
     * @param workflowTraceId The workflow trace ID for the message header.
     * @param domain The domain for the message header.
     * @param aggregationResult The payload for the message body.
     * @return A fully constructed `Message`.
     */
    fun buildAggregationMessage(workflowTraceId: String, domain: String, aggregationResult: AggregationResult): Message {
        val header = MessageBuilder.buildHeader(
            workflowTraceId = workflowTraceId,
            eventType = Flow_Server_4.event,
            domain = domain,
            status = COMPLETE)
        val body = MessageBuilder.buildBody(mutableListOf(aggregationResult))
        val aggregationMessage = MessageBuilder.buildMessage<AggregationResult>(
            id = AppUtils.getUuid8(),
            header = header,
            body = body
        )
        return aggregationMessage
    }
    /**
     * Builds a `Message` object for sending the initial global model weights.
     *
     * @param domain The domain for the message header.
     * @param globalModelWeight The initial model weight data for the message body.
     * @return A fully constructed `Message`.
     */
    fun buildInitialGlobalModelWeightMessage(domain: String, globalModelWeight: InitGlobalModelWeight): Message {
        val header = MessageBuilder.buildHeader(
            workflowTraceId = WORKFLOW_TRACE_ID_NONE,
            eventType = Flow_200_20.event,
            domain = domain,
            status = COMPLETE)
        val body = MessageBuilder.buildBody(mutableListOf(globalModelWeight))
        val globalModelWeightMessage = MessageBuilder.buildMessage<AggregationResult>(
            id = AppUtils.getUuid8(),
            header = header,
            body = body
        )
        return globalModelWeightMessage
    }
    /**
     * Builds an empty `Message` to indicate that no initial global model weights were found.
     *
     * @param domain The domain for the message header.
     * @return An empty `Message` with the appropriate header.
     */
    fun buildEmptyInitialGlobalModelWeightMessage(domain: String): Message {
        val header = MessageBuilder.buildHeader(
            workflowTraceId = WORKFLOW_TRACE_ID_NONE,
            eventType = Flow_200_20.event,
            domain = domain,
            status = COMPLETE)

        val globalModelWeightMessage = MessageBuilder.buildMessage<AggregationResult>(
            id = getUuid8(),
            header = header
        )
        globalModelWeightMessage.id = null
        return globalModelWeightMessage
    }

    /**
     * Processes the clients of the current round for a given workflow. It checks if the minimum
     * number of distinct clients have submitted their results. If so, it marks the round as complete,
     * updates the collaboration run, and prepares a request for the aggregation service.
     *
     * @param workflowTraceId The trace ID of the server workflow to process.
     * @return A `FlAggRequestForm` to be sent to the aggregation service if the round is complete, otherwise `null`.
     */
    fun processCurrentRoundClients(workflowTraceId: String): FlAggRequestForm? {
        // Fetch existing collaboration run client records by workflowTraceId
        val collaborationRunAllClients = modelService.findCollaborationRunClient(workflowTraceId)
        if (collaborationRunAllClients.isNotEmpty()) {
            logger.info("TRACE_ID- $workflowTraceId Collaboration run clients found.")
            // Find distinct clients and their latest submissions based on submissionDate
            val currentRoundClients = collaborationRunAllClients
                .groupBy { it.client?.id }
                .mapValues { it.value.maxByOrNull { clientRecord -> clientRecord.createdDate ?: Date(0) } } // Get latest for each client
                .values.filterNotNull()

            val minClientsPerRound = collaborationRunAllClients.first().minClientsPerRound
            if (currentRoundClients.size >= minClientsPerRound) {
                logger.info("TRACE_ID- $workflowTraceId Round has enough distinct clients (${currentRoundClients.size} >= $minClientsPerRound). Marking round complete.")
                val now = AppUtils.getCurrent()
                // Mark all clients in the current round as submitted and round complete
                collaborationRunAllClients.forEach { clientRecord ->
                    clientRecord.isSubmitted = true
                    clientRecord.isRoundComplete = true
                    clientRecord.submissionDate = now
                    clientRecord.lastUpdateDate = now
                    modelService.saveCollaborationRunClient(clientRecord)
                }
                val clientWorkflowTraceIds = currentRoundClients.map { it.clientWorkflowTraceId!! }
                val matchingTrainingResults = modelService.findModelClientTrainingResults(clientWorkflowTraceIds)
                if (matchingTrainingResults.isNotEmpty()) {
                   val clientTrainingResult = matchingTrainingResults[0]
                    val collaborationRunClient= currentRoundClients[0]
                    val runId = collaborationRunClient.run!!.id
                    val domain = clientTrainingResult.domain!!
                    val modelId = clientTrainingResult.modelId!!
                    val currentRound= collaborationRunClient.rounds
                    val collaborationRunEntity = modelService.findModelCollaborationRunById(runId)
                    // Update the corresponding ModelCollaborationRun to increment the round number
                    if (collaborationRunEntity.isPresent) {
                        val collaborationRun = collaborationRunEntity.get()
                        collaborationRun.currentRound = currentRound
                        modelService.saveModelCollaborationRun(collaborationRun)
                        display(logger, "====  TRACE_ID- $workflowTraceId Updated model_collaboration_run round to ${collaborationRun.currentRound}. ==== ")
                        val groupHash = collaborationRun.groupHash
                        return buildCurrentRoundFlAggregateMessage(currentRoundClients, matchingTrainingResults, domain, modelId, currentRound, workflowTraceId, groupHash, vendor, strategy)
                    } else {
                        logger.error("No corresponding ModelCollaborationRun found for workflowTraceId: $workflowTraceId")
                    }
                }


            } else {
                logger.error("TRACE_ID- $workflowTraceId Round does not have enough distinct clients yet (${currentRoundClients.size} < $minClientsPerRound).")
            }
        }
        return null
    }
    /**
     * Constructs the request form (`FlAggRequestForm`) required by the federated learning
     * aggregation service. This form contains all client parameters, metrics, and metadata for the completed round.
     *
     * @param collaborationRunClients The list of client records for the completed round.
     * @param trainingResults The list of persisted training results from the clients.
     * @param domain The model's domain.
     * @param modelId The model's ID.
     * @param currentRound The current round number.
     * @param workflowTraceId The server workflow trace ID.
     * @param groupHash The hash identifying the collaboration group.
     * @param vendor The aggregation vendor (e.g., "flwr").
     * @param strategy The aggregation strategy (e.g., "FedAvg").
     * @return A populated `FlAggRequestForm`, or `null` if inputs are invalid.
     */
    fun buildCurrentRoundFlAggregateMessage(
        collaborationRunClients: List<FlCollaborationRunClient>,
        trainingResults: List<FLModelClientTrainingResult>,
        domain: String,
        modelId: Long,
        currentRound: Int,
        workflowTraceId: String,
        groupHash: String,
        vendor: String,
        strategy: String
    ): FlAggRequestForm? {
        if (collaborationRunClients.isEmpty()) {
            logger.warn("collaborationRunClients list is empty")
            return null
        }
        if (trainingResults.isEmpty()) {
            val workflowTraceIds = collaborationRunClients.mapNotNull { it.clientWorkflowTraceId }
            logger.warn("No training results found for workflowTraceIds: $workflowTraceIds")
            return null
        }
        val clients = trainingResults.mapNotNull { trainingResult ->
            val clientWorkflowTraceId = trainingResult.clientWorkflowTraceId ?: return@mapNotNull null
            val clientId = trainingResult.clientId ?: return@mapNotNull null
            val parameters = trainingResult.parameters ?: return@mapNotNull null
            val numExamples = trainingResult.numExamples ?: 0

            val metricsList = modelService.findFlMetricsByWorkflowTraceIdAndSource(clientWorkflowTraceId, "CLIENT")
            val metricsMap: Map<String, Any> = if (metricsList.isNotEmpty()) {
                metricsList.associate { it.key to it.value }
            } else {
                logger.info("No metrics found for clientWorkflowTraceId: $clientWorkflowTraceId")
                emptyMap()
            }

            val weight = AppUtils.convertBlobToBase64String(parameters) ?: return@mapNotNull null

            FlAggClientForm(
                clientId = clientId.toInt(),
                data = FlAggClientDataForm(
                    parameters = weight,
                    metrics = metricsMap,
                    numExamples = numExamples
                )
            )
        }

        return FlAggRequestForm(
            domainType = domain,
            numRounds = currentRound,
            strategy = listOf(strategy),
            vendor = vendor,
            modelId = modelId,
            groupHash = groupHash,
            workflowTraceId = workflowTraceId,
            clients = clients
        )
    }
    /**
     * Scans for and manages stalled client requests. A request is considered stalled if it
     * has been in a pending state for longer than the configured `expireMinutes`.
     * This method finds such requests and marks their corresponding server workflows as FAILED.
     * This is intended to be run periodically (e.g., via a scheduled task).
     */
    @Transactional
    fun manageStalledClientRequestProcesses() {
        val someMinutesAgo = Date(System.currentTimeMillis() - (expireMinutes * 60 * 1000))
        val pendingStalledClientRequests = modelService.findPendingStalledClients(someMinutesAgo)
        if(pendingStalledClientRequests.isNotEmpty()) {
            for(pendingStalledClient in pendingStalledClientRequests) {
                val groupHash = pendingStalledClient.groupHash
                val workflowTraceId = pendingStalledClient.workflowTraceId!!
                val clientWorkflowTraceId = pendingStalledClient.clientWorkflowTraceId
                val createAt = dateToYYYYMMDDHHMMSS(pendingStalledClient.createdDate)
                val currentRound = pendingStalledClient.rounds
                val clientId = pendingStalledClient.client!!.clientId
                val clientName = pendingStalledClient.client!!.clientName
                val pendingStalledWorkflowEntity = workflowService.findWorkflowByTraceId(workflowTraceId)
                logger.info(
                    """
                -------- Found pending stalled client process --------
                Client Name: $clientName
                Client ID: $clientId
                Workflow Trace ID: $workflowTraceId
                Client Workflow Trace ID: $clientWorkflowTraceId
                Group Hash: $groupHash
                Created At: $createAt
                Current Round: $currentRound
                Process ID: ${pendingStalledClient.id}
                -----------------------------------------------
                """.trimIndent()
                )
                if(pendingStalledWorkflowEntity.isPresent) {
                    val pendingStalledWorkflow = pendingStalledWorkflowEntity.get()
                    val workflowId = pendingStalledWorkflow.id!!
                    val currentStep = pendingStalledWorkflow.currentStep
                    val flowEvent = workflowService.findServerFlowEventByStep(currentStep)!!
                    workflowService.updateServerWorkflow(workflowTraceId, FAIL, flowEvent)
                    logger.info("--------  marked as fail for pending stalled workflow processes ID: {} ----------------", workflowId)
                }
            }
        }
    }
    /**
     * Checks the status of one or more client requests by looking up the status of their
     * associated server-side workflows.
     *
     * @param clientWorkflowTraceIds A list of client workflow trace IDs to check.
     * @return A mutable list of `ClientRequestWithWorkflowStatus` objects, each containing the
     * client ID, its workflow trace ID, and the status of the corresponding server workflow.
     */
    fun checkClientRequest(clientWorkflowTraceIds: List<String>): MutableList<ClientRequestWithWorkflowStatus> {
        val clientTrainingResults = modelService.findModelClientTrainingResults(clientWorkflowTraceIds)
        // Prepare a list to hold the result of client requests with their workflow statuses
        val clientRequestsWithStatus = mutableListOf<ClientRequestWithWorkflowStatus>()
        // If there are client training results, proceed with finding their workflows
        if (clientTrainingResults.isNotEmpty()) {
            // Extract the workflow trace IDs from the client training results
            val workflowTraceIds = clientTrainingResults.map { it.workflowTraceId!! }
            val workflows = workflowService.findWorkflowsByTraceIds(workflowTraceIds)
            for (clientTrainingResult in clientTrainingResults) {
                val workflowTraceId = clientTrainingResult.workflowTraceId
                val matchingWorkflow = workflows.find { it.workflowTraceId == workflowTraceId }
                // Get the workflow status if the workflow exists, otherwise set it to "Unknown"
                val workflowStatus = matchingWorkflow?.status ?: "Unknown"
                // Create a new object to hold client request and workflow status information
                val clientRequestWithStatus = ClientRequestWithWorkflowStatus(
                    clientId = clientTrainingResult.clientId!!,
                    workflowTraceId = workflowTraceId,
                    workflowStatus = workflowStatus
                )
                // Add the result to the list
                clientRequestsWithStatus.add(clientRequestWithStatus)
            }
        }

        // Return the list of client requests with their workflow statuses
        return clientRequestsWithStatus

    }
}