package com.aikya.orchestrator.service

import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import com.aikya.orchestrator.shared.model.workflow.WorkflowEntity
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_5
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_6
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_7
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_8
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.PENDING
import com.aikya.orchestrator.utils.AppConstants.TRACE_ID
import com.aikya.orchestrator.utils.AppUtils
import com.aikya.orchestrator.utils.AppUtils.displayStep
import com.aikya.orchestrator.utils.FlowEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
/**
 * Service dedicated to managing and advancing the state of **client-side** federated learning workflows.
 *
 * This class provides specialized logic for progressing through the client's workflow lifecycle,
 * handling state transitions for key events like local model evaluation and receiving the
 * global aggregated model. It acts as a collaborator with the more generic `WorkflowService`.
 *
 * @param workflowService The generic service for core workflow operations.
 * @param modelService The service for accessing model-related data.
 */
@Service
class ClientWorkflowService constructor(private val workflowService: WorkflowService,
    private val modelService: ModelService){
    private val logger: Logger = LoggerFactory.getLogger(ModelService::class.java)
    /**
     * Advances the client workflow after the global model aggregation and sharing step.
     *
     * This method is called when the client is ready to receive the new global model. It transitions the workflow
     * from step 5 or 6 to step 7 (`Receive Global Model`), marking intermediate steps as complete
     * and creating placeholder steps for the remainder of the flow. This operation runs in its own
     * new transaction to ensure atomicity.
     *
     * @param workflowTraceId The unique identifier for the workflow to be updated.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun completeClientGlobalModelAggregate(workflowTraceId: String) {
        logger.info("$TRACE_ID: {} - start Complete ClientLocal Model Evaluation", workflowTraceId)
        val workflowEntity = workflowService.findWorkflowByTraceId(workflowTraceId)
        if(workflowEntity.isPresent) {
            val now = AppUtils.getCurrent()
            val workflow = workflowEntity.get()
            workflow.lastUpdateDate = now
            val step = workflow.currentStep
            workflow.status = PENDING
            logger.info("$TRACE_ID: {} - Current Step is {}", workflowTraceId, step)
            if(step == Flow_Client_5.step || step == Flow_Client_6.step) {
                workflow.currentStep = Flow_Client_7.step
                workflowService.save(workflow)
                val workflowDetail = workflowService.findWorkflowDetailByStep(workflow.id!!, step)
                if (workflowDetail != null) {
                    logger.info("$TRACE_ID: {} - Move step {} to complete", workflowTraceId, step)
                    workflowDetail.lastUpdateDate = now
                    workflowDetail.status = COMPLETE
                    workflowService.save(workflowDetail)
                }
                if (step == Flow_Client_5.step) {
                    logger.info("$TRACE_ID: {} - Move step {} to complete", workflowTraceId, Flow_Client_6)
                    workflowService.createWorkflowDetail(workflow, Flow_Client_6, COMPLETE)
                    workflowService.createWorkflowDetail(workflow, Flow_Client_7, PENDING)
                    workflowService.createWorkflowDetail(workflow, Flow_Client_8, INITIAL)
                } else if (step == Flow_Client_6.step) {
                    workflowService.createWorkflowDetail(workflow, Flow_Client_7, PENDING)
                    workflowService.createWorkflowDetail(workflow, Flow_Client_8, INITIAL)
                }
            }

        }
    }
    /**
     * Marks the "Local Model Evaluation" step (step 5) as complete and advances the workflow
     * to the next logical step (step 6).
     *
     * @param workflowTraceId The identifier for the workflow being updated.
     */
    @Transactional
    fun completeClientLocalModelEvaluation(workflowTraceId: String) {
        logger.info("$TRACE_ID: {} - start Complete Client Local Model Evaluation", workflowTraceId)
        val workflowEntity = workflowService.findWorkflowByTraceId(workflowTraceId)
        if(workflowEntity.isPresent) {
            val workflow = workflowEntity.get()
            val workflowTraceId = workflow.workflowTraceId!!
            workflow.status = PENDING
            workflow.currentStep = Flow_Client_6.step
            val now = AppUtils.getCurrent()
            workflow.lastUpdateDate = now
            val workflowDetail = workflowService.findWorkflowDetailByStep(workflow.id!!, Flow_Client_5.step)!!
            workflowDetail.lastUpdateDate = now
            workflowDetail.status = COMPLETE
            workflowService.save(workflow)
            workflowService.save(workflowDetail)
            workflowService.moveWorkflowToNextStep(workflow, Flow_Client_6, false)
            displayStep(logger, workflowTraceId, Flow_Client_6)
            logger.info(
                "$TRACE_ID: {} - end Complete Client Local Model Evaluation, workflow status: {}, current step: {}",
                workflowTraceId,
                workflow.status,
                workflow.currentStep
            )
        }
    }
    /**
     * Generic method to complete a given workflow step and advance the workflow to the next step.
     * It sets the current step's status to `COMPLETE` and the next step's status to `PENDING`.
     * If the completed step is the last one, the entire workflow is marked as `COMPLETE`.
     *
     * @param workflow The `WorkflowEntity` object to update.
     * @param currentFlowEvent The `FlowEvent` representing the step that has just been completed.
     */
    @Transactional
    fun completeClientCurrentWorkflowStep(workflow: WorkflowEntity, currentFlowEvent: FlowEvent) {
        val now = AppUtils.getCurrent()
        workflow.lastUpdateDate = now
        val workflowDetail = workflowService.findWorkflowDetailByStep(workflow.id!!, currentFlowEvent.step)!!
        workflowDetail.lastUpdateDate = now
        workflowDetail.status = COMPLETE
        workflowService.save(workflowDetail)
        if(currentFlowEvent.isLastStep) {
            workflow.status = COMPLETE
        } else {
            workflow.status = PENDING
            val nextFlowEvent = workflowService.getNextStep(true, currentFlowEvent)
            workflow.currentStep = nextFlowEvent.step
            val nextWorkflowDetail = workflowService.findWorkflowDetailByStep(workflow.id!!, nextFlowEvent.step)!!
            nextWorkflowDetail.status = PENDING
            nextWorkflowDetail.lastUpdateDate = now
            workflowService.save(nextWorkflowDetail)
        }
        workflowService.save(workflow)
    }
    /**
     * Overloaded convenience method to complete a workflow step. It finds the workflow by its
     * trace ID and then calls the primary `completeClientCurrentWorkflowStep` method.
     *
     * @param workflowTraceId The identifier of the workflow.
     * @param currentFlowEvent The event representing the completed step.
     */
    fun completeClientCurrentWorkflowStep(workflowTraceId: String, currentFlowEvent: FlowEvent) {
        val workflowEntity = workflowService.findWorkflowByTraceId(workflowTraceId)
        if (workflowEntity.isPresent) {
            val workflow = workflowEntity.get()
            completeClientCurrentWorkflowStep(workflow, currentFlowEvent)
        }
    }
    /**
     * Finds all workflows that are currently at the "Share Local Model" step (step 5) and
     * are in the `INITIAL` state, making them ready for processing.
     *
     * @return A list of `WorkflowDetailEntity` objects representing the pending tasks.
     */
    fun findInitialShareLocalWorkflows(): List<WorkflowDetailEntity> {
        return workflowService.findWorkflowsByStepAndStatus(Flow_Client_5.step, INITIAL)
    }
    /**
     * Finds and returns workflow entities corresponding to a list of trace IDs.
     * This is a pass-through method to the `WorkflowService`.
     *
     * @param workflowTraceIds The list of trace IDs to find.
     * @return A list of matching `WorkflowEntity` objects.
     */
    fun findWorkflowsByTraceIds(workflowTraceIds: List<String>): List<WorkflowEntity> {
        return workflowService.findWorkflowsByTraceIds(workflowTraceIds)
    }
}