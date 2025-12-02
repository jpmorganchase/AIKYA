package com.aikya.orchestrator.service

import com.aikya.orchestrator.agent.model.GlobalModelWeightsEntity
import com.aikya.orchestrator.client.model.fedlearn.ModelClientRecordEntity
import com.aikya.orchestrator.client.model.fedlearn.ModelClientRecordHistoryEntity
import com.aikya.orchestrator.client.model.fedlearn.ModelFeedbackEntity
import com.aikya.orchestrator.client.model.fedlearn.ModelPredictionDataEntity
import com.aikya.orchestrator.client.model.seeds.DataSeedMetaDataEntity
import com.aikya.orchestrator.dto.DashboardSummaryDTO
import com.aikya.orchestrator.dto.DataSeedLabelRequest
import com.aikya.orchestrator.dto.common.*
import com.aikya.orchestrator.dto.fedlearn.FeedbackRequest
import com.aikya.orchestrator.dto.fedlearn.FeedbackResponse
import com.aikya.orchestrator.dto.fedlearn.ModelResponse
import com.aikya.orchestrator.dto.fedlearn.ModelVersionTrackResponse
import com.aikya.orchestrator.dto.seeds.DataLoadRequest
import com.aikya.orchestrator.dto.seeds.DataSeedMetaData
import com.aikya.orchestrator.dto.workflow.MlModelTrack
import com.aikya.orchestrator.dto.workflow.PendingWorkflowDTO
import com.aikya.orchestrator.dto.workflow.PendingWorkflowResponse
import com.aikya.orchestrator.dto.workflow.WorkflowDetailDTO
import com.aikya.orchestrator.repository.agent.OrchestratorAgentQueryRepository
import com.aikya.orchestrator.service.auth.AuthService
import com.aikya.orchestrator.service.common.GlobalMemorySet
import com.aikya.orchestrator.service.common.QueryLoaderService
import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.shared.model.workflow.PendingWorkflow
import com.aikya.orchestrator.shared.model.workflow.WorkflowEntity
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_3
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_8
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.PENDING
import com.aikya.orchestrator.utils.AppConstants.RunModeOptionsEnum
import com.aikya.orchestrator.utils.AppConstants.allClientEventsFlow
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.AppUtils.dateToYYYYMMDDHHMMSS
import com.aikya.orchestrator.utils.AppUtils.toCurrentDateString
import com.aikya.orchestrator.utils.AppUtils.truncateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
/**
 * Facade service that acts as a primary entry point for the client-side UI. 🖥️
 *
 * This class orchestrates operations by delegating calls to various specialized services.
 * It simplifies the client's interaction with the backend by providing a unified interface for:
 * - Fetching dashboard data and workflow summaries.
 * - Managing client registration and model initialization.
 * - Handling data loading and user feedback submissions.
 * - Triggering local training processes.
 * - Providing administrative functions like resetting the client's state.
 *
 * @param authService Service for handling user authentication and authorization.
 * @param queryLoaderService Service for loading predefined SQL queries from files.
 * @param modelService Service for managing model definitions, weights, and records.
 * @param workflowService Service for managing the lifecycle of workflows.
 * @param dataSeedsService Service for handling data seed metadata.
 * @param clientWorkflowService Service for client-specific workflow operations.
 * @param workflowModelLogsService Service for logging workflow and model activities.
 * @param clientCallService Service for making outbound calls to other system components.
 */
@Suppress("cognitive-complexity")
@Service
class ClientFacadeService(
    private val authService: AuthService,
    private val queryLoaderService: QueryLoaderService,
    private val modelService: ModelService,
    private val workflowService: WorkflowService,
    private val dataSeedsService: DataSeedsService,
    private val clientWorkflowService: ClientWorkflowService,
    private val workflowModelLogsService: WorkflowModelLogsService,
    private val clientCallService: OrchestrationClientCallService
) {
    private val logger: Logger = LoggerFactory.getLogger(ClientFacadeService::class.java)

    @Autowired
    @Qualifier("orchestratorAgentQueryRepository")
    private lateinit var agentQueryRepository: OrchestratorAgentQueryRepository

    @Value("\${app.domains}")
    private lateinit var domains: String

    @Value("\${app.node.name}")
    val currentNode = ""

    @Value("\${app.node.number}")
    val currentNodeNumber = ""

    @Value("\${app.node.mail}")
    val currentNodeMail = ""
    /**
     * Retrieves a list of all supported learning domains.
     *
     * @return A list of `DomainDTO` objects.
     */
    fun getAllDomains(): List<DomainDTO> {
        return modelService.getAllDomains()
    }
    /**
     * Gathers data for the main dashboard view for a specific domain.
     * It lists pending workflows and their associated data batch information.
     *
     * @param domainType The domain for which to retrieve dashboard data.
     * @return A sorted list of `DashboardSummaryDTO` objects representing pending items.
     */
    fun getModelDashBoard(domainType: String): List<DashboardSummaryDTO> {
        val dashboardList = mutableListOf<DashboardSummaryDTO>()
        val modelDefinition = modelService.getModelDefinition(domainType)
        val pendingWorkflows = workflowService.getAllPendingWorkflows(modelDefinition.id)
        val workflowTraceIds = mutableListOf<String>()
        for (row in pendingWorkflows) {
            workflowTraceIds.add(row.workflowTraceId!!)
        }
        if (workflowTraceIds.isNotEmpty()) {
            val dataSeeds = dataSeedsService.findDataSeedsByWorkflowTraceIdIn(workflowTraceIds)
            for (dataSeed in dataSeeds) {
                val dashboardSummary = DashboardSummaryDTO()
                dashboardSummary.batch_id = dataSeed.batchId!!
                dashboardSummary.workflowTraceId = dataSeed.workflowTraceId!!
                dashboardSummary.name = dataSeed.label!!
                dashboardSummary.createdDate = dataSeed.createdDate!!
                dashboardSummary.status = PENDING
                dashboardSummary.fileName = dataSeed.fileName!!
                dashboardList.add(dashboardSummary)
            }
        }
        val sortedList = dashboardList.sortedBy { it.createdDate }
        return sortedList

    }
    /**
     * Gets metadata for the initial data seeds available for a given domain.
     *
     * @param domainType The domain to query.
     * @return A `Mono` emitting a list of `DataSeedMetaData`.
     */
    fun getInitialDataSeeds(domainType: String): Mono<List<DataSeedMetaData>> {
        val dataList = dataSeedsService.getInitialDataSeeds(domainType)
        return Mono.just(dataList)
    }
    /**
     * Retrieves all pending workflow entities for a given domain.
     *
     * @param domainType The domain to query.
     * @return A list of `WorkflowEntity` objects with a 'PENDING' status.
     */
    fun getAllPendingWorkflows(domainType: String): List<WorkflowEntity> {
        val modelDefinition = modelService.getModelDefinition(domainType)
        return workflowService.getAllPendingWorkflows(modelDefinition.id)
    }
    /**
     * Fetches the model definition for a given domain.
     *
     * @param domainType The domain for which to get the model definition.
     * @return A `ModelResponse` containing the domain and its model definition string.
     */
    fun getModel(domainType: String): ModelResponse {
        val modelDefinition = modelService.getModelDefinition(domainType)
        val modelDef = modelDefinition.modelDefinition!!
        return ModelResponse(domainType, modelDef)
    }
    /**
     * Triggers the process to register the current client node with the central orchestrator.
     */
    @Transactional
    fun registerClient() {
        val nodeName = currentNode
        val nodeNumber = currentNodeNumber
        clientCallService.callRegisterClient(nodeName, nodeNumber.toInt(), currentNodeMail)
    }
    /**
     * Initiates requests to the central orchestrator to fetch the initial global model weights
     * for all configured domains. This is a key step in bootstrapping the client.
     */
    @Transactional
    fun initialGlobalModelWeight() {
        val domainList = getDomainList()
        val dbDomains = modelService.getAllDomains().mapNotNull { it.name }
        domainList.forEach { domainName ->
            if (!dbDomains.contains(domainName)) {
                logger.info("Skipping domain: {} as it is not defined in the database.", domainName)
                return@forEach // Continue to the next domain
            }
            logger.info("Processing initial GlobalModelWeight domain: {}.", domainName)
            clientCallService.callRemoteAggInitialGlobalModel(domainName)
            initialAgentModelLogs()
        }
    }
    /**
     * Ensures the local agent's model logs and records are initialized. If the local model weight
     * or definition is missing, it triggers a call to fetch the initial weights from the orchestrator.
     */
    @Transactional
    fun initialAgentModelLogs() {
        val domainList = getDomainList()
        domainList.forEach { domainName ->
            logger.info("Processing model domain: {}.", domainName)
            val modelDef = modelService.findModelDefinition(domainName)
            val modelClientRecords = modelService.getModelClientRecordByDomain(domainName)

            when {
                modelClientRecords == null -> {
                    logger.info("Model domain definition is empty, loading definition")
                    clientCallService.callInitialWeight(domainName, modelDef.modelName!!, modelDef.modelVersion!!)
                }
                modelClientRecords.localModelWeights == null -> {
                    logger.info("Updating {} Model Client Record for local model weight", domainName)
                    clientCallService.callInitialWeight(domainName, modelDef.modelName!!, modelDef.modelVersion!!)
                }
                modelClientRecords.definition.isNullOrEmpty() -> {
                    logger.info("Updating {} Model Client Record for model definition", domainName)
                    clientCallService.callInitialWeight(domainName, modelDef.modelName!!, modelDef.modelVersion!!)
                }
                else -> {
                    logger.info("Domain: {} model weight is updated.", domainName)
                }
            }
        }
    }
    /**
     * Triggers the loading of local data for a training or prediction task.
     *
     * @param dataLoadRequest The request containing details about the data to load.
     * @return A `WebResponse` indicating the outcome of the operation.
     */
    @Transactional
    fun loadLocalData(dataLoadRequest: DataLoadRequest): WebResponse {
        return clientCallService.callDataInitLoad(dataLoadRequest)
    }
    /**
     * Skips the manual user feedback step in a workflow, advancing it to the next stage.
     *
     * @param workflowTraceId The trace ID of the workflow to modify.
     */
    @Transactional
    fun skipSendFeedback(workflowTraceId: String) {
        workflowService.moveMultipleStepsByTraceId(workflowTraceId, Flow_Client_3, 2)
    }
    /**
     * Processes and saves user-provided feedback on model predictions. This action completes
     * the feedback step in the workflow. It uses an in-memory set to prevent duplicate submissions
     * from the UI.
     *
     * @param feedbackReq The request containing the feedback data.
     * @return A `FeedbackResponse` summarizing the result of the submission.
     */
    @Transactional
    fun sendFeedback(feedbackReq: FeedbackRequest): FeedbackResponse {
        val response = FeedbackResponse()
        try {
            val batchId = feedbackReq.batchId!!
            // Check if batchId already exists
            if (GlobalMemorySet.isBatchIdExist(batchId)) {
                response.message = "Batch ID: ${batchId} already processed."
                response.success = false
                return response
            }
            // Add batchId to the set to mark it as processed, this will prevent UI double submit
            GlobalMemorySet.addBatchId(batchId)
            val dataSeed = dataSeedsService.findDataSeedsByBatchId(batchId)
            val workflowTraceId = dataSeed.workflowTraceId!!
            val domainType = dataSeed.domainType!!
            response.batchId = batchId
            response.workflowTraceId = workflowTraceId
            response.domainType = domainType
            val modelDataIds = feedbackReq.feedbacks.map { it.modelDataId }
            val modelPredictionDataEntities = findModelPredictionDataByIdsInChunks(modelDataIds, batchId)
            if (modelPredictionDataEntities.isEmpty()) {
                response.success = false
                response.message = "feedback data size is empty, check your submission"
                return response
            }
            val modelFeedbackEntities = mutableListOf<ModelFeedbackEntity>()
            val now = AppUtils.getCurrent()
            for (feedback in feedbackReq.feedbacks) {
                val modelDataId = feedback.modelDataId
                val modelPredictionDataEntity = modelPredictionDataEntities.find { it.id == modelDataId }

                if (modelPredictionDataEntity != null) {
                    val existingFeedback = modelService.findModelFeedbackByModelDataId(modelDataId)
                    val isCorrect = if (feedback.isCorrect!!) "Y" else "N"
                    if (existingFeedback.isPresent) {
                        val mlFeedbackData = existingFeedback.get()
                        mlFeedbackData.isCorrect = isCorrect
                        mlFeedbackData.status = 0
                        modelFeedbackEntities.add(mlFeedbackData)
                    } else {
                        val mlNewDataFeedback = ModelFeedbackEntity(
                            modelDataId = modelPredictionDataEntity.id!!,
                            batchId = feedbackReq.batchId!!,
                            workflowTraceId = modelPredictionDataEntity.workflowTraceId,
                            itemId = modelPredictionDataEntity.itemId?.toLong() ?: 0,
                            score = feedback.score,
                            isCorrect = isCorrect,
                            comment = feedback.comment, // Add comment field if necessary
                            status = 0,
                            createdDate = now
                        )
                        modelFeedbackEntities.add(mlNewDataFeedback)
                    }
                }
            }
            logger.info("TRACE-ID: {} -modelFeedback size: {}", workflowTraceId, modelFeedbackEntities.size)
            if (modelFeedbackEntities.isNotEmpty()) {
                modelService.saveFeedbacks(modelFeedbackEntities)
                logger.info("TRACE-ID: {} - saved feedback", workflowTraceId)
                workflowService.updateClientWorkflow(workflowTraceId, COMPLETE, Flow_Client_3)
            } else {
                response.success = false
                response.message = "no model feedback data found, check your submission"
                logger.info("TRACE-ID: {} -no modelFeedback found", workflowTraceId)
            }
            response.message = "saved feedback data, size: " + modelFeedbackEntities.size
        } catch (e: Exception) {
            val msg = "Failed to submit feedback: ${e.message}"
            logger.error(msg, e)
            response.message = msg
            response.success = false
        }
        return response
    }
    /**
     * Private helper to fetch model prediction data by IDs in manageable chunks to avoid
     * overwhelming the database with a large IN clause.
     *
     * @param modelDataIds The list of entity IDs to fetch.
     * @param batchId The batch ID to scope the search.
     * @return A list of `ModelPredictionDataEntity`.
     */
    private fun findModelPredictionDataByIdsInChunks(
        modelDataIds: List<Long>,
        batchId: String
    ): List<ModelPredictionDataEntity> {
        val chunkSize = 500
        val modelPredictionDataEntities = mutableListOf<ModelPredictionDataEntity>()

        modelDataIds.chunked(chunkSize).forEach { chunk ->
            modelPredictionDataEntities.addAll(modelService.findModelPredictionDataByIds(chunk, batchId))
        }

        return modelPredictionDataEntities
    }
    /**
     * Initiates a local model training process after feedback has been successfully submitted.
     *
     * @param feedbackRes The response from the feedback submission, containing necessary context.
     * @return A `WebResponse` indicating the outcome of the training call.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun callTraining(feedbackRes: FeedbackResponse): WebResponse {
        val res = WebResponse()
        res.message = feedbackRes.message
        try {
            clientCallService.callTraining(
                feedbackRes.workflowTraceId!!,
                feedbackRes.batchId!!, feedbackRes.domainType!!, true
            )
        } catch (e: Exception) {
            logger.error("Failed to call training", e)
            res.message = e.message
            res.success = false
        }
        return res
    }
    /**
     * Gets a detailed summary for a specific pending workflow, identified by its trace ID.
     *
     * @param workflowTraceId The unique trace ID of the workflow.
     * @return A `PendingWorkflowResponse` containing the workflow details.
     */
    fun getPendingWorkflowSummaryByTraceId(workflowTraceId: String): PendingWorkflowResponse {
        val dataSeed = dataSeedsService.findDataSeedByWorkflowTraceId(workflowTraceId)
        val domain = dataSeed.domainType!!
        return getPendingWorkflowSummary(domain)
    }
    /**
     * Retrieves and formats the status of the latest pending or completed client workflow
     * for a given domain, suitable for display on the UI.
     *
     * @param domain The domain to query.
     * @return A `PendingWorkflowResponse` containing the formatted workflow steps and status.
     */
    fun getPendingWorkflowSummary(domain: String): PendingWorkflowResponse {
        val pendingWorkflowDataRes = PendingWorkflowResponse()
        val pendingWorkflows = getPendingClientWorkflow(domain).toList()
        if (pendingWorkflows.isNotEmpty()) {
            //only display the latest
            val finalDisplay = pendingWorkflows[0]
            val dataSeed = dataSeedsService.findOptionalDataSeedByWorkflowTraceId(finalDisplay.workflowTraceId!!)
            if (dataSeed != null) {
                finalDisplay.trainingDate = dateToYYYYMMDDHHMMSS(dataSeed.createdDate)
            }
            handleRemainInitWorkflowSteps(finalDisplay)
            val uiWorkflowSteps = handleFinalUIDisplay(finalDisplay)
            pendingWorkflowDataRes.result.add(uiWorkflowSteps)
        } else {
            val completeWorkflows = getCompletedClientWorkflow(domain).toList()
            if (completeWorkflows.isNotEmpty()) {
                //only display the latest
                val finalDisplay = pendingWorkflows[0]
                val uiWorkflowSteps = handleFinalUIDisplay(finalDisplay)
                pendingWorkflowDataRes.result.add(uiWorkflowSteps)
            }
        }
        val globalVersion = modelService.findGlobalModelLogsByDomain(domain)
        pendingWorkflowDataRes.version = "v-" + globalVersion.globalWeightsVersion
        return pendingWorkflowDataRes

    }
    /**
     * Private helper to transform raw workflow steps into a simplified, consolidated view
     * for the user interface. It groups related steps (e.g., 3-7 into a single "Share Insights" step)
     * and sets appropriate statuses and labels for display.
     *
     * @param finalDisplay The `PendingWorkflowDTO` with raw step data.
     * @return A new `PendingWorkflowDTO` with steps formatted for the UI.
     */
    fun handleFinalUIDisplay(finalDisplay: PendingWorkflowDTO): PendingWorkflowDTO {
        val uIPendingWorkflowData = PendingWorkflowDTO()
        uIPendingWorkflowData.workflowId = finalDisplay.workflowId
        uIPendingWorkflowData.workflowTraceId = finalDisplay.workflowTraceId
        uIPendingWorkflowData.version = finalDisplay.version
        uIPendingWorkflowData.dataLoadDate = finalDisplay.dataLoadDate
        uIPendingWorkflowData.trainingDate = finalDisplay.trainingDate
        uIPendingWorkflowData.workflowStatus = finalDisplay.workflowStatus
        val finalSteps = mutableListOf<WorkflowDetailDTO>()
        val uniqueStepsSet = mutableSetOf<Int>()
        var lastStepBetween3And7: WorkflowDetailDTO? = null
        var isPendingStepInRange = false

        // Loop through steps and handle the logic
        for (step in finalDisplay.steps ?: listOf()) {
            if (uniqueStepsSet.contains(step.step)) {
                continue
            }
            when (step.step) {
                1, 2 -> {
                    // Always include step 1 and step 2 as they are
                    finalSteps.add(step)
                    uniqueStepsSet.add(step.step)
                }

                in 3..7 -> {
                    // Track the last step between 3 and 7
                    if (step.stepStatus == "Pending") {
                        isPendingStepInRange = true
                        lastStepBetween3And7 = step
                    } else if (!isPendingStepInRange) {
                        lastStepBetween3And7 = step
                    }
                }

                8 -> {
                    // Step 8 should always be included, but check if it's "Complete"
                    if (step.stepStatus == "Complete") {
                        uIPendingWorkflowData.workflowStatus = "Complete"
                        uIPendingWorkflowData.currentStep = 8
                    } else if (finalDisplay.currentStep in 6..7) {
                        step.stepStatus = "Pending"
                    }
                    uniqueStepsSet.add(step.step)
                }
            }
        }
        lastStepBetween3And7?.let {
            when (finalDisplay.currentStep) {
                2, 3 -> {
                    it.step = 5 // If current step is 2 or 3, set last step to 5
                    uIPendingWorkflowData.currentStep = 5 // Also set currentStep to 5
                }

                5, 6 -> { // When current step is 5 or 6, the displayed step is 6
                    it.step = 6
                    uIPendingWorkflowData.currentStep = 6
                }

                7 -> {
                    it.step = 7 // If current step is 7, set it to step 7
                    uIPendingWorkflowData.currentStep = 7 // Set currentStep to 7
                }
            }
            it.label = "Share Insights"
            if (!uniqueStepsSet.contains(it.step)) {
                finalSteps.add(it)
                uniqueStepsSet.add(it.step) // Mark the aggregate step as added
            }
        }
        // Add step 8, with the updated label
        val step8 = finalDisplay.steps?.find { it.step == 8 }
        step8?.let {
            it.label = "Receive & Update Model"
            if (uIPendingWorkflowData.currentStep == 8) {
                it.stepStatus = "Pending" // Set to Pending when currentStep is 8
            } else if (uIPendingWorkflowData.currentStep <= 7) {
                it.stepStatus = "Initial" // Set to Initial when currentStep is <= 7
            }
            finalSteps.add(it)
        }
        // Ensure the steps are sorted by their step number
        uIPendingWorkflowData.steps = finalSteps.sortedBy { it.step }.toMutableList()
        if (finalDisplay.currentStep == 3) {
            uIPendingWorkflowData.currentStep = 5
        } else if (finalDisplay.currentStep == 5) {
            uIPendingWorkflowData.currentStep = 6
        } else {
            // Otherwise, keep the current step as is
            uIPendingWorkflowData.currentStep = finalDisplay.currentStep
        }
        if (uIPendingWorkflowData.workflowStatus == "Pending") {
            val step7 = uIPendingWorkflowData.steps?.find { it.step == 7 }
            val step8Flow = uIPendingWorkflowData.steps?.find { it.step == 8 }

            if (step7?.stepStatus == "Complete" && step8Flow != null) {
                step8Flow.stepStatus = "Pending"
            }
        }
        return uIPendingWorkflowData
    }
    /**
     * Private helper that populates a workflow DTO with placeholder steps for future stages
     * that have not yet started. This ensures the UI can render a complete timeline.
     *
     * @param finalDisplay The `PendingWorkflowDTO` to be populated.
     */
    fun handleRemainInitWorkflowSteps(finalDisplay: PendingWorkflowDTO) {
        val size = finalDisplay.steps?.size ?: 0
        val lastClientWorkflowStep = Flow_Client_8.step
        if (size < lastClientWorkflowStep) {
            val lastCurrentStepWorkflow = finalDisplay.steps?.getOrNull(size - 1)
            val lastCurrentStep = lastCurrentStepWorkflow?.step ?: 0

            // Update the current last step's status to "Pending"
            if (lastCurrentStepWorkflow != null && lastCurrentStepWorkflow.stepStatus == "Initial") {
                lastCurrentStepWorkflow.stepStatus = "Pending"
            }
            for (start in (lastCurrentStep + 1)..lastClientWorkflowStep) {
                val flowEvent = allClientEventsFlow.find { it.step == start }
                if (flowEvent != null) {
                    val workflowDetailData = WorkflowDetailDTO().apply {
                        id = 0
                        step = flowEvent.step
                        stepDesc = flowEvent.description
                        source = flowEvent.source
                        target = flowEvent.target
                        stepStatus = "Initial"
                        label = flowEvent.label
                        createdDate = toCurrentDateString()
                        lastUpdateDate = toCurrentDateString()
                    }
                    finalDisplay.steps?.add(workflowDetailData)
                }
            }
        }
    }
    /**
     * Retrieves a list of completed client workflows for a given domain.
     *
     * @param domain The domain to query.
     * @return A list of `PendingWorkflowDTO` objects representing completed workflows.
     */
    fun getCompletedClientWorkflow(domain: String): List<PendingWorkflowDTO> {
        val getCompletedWorkflowSql = queryLoaderService.getQuery("getCompletedClientWorkflow")
        val pendingWorkflowList = agentQueryRepository.nativeQueryForResultList(
            getCompletedWorkflowSql,
            PendingWorkflow::class.java
        ).toList()
        return buildClientWorkflow(pendingWorkflowList).toList()
    }
    /**
     * Retrieves a list of pending client workflows for a given domain.
     *
     * @param domain The domain to query.
     * @return A list of `PendingWorkflowDTO` objects representing pending workflows.
     */
    fun getPendingClientWorkflow(domain: String): List<PendingWorkflowDTO> {
        val getPendingWorkflowSql = queryLoaderService.getQuery("getPendingClientWorkflow")
        val pendingWorkflowList = agentQueryRepository.nativeQueryForResultList(
            getPendingWorkflowSql,
            PendingWorkflow::class.java
        ).toList()
        return buildClientWorkflow(pendingWorkflowList).toList()
    }
    /**
     * Helper method to convert a raw list of workflow data from the database into a
     * structured list of `PendingWorkflowDTO` objects, grouping steps by workflow ID.
     *
     * @param workflowList The raw list of `PendingWorkflow` data.
     * @return A mutable list of structured `PendingWorkflowDTO` objects.
     */
    fun buildClientWorkflow(workflowList: List<PendingWorkflow>): MutableList<PendingWorkflowDTO> {
        val resultDataList = mutableListOf<PendingWorkflowDTO>()
        val workflowSet = mutableSetOf<Long>()
        var pendingWorkflowData: PendingWorkflowDTO? = null;
        for (pendingworkflow in workflowList) {
            val wfId = pendingworkflow.workflowId
            if (!workflowSet.contains(wfId)) {
                workflowSet.add(wfId)
                pendingWorkflowData = PendingWorkflowDTO()
                pendingWorkflowData.workflowId = wfId
                pendingWorkflowData.workflowTraceId = pendingworkflow.workflowTraceId
                pendingWorkflowData.currentStep = pendingworkflow.currentStep
                pendingWorkflowData.workflowStatus = pendingworkflow.workflowStatus
                resultDataList.add(pendingWorkflowData)
            }
            val workflowDetailData = WorkflowDetailDTO()
            workflowDetailData.id = pendingworkflow.workflowDetailId
            workflowDetailData.source = pendingworkflow.source
            workflowDetailData.target = pendingworkflow.target
            workflowDetailData.step = pendingworkflow.step
            workflowDetailData.stepDesc = pendingworkflow.stepDesc
            workflowDetailData.stepStatus = pendingworkflow.stepStatus
            workflowDetailData.createdDate = dateToYYYYMMDDHHMMSS(pendingworkflow.createdDate)
            workflowDetailData.lastUpdateDate = dateToYYYYMMDDHHMMSS(pendingworkflow.lastUpdateDate)
            if (Flow_Client_3.step == workflowDetailData.step && (PENDING == workflowDetailData.stepStatus || INITIAL == workflowDetailData.stepStatus)) {
                workflowDetailData.label = "Waiting for user feedback"
            } else {
                workflowDetailData.label = pendingworkflow.label
            }
            if (pendingWorkflowData != null) {
                pendingWorkflowData.steps?.add(workflowDetailData)
            }

        }
        // Set dataLoadDate and trainingDate based on steps
        for (workflow in resultDataList) {
            val step1 = workflow.steps?.find { it.step == 1 }
            val step4 = workflow.steps?.find { it.step == 4 }
            workflow.dataLoadDate = step1?.createdDate
            workflow.trainingDate = step4?.createdDate
        }
        return resultDataList
    }
    /**
     * Checks if the client's current model version is outdated compared to the latest global
     * version available, requiring user authentication.
     *
     * @param idToken The user's authentication token.
     * @param preVersion The client's current model version to check against.
     * @param domain The domain of the model.
     * @return A `Mono` emitting a `ModelVersionTrackResponse` with the check result.
     */
    fun getLatestGlobalModel(idToken: String?, preVersion: Int, domain: String): Mono<ModelVersionTrackResponse> {
        val modelTrackRes = ModelVersionTrackResponse()
        if (idToken != null) {
            val userResponse = authService.getUserResponse(idToken)
            modelTrackRes.message = userResponse.message
            modelTrackRes.success = userResponse.success!!
            modelTrackRes.statusCode = userResponse.statusCode!!
            if (!userResponse.success!!) {
                return Mono.just(modelTrackRes)
            }
            val clientGlobalModelVersion = modelService.findGlobalModelLogsByDomain(domain)
            val globalModelVersion = clientGlobalModelVersion.globalWeightsVersion
            modelTrackRes.isModelUsed = globalModelVersion <= preVersion
            modelTrackRes.version = globalModelVersion
        } else {
            modelTrackRes.message = "Invalid Token"
            modelTrackRes.success = false
            modelTrackRes.statusCode = 403
        }
        return Mono.just(modelTrackRes)
    }
    /**
     * Utility method to parse the comma-separated `domains` string from application properties
     * into a list of strings.
     *
     * @return A list of domain names.
     */
    fun getDomainList(): List<String> {
        return domains.split(",").map { it.trim() }
    }
    /**
     * Gathers data for and constructs the summary charts (contributions, performance) for the dashboard.
     *
     * @param domain The domain for which to generate the chart data.
     * @return A `Mono` emitting a `SummaryChartRes` containing the chart data.
     */
    fun getLocalNodeSummaryChart(domain: String): Mono<SummaryChartRes> {
        val summaryChartRes = SummaryChartRes()
        val globalModelWeights = modelService.findGlobalModelWeightsByModelId(domain)
        val contributes = getLocalModelContributionAnalysis(globalModelWeights)
        val modelTracks = generateDummyData()
        val performance = getLocalPerformanceAnalysis(modelTracks)
        summaryChartRes.contributions = contributes
        summaryChartRes.performance = performance
        return Mono.just(summaryChartRes)
    }
    /**
     * Builds the data structure for the model contribution pie chart.
     * (Currently marked as Future Development).
     *
     * @param modelTracks A list of global model weight entities.
     * @return A `ContributionsRes` object formatted for the chart.
     */
    fun getLocalModelContributionAnalysis(modelTracks: List<GlobalModelWeightsEntity>): ContributionsRes {
        val contributionsRes = ContributionsRes()
        contributionsRes.name = "${currentNode.uppercase()}  Model Contribution (Future Development)"
        // Initialize counters for self and others
        var selfCount = 0
        var otherCount = 0
        // Count the occurrences of self ('T') and others ('F')
        for (track in modelTracks) {
            if (track.isSelf == "T") {
                selfCount++
            } else if (track.isSelf == "F") {
                otherCount++
            }
        }
        // Calculate the total count
        val totalCount = selfCount + otherCount

        // Calculate the percentages
        val selfPercentage = if (totalCount > 0) (selfCount.toDouble() / totalCount) * 100 else 0.0
        val otherPercentage = if (totalCount > 0) (otherCount.toDouble() / totalCount) * 100 else 0.0

        // Create PieElementData for self and others
        val selfElement = PieElementData().apply {
            name = currentNode.uppercase()
            value = selfPercentage
            dbValue = selfCount.toString()
        }

        val otherElement = PieElementData().apply {
            name = "OTHER"
            value = otherPercentage
            dbValue = otherCount.toString()
        }
        contributionsRes.contribution = mutableListOf(selfElement, otherElement)
        return contributionsRes
    }
    /**
     * Stores generated global performance data for demo purposes.
     */
    val globalPerfData = generateGlobalPerformanceData(generateDummyData())
    /**
     * Generates semi-random global performance data based on local data for demonstration.
     *
     * @param localData The local performance data to base the global data on.
     * @return A list of generated global performance values.
     */
    fun generateGlobalPerformanceData(localData: List<MlModelTrack>): List<Double> {
        val random = java.util.Random()

        return localData.map { localTrack ->
            val localValue = localTrack.performance.toDouble()
            if (random.nextDouble() < 0.9) {
                // 90% of the time, global is higher
                localValue + (0.01 + random.nextDouble() * 0.05) // Small positive offset
            } else {
                // 10% of the time, global can be slightly lower
                localValue - (0.01 + random.nextDouble() * 0.02) // Small negative offset
            }
        }
    }
    /**
     * Generates dummy model performance data for chart demonstrations.
     *
     * @return A list of `MlModelTrack` objects with dummy data.
     */
    @Suppress("S1192")
    fun generateDummyData(): List<MlModelTrack> {
        val sources = listOf("101", "102", "103")
        val performanceValues = listOf(
            BigDecimal("0.76000"),
            BigDecimal("0.76100"),
            BigDecimal("0.76200"),
            BigDecimal("0.76300"),
            BigDecimal("0.76400"),
            BigDecimal("0.76500"),
            BigDecimal("0.78000"),
            BigDecimal("0.79400"),
            BigDecimal("0.73400"),
            BigDecimal("0.71400"),
            BigDecimal("0.76900"),
            BigDecimal("0.76300"),
            BigDecimal("0.76500"),
            BigDecimal("0.78400"),
            BigDecimal("0.77000")

        )

        val dummyData = mutableListOf<MlModelTrack>()

        for (i in 0 until 15) {
            val source = sources[i % sources.size] // Cycles through sources list
            val createdDate =
                Date.from(LocalDate.now().minusDays(i.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant())
            val performance = performanceValues[i % performanceValues.size] // Cycles through performanceValues list

            val track = MlModelTrack(
                source = source,
                createdDate = createdDate,
                performance = performance
            )
            dummyData.add(track)
        }

        return dummyData
    }
    /**
     * Builds the data structure for the model performance timeline chart, comparing
     * local and global model performance over time.
     *
     * @param modelTracks A list of model performance tracking data.
     * @return A `MultipleTimeLine` object formatted for the chart.
     */
    fun getLocalPerformanceAnalysis(modelTracks: List<MlModelTrack>): MultipleTimeLine {
        val timeLines = MultipleTimeLine()
        val localPerformance = Serie()
        localPerformance.id = "Local Model Performance"
        localPerformance.label = "Local Model Performance"
        val globalPerformance = Serie()
        globalPerformance.id = "Global Model Performance"
        globalPerformance.label = "Global Model Performance"

        val selfModelTracks = mutableListOf<MlModelTrack>()
        for (modelTrack in modelTracks) {
            if (modelTrack.source == currentNodeNumber) {
                selfModelTracks.add(modelTrack)
            }
        }

        timeLines.name = "Model Performance"

        val performanceMap: Map<Date, List<MlModelTrack>> = selfModelTracks
            .groupBy { truncateTime(it.createdDate) }
        val averagePerformanceMap: Map<Date, Double> = performanceMap.mapValues { (_, tracks) ->
            val total = tracks.sumOf { it.performance.toDouble() }
            if (tracks.isNotEmpty()) total / tracks.size else 0.0
        }
        // Calculate total contribution and count for each unique createdDate
        // Populate contributionMap with selfModelTracks
        val sortedAveragePerformanceMap = averagePerformanceMap.toList().sortedBy { it.first }
        val lastFifteenEntries = if (sortedAveragePerformanceMap.size >= 15) {
            sortedAveragePerformanceMap.takeLast(5)
        } else {
            sortedAveragePerformanceMap
        }
        var index = 0 // Initialize an index counter
        // Calculate average contribution for each unique createdDate
        for ((date, perf) in lastFifteenEntries) {
            val localPerfValue = String.format("%.2f", perf).toDouble()
            localPerformance.data.add(localPerfValue.toString())
            val globalPerfValue = String.format("%.2f", globalPerfData[index]).toDouble()
            globalPerformance.data.add(globalPerfValue.toString())
            timeLines.xAxis.add(AppUtils.dateToMMDD(date))
            index++
        }
        timeLines.addSerie(localPerformance)
        timeLines.addSerie(globalPerformance)
        return timeLines
    }
    /**
     * High-level administrative function that completely resets the client's state. It truncates
     * databases, re-enables auto weight updates, and re-initializes the global model weight.
     *
     * @return A `WebResponse` indicating the operation completed.
     */
    @Transactional
    fun reset(): WebResponse {
        val webResponse = WebResponse()
        resetFedlearnClientDB()
        resetFedlearnOrchestratorAgentDB()
        enableAutoWeight()
        initialGlobalModelWeight()
        return webResponse
    }
    /**
     * Sets the run mode to "AUTO" for all domains, allowing for automatic weight updates.
     */
    @Transactional(readOnly = false)
    fun enableAutoWeight() {
        val domains = modelService.getDomainEntities()
        for (domain in domains) {
            val clientRunModeEntity = workflowModelLogsService.getWeightRunModel(domain.name!!)
            clientRunModeEntity.mode = RunModeOptionsEnum.AUTO.mode
            workflowModelLogsService.save(clientRunModeEntity)
        }
    }
    /**
     * Prepares for a database reset by fetching and caching the initial (version 0)
     * model weights from the history table.
     *
     * @return A map of model names to their initial `ModelClientRecordHistoryEntity`.
     */
    fun prepare(): Map<String, ModelClientRecordHistoryEntity> {
        val modelVersionWeights = modelService.findAllModelClientRecordHistoryByVersion(0)
        val modelVersionWeightsMap: Map<String, ModelClientRecordHistoryEntity> =
            modelVersionWeights.associateBy { it.name }
        return modelVersionWeightsMap
    }
    /**
     * Applies the cached initial model weights back to the database. This is typically
     * called after truncating tables during a reset.
     *
     * @param modelVersionWeightsMap A map of model names to their initial weight entities.
     */
    @Transactional
    fun applyInitLocalModelWeight(modelVersionWeightsMap: Map<String, ModelClientRecordHistoryEntity>) {
        modelVersionWeightsMap.forEach { (name, modelClientRecordHistory) ->
            val existingHistoryRecord = modelService.findAllModelClientRecordHistoryByNameVersion(name, 0)
            if (!existingHistoryRecord.isPresent) {
                val modelClientRecordHistoryToInsert = ModelClientRecordHistoryEntity(
                    workflowTraceId = "0000000000000000000000000000", // Assuming constant for init trace id
                    name = modelClientRecordHistory.name,
                    modelWeights = modelClientRecordHistory.modelWeights,
                    version = 0,
                    createdDate = Date(), // Use current date or other logic for created date
                    lastUpdateDate = Date() // Use current date or other logic for last update
                )
                modelService.saveModelClientRecordHistory(modelClientRecordHistoryToInsert)
            }
            // If a record exists in ModelClientRecordEntity, check the localModelWeights field
            val existingdModelClientRecord = modelService.findModelClientRecordByName(name)
            if (existingdModelClientRecord.isPresent) {
                val modelClientRecord = existingdModelClientRecord.get()
                // Check if localModelWeights is null or empty (Blob.length() <= 0)
                if (modelClientRecord.localModelWeights == null || modelClientRecord.localModelWeights?.length() ?: 0 <= 0) {
                    modelClientRecord.localModelWeights = modelClientRecordHistory.modelWeights
                    modelClientRecord.localWeightsVersion = 0
                    modelClientRecord.lastUpdateDate = Date() // Update last modified date
                    modelService.saveModelClientRecord(modelClientRecord)
                }
            }
        }
    }
    /**
     * Resets the `fedlearn_client` database by truncating all relevant tables and then
     * re-inserting the initial (version 0) model weights and records.
     *
     * @return A list of the truncated table names.
     */
    @Transactional(readOnly = false)
    fun resetFedlearnClientDB(): List<String> {
        logger.info("truncate fedlearn_client Tables...")
        val start = System.currentTimeMillis()
        val initWeights = modelService.findInitialModelClientRecordHistory()
        val modelClientRecordCache = mutableMapOf<String, ModelClientRecordEntity>()
        // Check if the list is not empty before proceeding
        if (initWeights.isNotEmpty()) {
            // Pre-fetch related ModelClientRecordEntity for each record and cache them
            initWeights.forEach { record ->
                if (!modelClientRecordCache.containsKey(record.name)) {
                    val modelClientRecordOpt = modelService.findModelClientRecordByName(record.name)
                    if (modelClientRecordOpt.isPresent) {
                        modelClientRecordCache[record.name] = modelClientRecordOpt.get()
                    } else {
                        logger.warn("No matching ModelClientRecordEntity found for name: ${record.name}")
                    }
                }
            }
        }
        val tables = listOf(
            "fedlearn_client.model_feedback",
            "fedlearn_client.model_predict_data",
            "fedlearn_client.model_client_record_history",
            "fedlearn_client.model_client_records",
            "fedlearn_client.workflow_model_logs",
            "fedlearn_client.metrics",
            "fedlearn_client.model_training_result",
            "fedlearn_client.data_seed",
            "fedlearn_client.domain_payment_fraud_data",
            "fedlearn_client.domain_credit_card_fraud_data",
            "fedlearn_client.domain_payment_data",

            )
        // Disable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
        for (table in tables) {
            truncateTable(table)
        }
        // Enable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
        // Check if the list is not empty before proceeding
        if (initWeights.isNotEmpty()) {
            // Reinsert records with version = 0 after truncation using ModelClientRecordRepository
            initWeights.forEach { record ->
                logger.info("reInitialing model weight for: ${record.name}")
                // Insert into model_client_record_history
                logger.info("save model weight historical record for: ${record.name}")
                modelService.saveModelClientRecordHistory(record)
                val previousInitialModelClientRecord = modelClientRecordCache[record.name]
                if (previousInitialModelClientRecord != null) {
                    // Create and populate a new ModelClientRecordEntity
                    logger.info("reset and save initial model client weight record for: ${record.name}")
                    val localWeight = record.modelWeights
                    AppUtils.printPartialWeight(localWeight, record.name, logger)
                    val now = AppUtils.getCurrent()
                    val modelClientRecord = ModelClientRecordEntity()
                    modelClientRecord.name = previousInitialModelClientRecord.name
                    modelClientRecord.definition = previousInitialModelClientRecord.definition
                    modelClientRecord.modelVersion = previousInitialModelClientRecord.modelVersion
                    modelClientRecord.domain = previousInitialModelClientRecord.domain
                    modelClientRecord.localModelWeights = localWeight
                    modelClientRecord.localWeightsVersion = 0
                    modelClientRecord.globalWeightsVersion = 0
                    modelClientRecord.lastUpdateDate = now
                    modelClientRecord.createdDate = now
                    modelService.saveModelClientRecord(modelClientRecord)
                } else {
                    logger.info("No matching ModelClientRecordEntity found for name: ${record.name}")
                }
            }
        } else {
            logger.info("No records found with version 0 in model_client_record_history.")
        }
        logger.info(
            "--------  truncated all tables ----------------> duration: {} sec",
            TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - start))
        )
        return tables
    }
    /**
     * Resets the `fedlearn_orchestrator_agent` database by truncating all relevant tables.
     *
     * @return A list of the truncated table names.
     */
    @Transactional(readOnly = false)
    fun resetFedlearnOrchestratorAgentDB(): List<String> {
        logger.info("truncate fedlearn_orchestrator_agent Tables...")
        val start = System.currentTimeMillis()
        val tables = listOf(
            "fedlearn_orchestrator_agent.workflow_detail",
            "fedlearn_orchestrator_agent.workflow",
            "fedlearn_orchestrator_agent.agent_model_logs",
            "fedlearn_orchestrator_agent.global_model_weights",
            "fedlearn_orchestrator_agent.global_model_training_result",
            "fedlearn_orchestrator_agent.global_metrics",
        )
        // Disable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
        for (table in tables) {
            truncateTable(table)
        }
        // Enable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
        logger.info(
            "--------  truncated all tables ----------------> duration: {} sec",
            TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - start))
        )
        return tables
    }
    /**
     * Transactional helper method to truncate a single database table.
     *
     * @param table The fully qualified name of the table to truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun truncateTable(table: String) {
        logger.info("--------  truncating table  {} ----------------", table)
        agentQueryRepository.truncateTable(table)
    }
    /**
     * Finds and returns workflow entities corresponding to a list of trace IDs.
     *
     * @param workflowTraceIds The list of trace IDs to find.
     * @return A list of matching `WorkflowEntity` objects.
     */
    fun findWorkflowsByTraceIds(workflowTraceIds: List<String>): List<WorkflowEntity> {
        return workflowService.findWorkflowsByTraceIds(workflowTraceIds)
    }
    /**
     * Saves or updates the user-provided label and metadata for a specific data batch.
     *
     * @param dataSeedLabelReq The request containing the batch ID and new label/metadata.
     * @return A `WebResponse` indicating the outcome of the save operation.
     */
    @Transactional(readOnly = false)
    fun saveDataSeedLabel(dataSeedLabelReq: DataSeedLabelRequest): WebResponse {
        val res = WebResponse()
        val batchId = dataSeedLabelReq.batchId!!
        logger.info("--------  process  DataSeedMetaData, batchId: $batchId ----------------")
        val existingMetaDataEntity = dataSeedsService.findDataSeedMetaDataByBatchId(batchId)
        val now = AppUtils.getCurrent()
        if (existingMetaDataEntity.isPresent) {
            val dataSeedMeta = existingMetaDataEntity.get()
            dataSeedMeta.label = dataSeedLabelReq.label
            dataSeedMeta.fileName = dataSeedLabelReq.fileName
            dataSeedMeta.anomalyDesc = dataSeedLabelReq.anomalyDesc
            dataSeedMeta.lastUpdateDate = now
            dataSeedsService.saveDataSeedMetaData(dataSeedMeta)
            res.message = "update existing dataMetaData for batchId: $batchId, label: ${dataSeedMeta.label}"
        } else {
            val newdataSeedMetaData = DataSeedMetaDataEntity().apply {
                this.fileName = dataSeedLabelReq.fileName
                this.domainType = dataSeedLabelReq.domainType
                this.label = dataSeedLabelReq.label
                this.batchId = dataSeedLabelReq.batchId
                this.anomalyDesc = dataSeedLabelReq.anomalyDesc
                this.isMockData = "0"
                this.createdDate = now
                this.lastUpdateDate = now
            }
            dataSeedsService.saveDataSeedMetaData(newdataSeedMetaData)
            res.message = "created dataMetaData for batchId: $batchId, label: ${dataSeedLabelReq.label}"
        }
        return res
    }
}