package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.utils.AppConstants.Flow_Server_4
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * Scheduled task responsible for completing the federated learning workflow
 * by sharing the final aggregated global model with all clients.
 *
 * This step corresponds to [Flow_Server_4], the final step in the server-side workflow.
 *
 * The task:
 * - Identifies workflows that are ready for global model sharing.
 * - Retrieves model versioning information and workflow details.
 * - Dispatches the aggregated global model to participating clients via protocol dispatcher.
 */
@Component
class CompleteServerWorkflowTask: BaseServerWorkflowTask() {
    /**
     * Periodically checks for workflows at the global sharing step (Flow_Server_4),
     * retrieves the latest model version, and sends the final model to all clients.
     *
     * Runs at a fixed interval specified by the property:
     * `app.schedule.client-aggregation-complete.interval`.
     */
    @Scheduled(fixedRateString = "\${app.schedule.client-aggregation-complete.interval}")
    fun shareGlobalAggregatedModelTask() {
        val initShareGlobalAggregatedModelWorkflows = workflowService.findShareGlobalAggregatedModel(Flow_Server_4.step)
        if(initShareGlobalAggregatedModelWorkflows.isNotEmpty()) {
            for(initShareGlobalAggregatedModel in initShareGlobalAggregatedModelWorkflows) {
                val workflowTraceId = initShareGlobalAggregatedModel.workflowTraceId!!
                //get model weight, metrics
                //send http request to client
                val workflowEntity = workflowService.findWorkflowByTraceId(workflowTraceId)
                var currentMaxVersion = 0L
                var modelId = 0L
                if(workflowEntity.isPresent) {
                    val wf = workflowEntity.get()
                    modelId = wf.modelId
                    val currentMaxVersionWeight = modelService.getMaxWeightsVersionForModelId(wf.modelId)
                    currentMaxVersion = currentMaxVersionWeight?.version ?: 0L
                }
                logger.info("TRACE-ID: {} - handleServerToClient", workflowTraceId)
                protocolDispatcherService.handleServerToClient(initShareGlobalAggregatedModel)
                logger.info("TRACE-ID: ${workflowTraceId} ------------------------------>  Server  Workflow Complete!, Version: ${currentMaxVersion}, Model: ${modelId} <-------------------------------")
            }
        }
    }
}