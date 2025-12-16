package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.dto.seeds.DataLoadRequest
import com.aikya.orchestrator.service.DataSeedsService
import com.aikya.orchestrator.service.OrchestrationClientCallService
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_8
import com.aikya.orchestrator.utils.AppUtils.display
import com.aikya.orchestrator.utils.AppUtils.displayStep
import com.aikya.orchestrator.utils.AppUtils.success
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * A scheduled task that finalizes completed client workflows and intelligently triggers
 * retraining on the same datasets.
 *
 * This component periodically checks for workflows that have received a global model update
 * and are awaiting final completion. After marking them as complete, it determines if the
 * dataset used in the workflow is now "free" (i.e., not being used by any other pending workflow)
 * and, if so, initiates a new workflow to retrain on that data.
 *
 * @param dataSeedsService Service for accessing data batch (seed) metadata.
 * @param clientCallService Service for making outbound calls, specifically to start a new data load.
 */
@Component
class CompleteClientWorkflowTask(private val dataSeedsService: DataSeedsService,
                                 private val  clientCallService: OrchestrationClientCallService
): BaseClientWorkflowTask() {
    /**
     * Periodically finds and completes client workflows that are awaiting finalization.
     *
     * This scheduled method queries for workflows that have finished all federated steps and
     * are ready to be marked as `COMPLETE`. For each such workflow, it advances the state to the
     * final step and then triggers a check to see if retraining should occur.
     */
//    @Scheduled(initialDelay=0, fixedRateString = "\${app.schedule.predict-init.interval}")
    @Scheduled(initialDelay=0, fixedRate=8000)
    fun runCompleteWorkflowTask() {
        try {
            val initClientCompleteWorkflows = workflowService.findAllAwaitingCompletedWorkflows()
            if(initClientCompleteWorkflows.isNotEmpty()) {
                val completedWorkflowTraceIds = mutableListOf<String>()
                for(initClientCompleteWorkflow in initClientCompleteWorkflows) {
                    val workflowTraceId = initClientCompleteWorkflow.workflowTraceId!!
                    clientWorkflowService.completeClientCurrentWorkflowStep(initClientCompleteWorkflow, Flow_Client_8)
                    completedWorkflowTraceIds.add(workflowTraceId)
                    displayStep(logger, workflowTraceId, Flow_Client_8)
                    success(logger, "TRACE-ID: ${workflowTraceId} ------------------------------>  Client  Workflow Complete! <-------------------------------", )
                }
                retrain(completedWorkflowTraceIds)
            }
        }  catch (e: Exception) {
            logger.error("run complete workflow task with error: {}", e.message)
        }
    }
    /**
     * Determines if a new training cycle should be initiated for datasets from recently
     * completed workflows.
     *
     * This logic prevents multiple workflows from running on the same dataset simultaneously. It checks
     * if any other pending workflows are using the same source file as the just-completed workflow.
     * If no other workflows are using the file, it is considered "free" for retraining, and a new
     * data loading process is initiated to start a new cycle.
     *
     * @param completedWorkflowTraceIds A list of trace IDs for the workflows that were just completed.
     */
    fun retrain(completedWorkflowTraceIds: List<String>) {
        try {
            val fileMap = HashMap<String, Boolean>()
            for(completedWorkflowTraceId in completedWorkflowTraceIds) {
                val dataSeedEntry = dataSeedsService.findDataSeedByWorkflowTraceId(completedWorkflowTraceId)
                val fileName =  dataSeedEntry.fileName!!
                val domain =  dataSeedEntry.domainType!!
                val key = "$fileName:$domain"
                if (!fileMap.containsKey(key)) {
                    fileMap[key] = false
                }
                val allDataSeeds = dataSeedsService.getAllDataSeedsByFileName(fileName)
                val existTraceIds = mutableSetOf<String>()
                for(dataSeed in allDataSeeds) {
                    val dbExistTraceId =  dataSeed.workflowTraceId!!
                    if(dbExistTraceId!=completedWorkflowTraceId) {
                        existTraceIds.add(dbExistTraceId)
                    }
                }
                if(existTraceIds.isNotEmpty()) {
                    val workflows = workflowService.getPendingWorkflowsByTraceIdsAndStatus(existTraceIds.toList())
                    if(workflows.isNotEmpty()) {
                        fileMap[key] = true
                    }
                }
            }
            if(fileMap.isNotEmpty()) {
                val keysWithFalseValues = fileMap.filterValues { !it }.keys
                keysWithFalseValues.forEach { key ->
                    val (fileName, domain) = key.split(":")
                    val dataLoadRequest = DataLoadRequest()
                    dataLoadRequest.fileName =fileName
                    dataLoadRequest.domainType = domain
                    dataLoadRequest.mockerEnabled = false
                    display(logger, " Retrain start new workflow. send data-init request, fileName: ${dataLoadRequest.fileName}, domain: ${dataLoadRequest.domainType}" )
                    clientCallService.callDataInitLoad(dataLoadRequest)
                }
            }
        }  catch (e: Exception) {
            logger.error("run retrain task with error: {}", e.message)
        }
    }
}