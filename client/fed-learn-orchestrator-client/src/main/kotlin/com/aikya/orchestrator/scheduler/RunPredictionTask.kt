package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.service.DataSeedsService
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.FAIL
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_2
import com.aikya.orchestrator.utils.AppConstants.PENDING
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * RunPredictionTask is a scheduled task that periodically checks for workflows
 * ready for initial prediction, validates their associated data seed status,
 * and triggers model prediction requests to the orchestration server.
 *
 * This task ensures that prediction is only initiated after the corresponding
 * data seed is successfully prepared (status = COMPLETE).
 *
 * It also marks workflows as FAILED if the data seed preparation fails.
 */
@Component
class RunPredictionTask(val dataSeedsService: DataSeedsService) : BaseClientWorkflowTask() {
    /**
     * Scheduled method that runs every 5 seconds to initiate model predictions
     * for workflows in the "initial prediction" stage (Flow_Client_2).
     *
     * Steps:
     * 1. Retrieves all workflows marked for initial prediction.
     * 2. Retrieves corresponding data seeds based on workflowTraceIds.
     * 3. For each workflow:
     *    - If its data seed is COMPLETE, update status to PENDING and trigger prediction.
     *    - If its data seed is FAIL, mark the workflow as FAILED.
     *
     * Errors are caught and logged without interrupting the scheduler loop.
     */
    @Scheduled(initialDelay=0, fixedRate=5000)
    fun runInitialPredictionTask() {
        try {
            val initialWorkflowModels = workflowService.findInitialPredictWorkflows()
            if (initialWorkflowModels.isNotEmpty()) {
                val workflowTraceIds = initialWorkflowModels.mapNotNull { it.workflowTraceId }
                val dataSeeds = dataSeedsService.findDataSeedsByWorkflowTraceIdIn(workflowTraceIds)
                for (workflowModel in initialWorkflowModels) {
                    val workflowTraceId = workflowModel.workflowTraceId
                    val dataSeed = dataSeeds.find { it.workflowTraceId == workflowTraceId }
                    if (dataSeed?.status == COMPLETE) {
                        workflowService.updateClientWorkflow(workflowTraceId!!, PENDING, Flow_Client_2)
                        logger.info("Start workflow model prediction for $workflowTraceId")
                        orchestrationClientCallService.callPredict(workflowTraceId, true)
                    } else if(dataSeed?.status == FAIL) {
                        workflowService.updateClientWorkflow(workflowTraceId!!, FAIL, Flow_Client_2)
                    }
                }
            }
        }  catch (e: Exception) {
            logger.error("run Initial Prediction workflow task with error: {}", e.message)
        }
    }
}