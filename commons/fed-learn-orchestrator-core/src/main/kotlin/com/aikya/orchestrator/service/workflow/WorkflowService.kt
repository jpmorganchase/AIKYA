package com.aikya.orchestrator.service.workflow

import com.aikya.orchestrator.service.common.BaseService
import com.aikya.orchestrator.service.common.QueryLoaderService
import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import com.aikya.orchestrator.shared.model.workflow.WorkflowEntity
import com.aikya.orchestrator.shared.model.workflow.WorkflowTypeEntity
import com.aikya.orchestrator.shared.repository.workflow.WorkflowDetailRepository
import com.aikya.orchestrator.shared.repository.workflow.WorkflowRepository
import com.aikya.orchestrator.shared.repository.workflow.WorkflowTypeRepository
import com.aikya.orchestrator.utils.AppConstants
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.FAIL
import com.aikya.orchestrator.utils.AppConstants.Flow_00_000
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_7
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_8
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.PENDING
import com.aikya.orchestrator.utils.AppConstants.TRACE_ID
import com.aikya.orchestrator.utils.AppConstants.allClientEventsFlow
import com.aikya.orchestrator.utils.AppConstants.allServerEventsFlow
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.FlowEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Core service for managing the lifecycle of business workflows. 🚀
 *
 * This service handles the creation, state transition, and retrieval of [WorkflowEntity]
 * and [WorkflowDetailEntity] objects. It orchestrates the entire process flow,
 * distinguishing between client and server-side steps, and ensures data consistency
 * through transactional operations.
 *
 * @param queryLoaderService Service for loading pre-defined SQL queries (from BaseService).
 * @param workflowRepository Repository for [WorkflowEntity] data access.
 * @param workflowDetailRepository Repository for [WorkflowDetailEntity] data access.
 * @param workflowTypeRepository Repository for [WorkflowTypeEntity] data access.
 */
@Service
@Transactional
class WorkflowService @Autowired constructor(
    queryLoaderService: QueryLoaderService,
    private val workflowRepository: WorkflowRepository,
    private val workflowDetailRepository: WorkflowDetailRepository,
    private val workflowTypeRepository: WorkflowTypeRepository
) : BaseService(queryLoaderService) {
    private val logger: Logger = LoggerFactory.getLogger(WorkflowService::class.java)

    @Value("\${app.node.number}")
    val currentNode = ""

    /* =====================================================================================**/
    /**
     * Creates a new workflow and its initial detail step using a provided trace ID.
     *
     * @param traceId The unique trace ID for the entire workflow.
     * @param modId The ID of the associated model.
     * @param wfTypeId The ID of the workflow type definition.
     * @param workflowEvent The initial [FlowEvent] that starts the workflow.
     * @param wfStatus The initial status of the workflow (e.g., PENDING).
     * @param modVersion The version of the associated model.
     * @return The newly created [WorkflowEntity].
     */
    @Transactional
    fun createWorkflowWithTraceId(traceId: String, modId: Long, wfTypeId: Long, workflowEvent: FlowEvent, wfStatus: String, modVersion: Long) : WorkflowEntity {
        logger.info("TraceID: {} --------  createWorkflow for modelId {}, step: {}, source: {}, target: {} ----------------", traceId, modId, workflowEvent.step, workflowEvent.source, workflowEvent.target)
        val now = AppUtils.getCurrent()
        val workflow = WorkflowEntity().apply {
            status= getWorkflowStatus(wfStatus)
            workflowTraceId = traceId
            modelId = modId
            modelVersion = modVersion
            workflowTypeId = wfTypeId
            currentStep = workflowEvent.step
            createdDate = now
            lastUpdateDate = now
        }
        val savedWorkflow = workflowRepository.save(workflow)
        // Access the generated ID of the saved entity
        val wfId = savedWorkflow.id!!

        val workflowDetail = WorkflowDetailEntity().apply {
            workflowId = wfId
            workflowTraceId = traceId
            source = workflowEvent.source
            target = workflowEvent.target
            event = workflowEvent.event
            status= getWorkflowStatus(wfStatus)
            step = workflowEvent.step
            stepDesc = workflowEvent.description
            label = workflowEvent.label
            createdDate = now
            lastUpdateDate = now
        }
        workflowDetailRepository.save(workflowDetail)
        return workflow
    }
    /**
     * Creates a new workflow with an auto-generated trace ID.
     * This is a convenience method that generates a UUID and calls [createWorkflowWithTraceId].
     *
     * @param modelId The ID of the associated model.
     * @param wfTypeId The ID of the workflow type definition.
     * @param workflowEvent The initial [FlowEvent] that starts the workflow.
     * @param wfStatus The initial status of the workflow.
     * @param version The version of the associated model.
     * @return The newly created [WorkflowEntity].
     */
    @Transactional
    fun createWorkflow(modelId: Long, wfTypeId: Long, workflowEvent: FlowEvent, wfStatus: String, version: Long) : WorkflowEntity {
        val workflowUUID = AppUtils.generateWorkflowUUID(currentNode, 6)
        return createWorkflowWithTraceId(workflowUUID, modelId, wfTypeId, workflowEvent, wfStatus, version)
    }
    /**
     * Normalizes a workflow status string.
     *
     * @param status The raw status string.
     * @return The normalized status (e.g., PENDING, COMPLETE, FAIL).
     * @throws UnsupportedOperationException if the status is not recognized.
     */
    fun getWorkflowStatus(status: String): String {
        return when (status) {
            INITIAL, PENDING -> PENDING
            COMPLETE -> COMPLETE
            FAIL -> FAIL
            else -> throw UnsupportedOperationException("Unsupported status: $status")
        }
    }
    /**
     * Retrieves a workflow type definition by its name.
     *
     * @param name The name of the workflow type.
     * @return The found [WorkflowTypeEntity] or null if it doesn't exist.
     */
    fun getWorkflowType(name: String) : WorkflowTypeEntity {
        return workflowTypeRepository.findWorkflowTypeByName(name).orElse(null)
    }
    /**
     * Finds a workflow by its unique trace ID.
     *
     * @param traceId The workflow trace ID.
     * @return An [Optional] containing the [WorkflowEntity] if found.
     */
    fun findWorkflowByTraceId(traceId: String): Optional<WorkflowEntity> {
        return workflowRepository.findWorkflowByTraceId(traceId)
    }
    /**
     * Saves or updates a [WorkflowEntity].
     */
    @Transactional
    fun save(workflow: WorkflowEntity) {
        workflowRepository.save(workflow)
    }
    /**
     * Saves or updates a [WorkflowDetailEntity].
     */
    @Transactional
    fun save(workflowDetail: WorkflowDetailEntity) {
        workflowDetailRepository.save(workflowDetail)
    }
    /**
     * Updates a **client-side** workflow to a new event and status.
     */
    @Transactional
    fun updateClientWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent) {
        updateWorkflow(workflowTraceId, workflowStatus, currentFlowEvent, true)
    }
    /**
     * Updates a **server-side** workflow to a new event and status.
     */
    @Transactional
    fun updateServerWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent) {
        updateWorkflow(workflowTraceId, workflowStatus, currentFlowEvent, false)
    }
    /**
     * Updates a **client-side** workflow to a new event and status.
     */
    @Transactional
    fun updateClientWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent, nextInitial: Boolean) {
        updateWorkflow(workflowTraceId, workflowStatus, currentFlowEvent, true, nextInitial)
    }
    /**
     * Updates a **server-side** workflow to a new event and status.
     */
    @Transactional
    fun updateServerWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent, nextInitial: Boolean) {
        updateWorkflow(workflowTraceId, workflowStatus, currentFlowEvent, false, nextInitial)
    }

    @Transactional
    fun updateWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent, isClient: Boolean) {
        updateWorkflow(workflowTraceId, workflowStatus, currentFlowEvent, isClient, true)
    }
    /**
     * The primary logic for updating a workflow's state.
     * It handles status changes (COMPLETE, FAIL, PENDING) and automatically
     * transitions the workflow to the next step when a step is completed.
     *
     * @param workflowTraceId The trace ID of the workflow to update.
     * @param workflowStatus The new status for the current step.
     * @param currentFlowEvent The [FlowEvent] that is being updated.
     * @param isClient True if this is a client-side flow, false for server-side.
     * @param nextInitial True if the next step should be marked as **INITIAL**.
     */
    @Transactional
    fun updateWorkflow(workflowTraceId: String, workflowStatus: String, currentFlowEvent: FlowEvent, isClient: Boolean, nextInitial: Boolean) {
        logger.info("$TRACE_ID: {} - update workflow -> event: {}, status: {}", workflowTraceId, currentFlowEvent.event, workflowStatus)
        val workflowEntity = findWorkflowByTraceId(workflowTraceId)
        if(workflowEntity.isPresent) {
            val workflow = workflowEntity.get()
            val workflowDetail = workflowDetailRepository.findWorkflowDetailByStep(workflow.id!!, currentFlowEvent.step)
            val now = AppUtils.getCurrent()
            if (workflowDetail != null) {
                workflow.lastUpdateDate = now
                workflowDetail.lastUpdateDate = now

                when (workflowStatus) {
                    COMPLETE -> {
                        workflowDetail.status = COMPLETE
                        workflow.currentStep = currentFlowEvent.step
                        if (currentFlowEvent.isLastStep) {
                            workflow.status = COMPLETE
                        } else {
                            workflow.status = PENDING
                            val nextFlowEvent = getNextStep(isClient, currentFlowEvent)
                            moveWorkflowToNextStep(workflow, nextFlowEvent, true)
                        }
                    }

                    FAIL -> {
                        workflow.status = FAIL
                        workflow.currentStep = currentFlowEvent.step
                        workflowDetail.status = FAIL
                    }

                    PENDING -> {
                        workflow.status = PENDING
                        workflow.currentStep = currentFlowEvent.step
                        workflowDetail.status = PENDING
                    }
                }

                workflowRepository.save(workflow)
                workflowDetailRepository.save(workflowDetail)
            }
        }

    }
    /**
     * Gets the next flow event based on the current event and context (client or server).
     *
     * @param isClient True if the context is client-side.
     * @param currentFlowEvent The current [FlowEvent].
     * @return The next [FlowEvent] in the sequence.
     */
    fun getNextStep(isClient: Boolean, currentFlowEvent: FlowEvent): FlowEvent {
        if(isClient) {
            return getNextClientStep(currentFlowEvent)
        }
        return getNextServerStep(currentFlowEvent)
    }
    /**
     * Advances a workflow to the next step by creating a new [WorkflowDetailEntity].
     *
     * @param workflow The parent [WorkflowEntity].
     * @param nextFlowEvent The [FlowEvent] for the new step.
     * @param status The status for the new workflow detail step.
     * @return The newly created [WorkflowDetailEntity].
     */
    @Transactional
    fun moveWorkflowToNextStep(workflow: WorkflowEntity, nextFlowEvent: FlowEvent, status: String): WorkflowDetailEntity {
        logger.info("$TRACE_ID: {} - workflow move to next step -> event: {}, status: {}", workflow.workflowTraceId, nextFlowEvent.event,status)
        return createWorkflowDetail(workflow, nextFlowEvent, status)
    }
    /**
     * Creates and persists a new [WorkflowDetailEntity] for a given step in a workflow.
     *
     * @param workflow The parent [WorkflowEntity] to which this detail belongs.
     * @param flowEvent The [FlowEvent] defining the new step.
     * @param status The initial status for this new detail step.
     * @return The persisted [WorkflowDetailEntity].
     */
    @Transactional
    fun createWorkflowDetail(workflow: WorkflowEntity, flowEvent: FlowEvent, status: String): WorkflowDetailEntity {
        val now = AppUtils.getCurrent()
        val workflowDetail = WorkflowDetailEntity()
        workflowDetail.workflowId = workflow.id!!
        workflowDetail.workflowTraceId = workflow.workflowTraceId
        workflowDetail.source = flowEvent.source
        workflowDetail.target = flowEvent.target
        workflowDetail.event=flowEvent.event
        workflowDetail.status= status
        workflowDetail.step= flowEvent.step
        workflowDetail.stepDesc=flowEvent.description
        workflowDetail.label = flowEvent.label
        workflowDetail.createdDate = now
        workflowDetail.lastUpdateDate = now
        workflowDetailRepository.save(workflowDetail)
        logger.info("$TRACE_ID: {} - created workflow detail -> event: {}, status: {}", workflow.workflowTraceId, flowEvent.event, workflowDetail.status)
        return workflowDetail
    }
    /**
     * Advances a workflow to the next step by creating a new [WorkflowDetailEntity].
     *
     * @param workflow The parent [WorkflowEntity].
     * @param nextFlowEvent The [FlowEvent] for the new step.
     * @param status The status for the new workflow detail step.
     * @return The newly created [WorkflowDetailEntity].
     */
    @Transactional
    fun moveWorkflowToNextStep(workflow: WorkflowEntity, nextFlowEvent: FlowEvent, initial: Boolean) {
        val status = if (initial) INITIAL else PENDING
        moveWorkflowToNextStep(workflow, nextFlowEvent, status)
    }
    /**
     * Checks if the given flow event is the last step in its sequence.
     */
    fun hasNextStep(current: FlowEvent): Boolean {
        return !current.isLastStep
    }
    /**
     * Gets the next step in the client-side workflow sequence.
     */
    fun getNextClientStep(current: FlowEvent): FlowEvent {
        // Find the index of the current flow event
        val currentIndex = allClientEventsFlow.indexOf(current)
        // If the current event is not the last one, return the next event
        return if (currentIndex != -1 && currentIndex < allClientEventsFlow.size - 1) {
            allClientEventsFlow[currentIndex + 1]
        } else {
            Flow_00_000 // Return NONE if there is no next step
        }
    }
    /**
     * Gets the next step in the server-side workflow sequence.
     */
    fun getNextServerStep(current: FlowEvent): FlowEvent {
        // Find the index of the current flow event
        val currentIndex = allServerEventsFlow.indexOf(current)
        // If the current event is not the last one, return the next event
        return if (currentIndex != -1 && currentIndex < allServerEventsFlow.size - 1) {
            allServerEventsFlow[currentIndex + 1]
        } else {
            Flow_00_000 // Return NONE if there is no next step
        }
    }
    /**
     * Advances a workflow by a specified number of steps, identified by its trace ID.
     */
    @Transactional
    fun moveMultipleStepsByTraceId(workflowTraceId: String, currentFlowEvent: FlowEvent, stepsToMove: Int) {
        val workflowEntity = findWorkflowByTraceId(workflowTraceId)
        if (workflowEntity.isPresent) {
            val workflow = workflowEntity.get()
            moveMultipleSteps(workflow, currentFlowEvent, stepsToMove)
        }
    }
    /**
     * Core logic to advance a workflow by a specified number of steps.
     * It iteratively marks steps as **COMPLETE** until it reaches the target step,
     * which is marked as **INITIAL**.
     */
    @Transactional
    fun moveMultipleSteps(workflow: WorkflowEntity, currentFlowEvent: FlowEvent, stepsToMove: Int) {
        val now = AppUtils.getCurrent()
        var currentEvent = currentFlowEvent
        var currentWorkflowDetail = findWorkflowDetailByStep(workflow.id!!, currentEvent.step)!!
        // Set the current workflow detail as COMPLETE before moving steps
        currentWorkflowDetail.status = COMPLETE
        currentWorkflowDetail.lastUpdateDate = now
        save(currentWorkflowDetail)
        // Loop to move through the steps
        for (i in 1..stepsToMove) {
            // Get the next step in the workflow
            val nextEvent = getNextClientStep(currentEvent)
            if (i < stepsToMove) {
                moveWorkflowToNextStep(workflow, nextEvent, COMPLETE)
            } else {
                if (nextEvent.isLastStep) {
                    workflow.status = COMPLETE
                } else {
                    workflow.status = PENDING
                }
                moveWorkflowToNextStep(workflow, nextEvent, INITIAL)
            }
            // Update the workflow's current step and save it
            workflow.currentStep = nextEvent.step
            workflow.lastUpdateDate = now
            save(workflow)
            // Move to the next event for the next iteration
            currentEvent = nextEvent
        }
    }
    /**
     * Finds all workflows for a given model that are in a **PENDING** state.
     */
    fun getAllPendingWorkflows(modelId :Long): List<WorkflowEntity> {
        return workflowRepository.findAllPendingWorkflows(modelId)
    }
    /**
     * Finds all workflow details that are at the prediction step and have a status of **INITIAL**.
     */
    fun findInitialPredictWorkflows(): List<WorkflowDetailEntity> {
        return workflowDetailRepository.findByStepAndStatus(AppConstants.Flow_Client_2.step, INITIAL)
    }
    /**
     * Finds workflow details for a given step that are in an **INITIAL** state, typically waiting to be processed.
     */
    fun findReceivedInitAggWorkflows(step: Int): List<WorkflowDetailEntity> {
        return workflowDetailRepository.findByStepAndStatus(step, INITIAL)
    }
    /**
     * Finds workflow details for a given step that are in a **PENDING** state, typically undergoing aggregation.
     */
    fun findPendingAggWorkflows(step: Int): List<WorkflowDetailEntity> {
        return workflowDetailRepository.findByStepAndStatus(step, PENDING)
    }
    /**
     * Finds workflow details for a given step with a specific status.
     */
    fun findWorkflowsByStepAndStatus(step: Int, status: String) : List<WorkflowDetailEntity>{
        return workflowDetailRepository.findByStepAndStatus(step, status)
    }
    /**
     * Finds workflow details at a given step that are **PENDING**, representing models ready to be updated.
     */
    fun findUpdateGlobalAggregatedModel(step: Int): List<WorkflowDetailEntity> {
        return workflowDetailRepository.findByStepAndPendingStatus(step)
    }
    /**
     * Finds workflow details at a given step that are **PENDING**, representing models ready to be shared.
     */
    fun findShareGlobalAggregatedModel(step: Int): List<WorkflowDetailEntity> {
        return workflowDetailRepository.findByStepAndPendingStatus(step)
    }
    /**
     * Finds all workflows waiting for a remote global aggregation for a specific model.
     */
    fun findAwaitingRemoteGlobalAggregateWorkflows(modelId: Long): List<WorkflowEntity> {
        return workflowRepository.findAwaitingRemoteGlobalAggregateWorkflows(modelId)
    }
    /**
     * Finds workflows for a given model that are in a paused state awaiting action.
     */
    fun findAwaitingPausedWorkflows(modelId: Long): List<WorkflowEntity> {
        return workflowRepository.findAwaitingPausedWorkflows(modelId)
    }
    /**
     * Retrieves a list of workflows based on a collection of trace IDs.
     */
    fun findWorkflowsByTraceIds(workflowTraceIds: List<String>): List<WorkflowEntity> {
        return workflowRepository.findByWorkflowTraceIdIn(workflowTraceIds)
    }
    /**
     * Finds all client workflows waiting to receive the globally aggregated model (`Flow_Client_7`).
     */
    fun findAllAwaitingReceiveGlobalAggregatedWorkflows(): List<WorkflowEntity> {
        return workflowRepository.findAllAwaitingCurrentPendingWorkflows(Flow_Client_7.step)
    }
    /**
     * Finds all client workflows that have received the model and are waiting for final completion (`Flow_Client_8`).
     */
    fun findAllAwaitingCompletedWorkflows(): List<WorkflowEntity> {
        return workflowRepository.findAllAwaitingCurrentPendingWorkflows(Flow_Client_8.step)
    }
    /**
     * Retrieves the most recently created workflow for a given model ID.
     */
    fun findLastestWorkflowByModelId(modelId: Long): Optional<WorkflowEntity> {
        return workflowRepository.findTopByModelIdOrderByCreatedDateDesc(modelId)
    }
    /**
     * Finds all **PENDING** workflows from a given list of trace IDs.
     */
    fun getPendingWorkflowsByTraceIdsAndStatus(workflowTraceIds: List<String>): List<WorkflowEntity> {
        return workflowRepository.findByWorkflowTraceIdsAndStatus(workflowTraceIds)
    }
    /**
     * Finds a specific workflow step detail for a given workflow ID.
     *
     * @param workflowId The ID of the parent workflow.
     * @param step The step number to find.
     * @return The [WorkflowDetailEntity] if found, otherwise null.
     */
    fun findWorkflowDetailByStep(workflowId: Long, step: Int): WorkflowDetailEntity? {
        return  workflowDetailRepository.findWorkflowDetailByStep(workflowId, step)
    }
    /**
     * Finds all detail steps for a given workflow that are in a **PENDING** or **INITIAL** state.
     */
    fun findPendingWorkflowDetail(workflowId: Long): List<WorkflowDetailEntity> {
        return  workflowDetailRepository.findPendingOrInitialByWorkflowId(workflowId)
    }
    /**
     * Finds a server-side [FlowEvent] definition by its step number.
     */
    fun findServerFlowEventByStep(step: Int): FlowEvent? {
        return allServerEventsFlow.find { it.step == step }
    }
    /**
     * Finds a client-side [FlowEvent] definition by its step number.
     */
    fun findClientFlowEventByStep(step: Int): FlowEvent? {
        return allClientEventsFlow.find { it.step == step }
    }
}