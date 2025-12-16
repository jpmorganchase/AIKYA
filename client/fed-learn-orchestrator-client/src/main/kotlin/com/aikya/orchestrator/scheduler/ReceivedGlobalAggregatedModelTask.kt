package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.service.OrchestrationClientCallService
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_7
import com.aikya.orchestrator.utils.AppUtils.displayStep
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
/**
 * ReceivedGlobalAggregatedModelTask is a scheduled task responsible for
 * detecting when a global aggregated model has been received by the client
 * and transitioning the associated client workflows to the next step.
 *
 * This task helps coordinate the progression from the model aggregation
 * phase to retraining or other downstream steps in the federated learning lifecycle.
 */
@Component
class ReceivedGlobalAggregatedModelTask(private val clientCallService: OrchestrationClientCallService) : BaseClientWorkflowTask() {

    /**
     * Scheduled method that runs every 35 seconds to process client workflows
     * that are awaiting receipt of the global aggregated model.
     *
     * Steps:
     * 1. Retrieve workflows in the `AWAITING_RECEIVE_GLOBAL_MODEL` state.
     * 2. For each workflow:
     *    - Invoke the training logic via the orchestration client call service.
     *    - Mark the current client workflow step (Flow_Client_7) as complete.
     *    - Log the step transition for traceability.
     *
     * This method is transactional to ensure atomicity of workflow updates and calls.
     *
     * Errors are caught and logged without stopping the scheduled execution.
     */
    //@Scheduled(initialDelay=0, fixedRateString = "\${app.schedule.predict-init.interval}")
    @Scheduled(initialDelay=0, fixedRate=35000)
    @Transactional
    fun runReceivedGlobalAggregatedModelTask() {
        try {
            val initReceivedGlobalModelWorkflows = workflowService.findAllAwaitingReceiveGlobalAggregatedWorkflows()
            if(initReceivedGlobalModelWorkflows.isNotEmpty()) {
                for(globalAggregatedWorkflow in initReceivedGlobalModelWorkflows) {
                    val workflowTraceId =  globalAggregatedWorkflow.workflowTraceId!!
                    clientCallService.callTraining(workflowTraceId)
                    clientWorkflowService.completeClientCurrentWorkflowStep(globalAggregatedWorkflow, Flow_Client_7)
                    displayStep(logger, workflowTraceId, Flow_Client_7)
                }
            }
        }  catch (e: Exception) {
            logger.error("run Received Global Aggregated Model workflow task with error: {}", e.message)
        }
    }

}