package com.aikya.orchestrator.service

import com.aikya.orchestrator.agent.model.AgentModelLogsEntity
import com.aikya.orchestrator.agent.model.GlobalMetrics
import com.aikya.orchestrator.agent.model.GlobalModelTrainingResult
import com.aikya.orchestrator.agent.model.GlobalModelWeightsEntity
import com.aikya.orchestrator.client.model.fedlearn.ModelClientRecordEntity
import com.aikya.orchestrator.dto.common.GlobalModelWeightInitRes
import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.PredictResponse
import com.aikya.orchestrator.dto.fedlearn.TrainingResponse
import com.aikya.orchestrator.dto.fedlearn.WeightResponse
import com.aikya.orchestrator.dto.message.*
import com.aikya.orchestrator.dto.seeds.DataLoadRequest
import com.aikya.orchestrator.dto.seeds.SeedProcessResponse
import com.aikya.orchestrator.service.common.CallerService
import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.utils.AppConstants.CLIENT
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.FAIL
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_1
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_2
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_4
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_5
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_6
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.TRAINING
import com.aikya.orchestrator.utils.AppConstants.WORKFLOW_TRACE_ID_NONE
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.AppUtils.convertBase64StringToBlob
import com.aikya.orchestrator.utils.AppUtils.display
import com.aikya.orchestrator.utils.AppUtils.failure
import com.aikya.orchestrator.utils.AppUtils.getNStrings
import com.aikya.orchestrator.utils.AppUtils.isBlobNullOrEmpty
import com.aikya.orchestrator.utils.AppUtils.success
import com.aikya.orchestrator.utils.AppUtils.toTFString
import com.aikya.orchestrator.utils.MessageBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.math.BigDecimal
/**
 * Service responsible for orchestrating outbound HTTP calls from the client to other services
 * in the federated learning ecosystem, such as the central server, the local agent, and the data processor.
 *
 * This class encapsulates the logic for building request bodies, making REST calls, and processing
 * the responses to drive the federated learning workflow forward.
 *
 * @param urlService Service for resolving the URLs of external services.
 * @param callerService A generic service for making HTTP POST requests.
 * @param clientWorkflowService Service for managing client-specific workflow state transitions.
 * @param workflowService Generic service for core workflow operations.
 * @param modelService Service for accessing and persisting model-related data.
 * @param dataSeedsService Service for managing data batch (seed) metadata.
 * @param workflowModelLogsService Service for managing run modes and model logs.
 */
@Transactional
@Service
class OrchestrationClientCallService(
    private val urlService: UrlService,
    private val callerService: CallerService,
    private val clientWorkflowService: ClientWorkflowService,
    private val workflowService: WorkflowService,
    private val modelService: ModelService,
    private val dataSeedsService: DataSeedsService,
    private val workflowModelLogsService: WorkflowModelLogsService
) {
    private val logger: Logger = LoggerFactory.getLogger(OrchestrationClientCallService::class.java)
    /**
     * Registers this client node with the central orchestrator server.
     *
     * @param nodeName The name of the client node.
     * @param nodeNumber The unique integer ID of the client node.
     * @param nodeMail The contact email for the client node.
     */
    fun callRegisterClient(nodeName: String, nodeNumber:Int, nodeMail: String) {
        try {

            val requestBody = mapOf(
                "clientName" to nodeName,
                "clientId" to nodeNumber,
                "email" to nodeMail,
                "consentRecord" to false,
                "complianceStatus" to true,
            )
            val orchestratorServerClientRegisterUrl = urlService.getServerUrl("clientRegister")
            val response: WebResponse = callerService.post(
                orchestratorServerClientRegisterUrl,
                requestBody,
                WebResponse::class.java
            )
            success(logger, "Response : ${response.message}")
        } catch (e: Exception) {
            logger.error("run register client with error: {}", e.message)
        }
    }
    /**
     * Calls the local agent to initialize model weights and updates local records accordingly.
     *
     * @param domain The learning domain.
     * @param model The name of the model.
     * @param version The version of the model.
     */
    fun callInitialWeight(domain: String, model: String, version: Int) {
        try {
            val initialWeightsUrl = urlService.getAgentUrl("initialWeights")
            val requestBody = mapOf(
                "name" to model,
                "domain" to domain,
                "version" to version.toString(),
            )
            val response: WeightResponse = callerService.post(
                initialWeightsUrl,
                requestBody,
                WeightResponse::class.java
            )
            if (response.status == "success") {
                val modelClientRecords = modelService.getModelClientRecordByDomain(domain)
                if(modelClientRecords!=null) {
                    modelClientRecords.domain
                    val modelDefinition = modelService.findModelDefinition(domain)
                    modelDefinition.modelDefinition = modelClientRecords.definition
                    modelService.saveModelDefinition(modelDefinition)
                    val agentModelLogs = modelService.findAgentModelLogs(model)
                    if(agentModelLogs==null) {
                        val agentModelLog = AgentModelLogsEntity()
                        agentModelLog.modelId = modelDefinition.id
                        agentModelLog.localWeightsVersion = 1
                        agentModelLog.globalWeightsVersion = 0
                        modelService.saveAgentModelLogs(agentModelLog)
                        logger.info(LOG_INITIAL_WEIGHT_SAVED, agentModelLog.modelId)
                    }
                }
            }
        }  catch (e: Exception) {
            logger.error("run complete workflow task with error: {}", e.message)
        }
    }
    /**
     * Initiates a data loading process by creating a workflow and calling the external data processor service.
     * This method operates asynchronously but blocks to wait for a response to update the workflow status.
     *
     * @param dataLoadRequest The request containing details about the data to be loaded.
     * @return A `WebResponse` indicating the immediate outcome of the call.
     */
    fun callDataInitLoad(dataLoadRequest: DataLoadRequest): WebResponse {
        val webRes = WebResponse()
        val domainType = dataLoadRequest.domainType!!
        val fileName = dataLoadRequest.fileName!!
        val mockerEnabled = dataLoadRequest.mockerEnabled!!
        val workflowType = workflowService.getWorkflowType(domainType)
        val modelDefinition = modelService.getModelDefinition(domainType)
        val globalModelLogversion = modelService.findGlobalModelLogsByDomain(domainType)
        val wf = workflowService.createWorkflow(modelDefinition.id, workflowType.id, Flow_Client_1, INITIAL, globalModelLogversion.globalWeightsVersion)
        val workflowTraceId =  wf.workflowTraceId!!
        val modelName = modelDefinition.modelName!!
        val isMockData = mockerEnabled.toTFString()
        val batchId = AppUtils.getUuid8()
        logger.info("TRACE-ID: {} - send data-init request, batchId: {}", workflowTraceId, batchId)
        // send over to data processor
        val requestBody = mapOf(
            "fileName" to fileName,
            "domainType" to domainType,
            "mockerEnabled" to isMockData,
            "workflowTraceId" to workflowTraceId,
            "batchId" to batchId,
            "model" to modelName,
        )
        val dataInitLoadUrl = urlService.getDataUrl("initLoad")
        val requestMono: Mono<SeedProcessResponse> = Mono.fromCallable {
            callerService.post(
                dataInitLoadUrl,
                requestBody,
                SeedProcessResponse::class.java
            )
        }.onErrorResume { e ->
            if (e is WebClientResponseException) {
                // Log and return a fallback or an empty Mono
                logger.error("Error response body: ${e.responseBodyAsString}")
                logger.error("Status code: ${e.statusCode}")
                Mono.empty()  // No value will be emitted
            } else {
                logger.error("An error occurred: ${e.message}")
                Mono.empty()
            }
        }
        requestMono.subscribe(
            { response -> logger.info("Received response: $response") },
            { error -> logger.error("Error during subscription: ${error.message}") }
        )
        val seedProcessResponse = requestMono.block()
        if (seedProcessResponse != null) {
            workflowService.updateClientWorkflow(workflowTraceId, COMPLETE, Flow_Client_1)
            webRes.message = "data load ($fileName) in progress. "
            webRes.success = true
        } else {
            webRes.message = "data load ($fileName) failed."
            webRes.success = false
            workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_1)
        }
        return webRes
    }
    /**
     * Calls the local agent to perform predictions on a batch of data.
     * Updates the workflow status based on the success or failure of the prediction call.
     *
     * @param workflowTraceId The trace ID of the associated workflow.
     * @param workflowEnable A flag indicating if this call should trigger subsequent workflow steps.
     */
    fun callPredict(workflowTraceId: String, workflowEnable: Boolean) {
        try {
            val modelPredict = modelService.findModelPredictionByWorkflowTraceId(workflowTraceId)
            val batchId= modelPredict.batchId!!
            val requestBody = mapOf(
                "domainType" to modelPredict.domain,
                "workflowTraceId" to workflowTraceId,
                "batchId" to batchId,
                "workflowEnable" to workflowEnable,
                "workflowEvent" to Flow_Client_2.event
            )
            logger.info("TRACE-ID: {} -calling agent predict function for batch: {}", workflowTraceId, batchId)
            val predictUrl = urlService.getAgentUrl("predict")
            val response: PredictResponse = callerService.post(
                predictUrl,
                requestBody,
                PredictResponse::class.java
            )
            logger.info("TRACE-ID: {} -received predict result, status: {}, number of prediction items: {}", response.workflowTraceId, response.status, response.items)
            if (response.status != "success") {
                val workflowModel = workflowModelLogsService.getInitialWorkflowModelLogs(workflowTraceId)
                workflowModel.status = FAIL
                workflowModelLogsService.updateWorkflowModelLogs(workflowModel)
                workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_2)
            }
        }  catch (e: Exception) {
            logger.error("TRACE-ID: {} - run call Predict task with error: {}", workflowTraceId, e.message)
        }
    }
    /**
     * Convenience method to initiate a training call using only the workflow trace ID.
     * It looks up the necessary details and calls the core training method.
     *
     * @param workflowTraceId The trace ID of the workflow that is ready for training.
     */
    @Transactional
    fun callTraining(workflowTraceId: String) {
        val dataSeed = dataSeedsService.findDataSeedByWorkflowTraceId(workflowTraceId)
        val domain = dataSeed.domainType!!
        val batchId =  dataSeed.batchId!!
        callTraining(workflowTraceId, batchId, domain, false)
        val workflowModel = workflowModelLogsService.getWorkflowModelLogs(workflowTraceId, TRAINING)
        if(workflowModel.status== FAIL) {
            workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_4)
            failure(logger, "TRACE-ID:  ${workflowTraceId} -Training failed")
        }
    }
    /**
     * Calls the local agent to start a training job on a specific batch of data.
     * It updates the workflow status based on the success or failure of the training call.
     *
     * @param workflowTraceId The trace ID of the associated workflow.
     * @param batchId The ID of the data batch to be used for training.
     * @param domainType The learning domain.
     * @param workflowEnable A flag indicating if this call should trigger subsequent workflow steps.
     */
    @Transactional
    fun callTraining(workflowTraceId: String, batchId: String, domainType: String, workflowEnable: Boolean) {
        try {
            val requestBody = mapOf(
                "domainType" to domainType,
                "workflowTraceId" to workflowTraceId,
                "batchId" to batchId,
                "workflowEnable" to workflowEnable,
                "workflowEvent" to Flow_Client_4.event
            )
            val message = "TRACE-ID: ${workflowTraceId} -calling agent training function for batch: ${batchId}, workflowEnabled: ${workflowEnable}"
            display(logger, message)
            val trainingUrl = urlService.getAgentUrl("training")
            val response: TrainingResponse = callerService.post(
                trainingUrl,
                requestBody,
                TrainingResponse::class.java
            )
            display(logger, "TRACE-ID: ${workflowTraceId} -received training result, status: ${response.status}, number of examples: ${response.numExamples}")
            if (response.status != "success") {
                val workflowModel = workflowModelLogsService.getTrainingWorkflowModelLogs(workflowTraceId)
                workflowModel.status = FAIL
                workflowModelLogsService.updateWorkflowModelLogs(workflowModel)
                workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_4)
                failure(logger, "TRACE-ID:  ${workflowTraceId} -Training failed")
            }
        }  catch (e: Exception) {
            workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_4)
            logger.error("TRACE-ID: {} - run call Training task with error: {}", workflowTraceId, e.message)
        }
    }

    /**
     * Gathers the locally trained model parameters and metrics, constructs a message,
     * and sends it to the central aggregation server.
     *
     * @param workflowTraceId The trace ID of the workflow whose model is being shared.
     */
    @Transactional
    fun shareLocalModelTaskToRemoteOrchestrationServer(workflowTraceId: String) {
        val modelTrainingResult = modelService.findModelTrainingResult(workflowTraceId)
        // If modelTrainingResult is null, set metrics to null
        val dataSeed = dataSeedsService.findDataSeedByWorkflowTraceId(workflowTraceId)
        val numberOfData = modelService.getPreditionCouns(workflowTraceId)
        val numExamples = modelTrainingResult?.numExamples?.takeIf { it > 0 } ?: numberOfData.toInt()
        val metrics: Map<String, Double>? = if (modelTrainingResult == null) {
            null  // Set metrics as null if no model training result
        } else {
            // If model training result exists, find the metrics list and include loss in the metrics
            val metricsList = modelService.findMetricsByWorkflowTraceIdAndSource(workflowTraceId, CLIENT)
            val dynamicMetrics = metricsList.associate { it.key to it.value }
            dynamicMetrics
        }
        logger.info("TRACE-ID: {} - found model training result, number of samples: {},  metrics: {}", workflowTraceId, numExamples, metrics)

        val clientId = urlService.getCurrentNodeNumber().toString()

        val modelClientRecord = modelService.findModelClientRecordByDomainAndName(dataSeed.domainType!!, dataSeed.model!!)
        val parameters = AppUtils.convertBlobToBase64String(modelClientRecord.localModelWeights!!)!!
        val trainingResult = TrainingResult(clientId, parameters, metrics, numExamples)
        val message = buildModelMessage(workflowTraceId, trainingResult, dataSeed.domainType!!)
        sendRequestToRemoteAggregateServer(workflowTraceId, message)
    }
    /**
     * Periodically requests the latest global model versions for all managed domains from the central server.
     * This method identifies which local workflows are awaiting a global update and triggers requests for them.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun requestLatestVersionFromRemoteOrchestrationServer() {

        val modelVersions = modelService.getAllModelLatestVersions()
        val clientId = urlService.getCurrentNodeNumber().toString()
        for (modelVersion in modelVersions) {
            val domain = modelVersion.domain
            val awaitingWorkflows = workflowService.findAwaitingRemoteGlobalAggregateWorkflows(modelVersion.modelId)
            var workflowTraceId = WORKFLOW_TRACE_ID_NONE
            val workflowTraceIds = mutableListOf<String>()
            for(awaitingWorkflow in awaitingWorkflows) {
                workflowTraceIds.add(awaitingWorkflow.workflowTraceId!!)
            }
            val version = modelVersion.version+1
            //let's start size for 1 now
            val size = 1
            val modelName = modelVersion.modelName
            if(workflowTraceIds.isNotEmpty()) {
                workflowTraceId = workflowTraceIds[0]
            }
            val aggregationReq = AggregationRequest(clientId, version, size, modelName)
            processAggregationRequest(workflowTraceId, domain, clientId, aggregationReq, size, modelName, workflowTraceIds)
        }
    }
    /**
     * Helper method to build and send an aggregation request for a specific domain.
     *
     * @param workflowTraceId The trace ID for context (can be NONE).
     * @param domain The domain for the request.
     * @param clientId The client's ID.
     * @param aggregationRequest The request object.
     * @param size The number of versions requested.
     * @param modelName The name of the model.
     * @param workflowTraceIds A list of all workflows that will be updated by this request.
     */
    fun processAggregationRequest(
        workflowTraceId: String, domain: String, clientId: String,
        aggregationRequest: AggregationRequest, size: Int, modelName: String,
        workflowTraceIds: List<String>) {
        val version = aggregationRequest.version
        val aggregationReq = AggregationRequest(clientId, version, size, modelName)
        val message = buildAggregationReqMessage(workflowTraceId, domain, aggregationReq)
        callRemoteAggGlobalModel(workflowTraceIds, message, version)
    }
    /**
     * Builds a `Message` object for requesting the latest aggregated model.
     *
     * @param workflowTraceId The workflow trace ID for the message header.
     * @param domain The domain for the message header.
     * @param aggregationReq The payload for the message body.
     * @return A fully constructed `Message`.
     */
    fun buildAggregationReqMessage(workflowTraceId: String, domain: String, aggregationReq: AggregationRequest): Message {
        val header = MessageBuilder.buildHeader(
            workflowTraceId = workflowTraceId,
            domain = domain,
            eventType = Flow_Client_6.event,
            status = INITIAL)
        val body = MessageBuilder.buildBody(mutableListOf(aggregationReq))
        val clientTrainingMessage = MessageBuilder.buildMessage<AggregationRequest>(
            id = AppUtils.getUuid8(),
            header = header,
            body = body
        )
        return clientTrainingMessage
    }
    /**
     * Builds a `Message` object for sending the local model update.
     *
     * @param workflowTraceId The workflow trace ID for the message header.
     * @param trainingResult The payload containing model parameters and metrics.
     * @param domain The domain for the message header.
     * @return A fully constructed `Message`.
     */
    fun buildModelMessage(workflowTraceId: String, trainingResult: TrainingResult, domain: String): Message {
        val header = MessageBuilder.buildHeader(
            workflowTraceId = workflowTraceId,
            domain = domain,
            eventType = Flow_Client_5.event,
            status = INITIAL
        )
        val body = MessageBuilder.buildBody(mutableListOf(trainingResult))
        val clientTrainingMessage = MessageBuilder.buildMessage<TrainingResult>(
            id = AppUtils.getUuid8(),
            header = header,
            body = body
        )
        return clientTrainingMessage
    }
    /**
     * Sends the local model update message to the central aggregation server and processes the acknowledgment.
     * On success, it completes the local evaluation workflow step. On failure, it marks the step as failed.
     * This method runs in a new transaction to ensure its outcome is committed independently.
     *
     * @param workflowTraceId The trace ID of the workflow.
     * @param message The `Message` object to send.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun sendRequestToRemoteAggregateServer(workflowTraceId: String, message: Message) {
        logger.info("TRACE-ID: {} - calling FL Aggregate", workflowTraceId)
        try {
            val jsonMessage = MessageBuilder.convertToJson(message)
            logger.info("aggregate message: {}", jsonMessage)
            val orchestratorServerSendModelUrl = urlService.getServerUrl("sentModel")
            val response: WebResponse = callerService.httpsPost(
                orchestratorServerSendModelUrl,
                jsonMessage,
                WebResponse::class.java
            )
            logger.info("TRACE-ID: {} - received FL Aggregate ACK, message: {}", workflowTraceId, response.message)
            if (!response.success!!) {
                failure(logger,"TRACE-ID: ${workflowTraceId} - calling FL Aggregate failed, response: ${response.success}")
                // Uncomment and complete the workflowService methods if needed
                 val workflowModel = workflowModelLogsService.getTrainingWorkflowModelLogs(workflowTraceId)
                 workflowModel.status = FAIL
                 workflowModelLogsService.updateWorkflowModelLogs(workflowModel)
                 workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_5)
            } else {
                clientWorkflowService.completeClientLocalModelEvaluation(workflowTraceId)
            }
        } catch (e: Exception) {
            failure(logger,"TRACE-ID: ${workflowTraceId} - calling FL Aggregate failed, error: ${e.message}")
            // Uncomment and complete the workflowService methods if needed
            val workflowModel = workflowModelLogsService.getTrainingWorkflowModelLogs(workflowTraceId)
            workflowModel.status = FAIL
            workflowModelLogsService.updateWorkflowModelLogs(workflowModel)
            workflowService.updateClientWorkflow(workflowTraceId, FAIL, Flow_Client_5)
        }
    }
    /**
     * Safely parses a `LinkedHashMap` from a generic response into a strongly-typed `AggregationResult` object.
     *
     * @param item The map to parse.
     * @return An `AggregationResult` object.
     * @throws IllegalArgumentException if required fields are missing or invalid.
     */
    fun parseAggregationResult(item: LinkedHashMap<String, Any>): AggregationResult {
        val historicResults = (item["historicResults"] as? List<*>)?.mapNotNull { element ->
            val map = element?.takeIf { it is Map<*, *> } as? Map<*, *>
            val typedMap: Map<String, Any> = map
                ?.filterKeys { it is String }
                ?.mapNotNull { (k, v) ->
                    val key = k as? String
                    if (key != null && v != null) key to v else null
                }?.toMap()
                ?: emptyMap()
            typedMap.let { parseAggregationResult(LinkedHashMap(it)) }
        }?.sortedBy { it.version } ?: emptyList()
        val metrics = (item["metrics"] as? Map<*, *>)?.mapNotNull { (k, v) ->
            (k as? String)?.let { key ->
                (v as? Number)?.toDouble()?.let { value ->
                    key to value
                }
            }
        }?.toMap() ?: throw IllegalArgumentException("Missing or invalid 'metrics'")
        return AggregationResult(
            version = (item["version"] as Int).toLong(),
            parameters = item["parameters"] as String,
            metrics = metrics,
            numExamples = (item["numExamples"] as Int),
            workflowTraceId = item["workflowTraceId"] as String,
            isSelf = (item["isSelf"] as Boolean),
            historicResults = historicResults
        )
    }
    /**
     * Safely parses a `LinkedHashMap` from a generic response into a strongly-typed `InitGlobalModelWeight` object.
     *
     * @param item The map to parse.
     * @return An `InitGlobalModelWeight` object.
     */
    fun parseInitialGlobalModelResult(item: LinkedHashMap<String, Any>): InitGlobalModelWeight {
        return InitGlobalModelWeight(
            version = (item["version"] as Int),
            parameters = item["parameters"] as String,
            modelDefinition = item["modelDefinition"] as String
        )
    }
    /**
     * Calls the central server to get the initial global model for a domain. If successful,
     * it saves the weights and definition to the client's database, bootstrapping the model.
     *
     * @param domain The domain for which to fetch the initial model.
     * @return A `GlobalModelWeightInitRes` indicating the status of the operation.
     */
    @Transactional()
    fun callRemoteAggInitialGlobalModel(domain: String): GlobalModelWeightInitRes {
        val globalModelWeightInitRes = GlobalModelWeightInitRes()
        try {
            val modelDef = modelService.findModelDefinition(domain)
            val modelName =modelDef.modelName!!
            val modelClientRecords = modelService.getModelClientRecordByDomain(domain)
            if (modelClientRecords == null || isBlobNullOrEmpty(modelClientRecords.globalModelWeights)) {
                // Call remote service here
                val initialGlobalModelWeightUrl = urlService.getServerUrl(
                    "initialGlobalModelWeight",
                    "domain" to domain
                )
                val msg: Message = callerService.httpsPost(
                    initialGlobalModelWeightUrl,
                    Message::class.java
                )
                if (msg.id != null) {
                    val body = msg.body!!
                    val bodyData = body.data?.mapNotNull { element ->
                        val map = element as? Map<*, *> ?: return@mapNotNull null
                        val safeMap = map.entries.mapNotNull { (k, v) ->
                            if (k is String && v != null) k to v else null
                        }.toMap()

                        if (safeMap.keys.containsAll(listOf("version", "parameters", "modelDefinition"))) {
                            LinkedHashMap(safeMap)
                        } else null
                    } ?: emptyList()
                    bodyData.forEach { item ->
                        val initialGlobalModelResult = parseInitialGlobalModelResult(item)
                        if (initialGlobalModelResult.parameters.isNotEmpty()) {
                            logger.info("Received initial ${domain} global model weight, ")
                            val parametersByteArray = convertBase64StringToBlob(initialGlobalModelResult.parameters)
                            val weighVersion = initialGlobalModelResult.version
                            val now = AppUtils.getCurrent()
                            if (modelClientRecords == null) {
                                val modelNewClientRecords = ModelClientRecordEntity()
                                modelNewClientRecords.name = modelName
                                modelNewClientRecords.domain = domain
                                modelNewClientRecords.definition = initialGlobalModelResult.modelDefinition
                                modelNewClientRecords.modelVersion = modelDef.modelVersion
                                modelNewClientRecords.globalModelWeights = parametersByteArray
                                modelNewClientRecords.localModelWeights = parametersByteArray
                                modelNewClientRecords.globalWeightsVersion = weighVersion.toLong()
                                modelNewClientRecords.localWeightsVersion = 0
                                modelNewClientRecords.createdDate = now
                                modelNewClientRecords.lastUpdateDate = now
                                modelService.saveModelClientRecord(modelNewClientRecords)
                            } else {
                                modelClientRecords.globalModelWeights = parametersByteArray
                                modelClientRecords.globalWeightsVersion = weighVersion.toLong()
                                if(isBlobNullOrEmpty(modelClientRecords.localModelWeights)) {
                                    modelClientRecords.localModelWeights = parametersByteArray
                                    modelClientRecords.localWeightsVersion = 0
                                    if(modelClientRecords.globalWeightsVersion ==null) {
                                        modelClientRecords.globalWeightsVersion = 0
                                    }
                                }
                                modelClientRecords.lastUpdateDate = now
                                modelService.saveModelClientRecord(modelClientRecords)
                            }
                            if(modelDef.modelDefinition.isNullOrEmpty()) {
                                modelDef.modelDefinition = initialGlobalModelResult.modelDefinition
                                modelService.saveModelDefinition(modelDef)
                            }
                            val agentModelLogs = modelService.findAgentModelLogs(modelName)
                            if(agentModelLogs==null) {
                                val agentModelLog = AgentModelLogsEntity()
                                agentModelLog.modelId = modelDef.id
                                agentModelLog.localWeightsVersion = 0
                                agentModelLog.globalWeightsVersion = 0
                                modelService.saveAgentModelLogs(agentModelLog)
                                logger.info(LOG_INITIAL_WEIGHT_SAVED, agentModelLog.modelId)
                            }
                            globalModelWeightInitRes.success = true
                            globalModelWeightInitRes.status = GlobalModelWeightInitRes.Status.SUCCESS
                            success(logger, "Success synced with aggregate server for ${domain} initial global model weight ")
                        }
                    }
                } else {
                    globalModelWeightInitRes.success = false
                    globalModelWeightInitRes.status = GlobalModelWeightInitRes.Status.NO_RESULT_FOUND
                    display(logger, "${domain} global model weight not found, switch to local initial weight process")
                    val agentModelLogs = modelService.findAgentModelLogs(modelName)
                    if(agentModelLogs==null) {
                        val agentModelLog = AgentModelLogsEntity()
                        agentModelLog.modelId = modelDef.id
                        agentModelLog.localWeightsVersion = 0
                        agentModelLog.globalWeightsVersion = 0
                        modelService.saveAgentModelLogs(agentModelLog)
                        logger.info("InitialWeight updated local weight, ModelLog saved with modelId: {}", agentModelLog.modelId)
                    }
                }
            } else {
                globalModelWeightInitRes.success = true
                globalModelWeightInitRes.status = GlobalModelWeightInitRes.Status.ALREADY_INITIALIZED
                success(logger, "${domain} global model weight already initialized")
            }

        } catch (e: Exception) {
            logger.error("calling Receive FL Aggregate model failed with error: {}", e.message)
            globalModelWeightInitRes.success = false
            globalModelWeightInitRes.status = GlobalModelWeightInitRes.Status.REMOTE_CALL_FAILED
            failure(logger, "${domain} global model weight initialize failed")
        }
        return globalModelWeightInitRes
    }
    /**
     * Calls the central server to request the latest aggregated global model and processes the response.
     * The response may contain the latest model and/or historical results, each of which is processed
     * and persisted. It then completes all local workflows that were awaiting this update.
     *
     * @param workflowTraceIds A list of all workflows that will be updated by this request.
     * @param message The request `Message` object.
     * @param queryVersion The version number being requested.
     */
    @Transactional()
    fun callRemoteAggGlobalModel(workflowTraceIds: List<String>, message: Message, queryVersion: Long) {
        var workflowTraceId = WORKFLOW_TRACE_ID_NONE
        if (workflowTraceIds.isNotEmpty()) {
            workflowTraceId = workflowTraceIds[0]
        }
        try {
            val jsonMessage = MessageBuilder.convertToJson(message)
            val orchestratorServerReceiveModelUrl = urlService.getServerUrl("receiveModel")
            val msg: Message = callerService.httpsPost(
                orchestratorServerReceiveModelUrl,
                jsonMessage,
                Message::class.java
            )
            if (msg.id != null) {
                val header = msg.header!!
                val domain = header.domain!!
                val body = msg.body!!
                val bodyData: List<LinkedHashMap<String, Any>> = when (val rawData = body.data) {
                    is List<*> -> rawData.mapNotNull { item ->
                        val map = item as? Map<*, *> ?: return@mapNotNull null
                        val safeMap = map.entries.mapNotNull { (k, v) ->
                            if (k is String && v != null) k to v else null
                        }.toMap()
                        LinkedHashMap(safeMap)
                    }
                    else -> emptyList()
                }

                bodyData.forEach { item ->
                    val aggregationResult = parseAggregationResult(item)
                    val historicResults = aggregationResult.historicResults
                    val receivedVersion = aggregationResult.version;
                    val modelDefinition = modelService.getModelDefinition(domain)
                    val modelId = modelDefinition.id
                    val modelName = modelDefinition.modelName!!
                    val currentMaxVersion = modelService.findMaxGlobalModelVersion(modelId)!!
                    if (historicResults.isNotEmpty()) {
                        historicResults
                            .filter { it.version > currentMaxVersion }
                            .forEach { historicResult ->
                                processAggregationResult(historicResult, modelId, domain, modelName, true)
                            }
                    }
                    if (aggregationResult.version > currentMaxVersion) {
                        val receivedWorkflowTraceId = aggregationResult.workflowTraceId
                        display(logger, "--------- TRACE-ID: $workflowTraceId, currentMaxVersion: $currentMaxVersion, received Version: $receivedVersion, server-trace: $receivedWorkflowTraceId --------")
                        processAggregationResult(aggregationResult, modelId, domain, modelName, false)

                        val awaitingWfList =  workflowService.findAwaitingRemoteGlobalAggregateWorkflows(modelId)
                        logger.info("TRACE-ID: {} - All awaiting workflows: {}, modelId: {}", receivedWorkflowTraceId, awaitingWfList.size, modelId)
                        var found = false
                        for (workflow in awaitingWfList) {
                            logger.info("TRACE-ID: {} - Update workflow", workflow.workflowTraceId)
                            if (workflow.workflowTraceId == receivedWorkflowTraceId) {
                                found = true
                            }
                            clientWorkflowService.completeClientGlobalModelAggregate(workflow.workflowTraceId!!)
                        }
                        if (!found) {
                            logger.info("TRACE-ID: {} - Received trace ID not found in awaiting workflows, calling completeClientGlobalModelAggregate for it.", receivedWorkflowTraceId)
                            clientWorkflowService.completeClientGlobalModelAggregate(receivedWorkflowTraceId)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("TRACE-ID: {} - calling Receive FL Aggregate model failed with error: {}", workflowTraceId, e.message)
        }
    }
    /**
     * Helper method to process a single `AggregationResult` from the central server. It transactionally
     * saves the metrics, weights, and training results, and updates the main client record and agent logs.
     *
     * @param aggregationResult The result to process.
     * @param modelId The ID of the model.
     * @param domain The learning domain.
     * @param modelName The name of the model.
     */
    @Transactional
    fun processAggregationResult(aggregationResult: AggregationResult, modelId: Long, domain: String, modelName: String, historical: Boolean) {
        val receivedWorkflowTraceId = aggregationResult.workflowTraceId
        // Save each metric in GlobalMetrics
        aggregationResult.metrics.forEach { (key, value) ->
            val globalMetric = GlobalMetrics(
                workflowTraceId = receivedWorkflowTraceId,
                source = "SERVER",
                key = key,
                value = value
            )
            modelService.saveGlobalMetrics(globalMetric)
            logger.info("TRACE-ID: {} - Saved metric - {}: {}", receivedWorkflowTraceId, key, value)
        }
        val now = AppUtils.getCurrent()!!
        val shortWeight = getNStrings(aggregationResult.parameters, 15)
        logger.info(
            "TRACE-ID: {} - version: {}, numExamples: {}, parameters: {}",
            receivedWorkflowTraceId, aggregationResult.version, aggregationResult.numExamples, shortWeight)

        val parametersByteArray = convertBase64StringToBlob(aggregationResult.parameters)

        val globalModelWeights = GlobalModelWeightsEntity().apply {
            this.modelId = modelId
            this.version = aggregationResult.version
            this.isSelf = if (aggregationResult.isSelf) "T" else "F"
            this.parameters = parametersByteArray
            this.workflowTraceId = receivedWorkflowTraceId
            this.createdDate = now
            this.lastUpdateDate = now
        }
        modelService.saveGlobalModelWeight(globalModelWeights)
        logger.info("TRACE-ID: {} - save GlobalModelWeight", receivedWorkflowTraceId)

        val globalModelTrainingResult = GlobalModelTrainingResult().apply {
            this.workflowTraceId = receivedWorkflowTraceId
            this.modelId = modelId
            this.loss = aggregationResult.metrics["loss"]?.toBigDecimal() ?: BigDecimal.ZERO
            this.numExamples = aggregationResult.numExamples
            this.createdDate = now
        }
        modelService.saveGlobalModelTrainingResult(globalModelTrainingResult)
        logger.info("TRACE-ID: {} - save GlobalModelTrainingResult", receivedWorkflowTraceId)

        val modelClientRecord = modelService.findModelClientRecordByDomainAndName(domain, modelName).apply {
            this.globalWeightsVersion = aggregationResult.version
            this.globalModelWeights = parametersByteArray
            this.lastUpdateDate = now
        }
        modelService.saveModelClientRecord(modelClientRecord)
        logger.info("TRACE-ID: {} - save ModelClientRecord", receivedWorkflowTraceId)

        val agentModelLogsEntity = modelService.findAgentModelLogs(modelName)
        if (agentModelLogsEntity != null) {
            agentModelLogsEntity.globalWeightsVersion = aggregationResult.version
            modelService.saveAgentModelLogs(agentModelLogsEntity)
        } else {
            val newAgentModelLogsEntity = AgentModelLogsEntity().apply {
                this.modelId = modelId
                this.globalWeightsVersion = aggregationResult.version
                this.localWeightsVersion = 1
            }
            modelService.saveAgentModelLogs(newAgentModelLogsEntity)
        }
    }
    companion object {
        private const val LOG_INITIAL_WEIGHT_SAVED = "InitialWeight updated local weight, ModelLog saved with modelId: {}"
    }
}