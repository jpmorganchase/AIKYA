package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.utils.AppConstants.Flow_Client_5
import com.aikya.orchestrator.utils.AppConstants.PENDING
import com.aikya.orchestrator.utils.AppUtils.displayStep
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * ShareLocalModelTask is a scheduled component responsible for identifying completed
 * local training workflows and initiating the model-sharing process with the orchestration server.
 *
 * This task scans for workflows marked as ready for sharing (Flow_Client_5),
 * updates their status to PENDING, and sends the local model to the remote orchestrator.
 *
 * This task is typically executed at regular intervals, as configured by the scheduling annotations.
 */
@Component
class ShareLocalModelTask  : BaseClientWorkflowTask() {
    /**
     * Scheduled method that runs periodically to share completed local models
     * with the remote orchestration server.
     *
     * Steps:
     * 1. Finds client workflows ready for sharing.
     * 2. Updates workflow state to PENDING for Flow_Client_5.
     * 3. Invokes orchestration service to push the model.
     *
     * Exceptions are caught and logged to avoid task interruption.
     */
    //@Scheduled(initialDelay=0, fixedRateString = "\${app.schedule.training.interval}")
    @Scheduled(initialDelay=0, fixedRate=1000)
    fun runShareLocalModelTask() {
        try {
            val initialShareLocalModelWorkflows = clientWorkflowService.findInitialShareLocalWorkflows()
            if(initialShareLocalModelWorkflows.isNotEmpty()) {
                for(workflowModel in initialShareLocalModelWorkflows) {
                    val workflowTraceId = workflowModel.workflowTraceId!!
                    logger.info("Found completed workflow local model task for $workflowTraceId")
                    displayStep(logger, workflowTraceId, Flow_Client_5)
                    logger.info("update completed workflow local model task for $workflowTraceId")
                    workflowService.updateClientWorkflow(workflowTraceId, PENDING, Flow_Client_5)
                    orchestrationClientCallService.shareLocalModelTaskToRemoteOrchestrationServer(workflowTraceId)

                }
            }
        }  catch (e: Exception) {
            logger.error("run share local model  task with error: {}", e.message)
        }
    }
}