package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.service.DataSeedsService
import com.aikya.orchestrator.utils.AppConstants.ANSI_PINK
import com.aikya.orchestrator.utils.AppConstants.ANSI_RESET
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.FINAL
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_2
import com.aikya.orchestrator.utils.AppUtils.display
import com.aikya.orchestrator.utils.AppUtils.displayStep
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * A scheduled task that advances the workflow after a prediction job is completed. 💬
 *
 * This component periodically scans for workflows where the prediction step has finished.
 * Upon finding one, it updates the workflow's state to signify that it is now
 * awaiting user feedback, which is the next step in the process.
 *
 * @param dataSeedsService Service for accessing data batch (seed) metadata.
 */
@Component
class FeedbackTask(private val dataSeedsService: DataSeedsService) : BaseClientWorkflowTask() {
    /**
     * Executes the task to process completed prediction jobs.
     *
     * This method is scheduled to run at a fixed rate. It queries for workflow logs
     * indicating a completed prediction. For each one found, it marks the log as final
     * to prevent reprocessing and updates the main client workflow status to complete the
     * prediction step (`Flow_Client_2`), effectively moving it to the "awaiting feedback" stage.
     */
//    @Scheduled(initialDelay=0, fixedRateString = "\${app.schedule.predict.interval}")
    @Scheduled(initialDelay=0, fixedRate=1000)
    fun runPredictionTask() {
        try {
            val completedPredictsWorkflow = workflowModelLogsService.getCompletedPredictWorkflowModelLogs()
            if(completedPredictsWorkflow.isNotEmpty()) {
                for(workflowModel in completedPredictsWorkflow) {
                    val workflowTraceId = workflowModel.workflowTraceId!!
                    val dataSeed = dataSeedsService.findDataSeedByWorkflowTraceId(workflowTraceId)
                    val batchId = dataSeed.batchId
                    logger.info("TRACE-ID: $workflowTraceId Found completed workflow model prediction for batchID=$batchId")
                    workflowModel.status= FINAL
                    workflowModelLogsService.updateWorkflowModelLogs(workflowModel)
                    workflowService.updateClientWorkflow(workflowTraceId, COMPLETE, Flow_Client_2)
                    display(logger, ".........${ANSI_PINK}TRACE-ID: $workflowTraceId - Completed workflow prediction, await Sent Feedback for batchID=$batchId.........$ANSI_RESET")
                    displayStep(logger, workflowTraceId, Flow_Client_2)
                }
            }
        }  catch (e: Exception) {
            logger.error("run Prediction workflow task with error: {}", e.message)
        }
    }
}