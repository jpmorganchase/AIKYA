package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.utils.AppConstants.Flow_Server_2
import com.aikya.orchestrator.utils.AppUtils.displayStep
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * Scheduled task responsible for initiating the federated learning aggregation process.
 *
 * This task:
 * - Identifies workflows that have completed client updates and are ready for aggregation (Flow_Server_2 step).
 * - Processes current-round clients to generate an aggregation request.
 * - Invokes the aggregation process by calling the federated learning aggregation service.
 *
 * Runs at a fixed interval defined by the property `app.schedule.client-aggregation-init.interval`.
 */
@Component
class ClientFlAggregateTask: BaseServerWorkflowTask() {
    /**
     * Periodic task that looks for workflows ready to begin aggregation (at Flow_Server_2),
     * generates the aggregation request, and triggers the aggregation process.
     *
     * If the request form cannot be built (e.g., due to missing or invalid data),
     * it logs a warning and skips the current workflow.
     */
    @Scheduled(fixedRateString = "\${app.schedule.client-aggregation-init.interval}")
    fun findClientAggInitRunTask() {
        val initialAggWorkflows = workflowService.findReceivedInitAggWorkflows(Flow_Server_2.step)
        if(initialAggWorkflows.isNotEmpty()) {
            for(initialAggWorkflow in initialAggWorkflows) {
                val workflowTraceId = initialAggWorkflow.workflowTraceId!!
                displayStep(logger, workflowTraceId, Flow_Server_2)
                val aggRequestForm =  orchestrationServerService.processCurrentRoundClients(workflowTraceId)
                if (aggRequestForm != null) {
                    orchestrationServerCallService.callFedLearnAggregate(workflowTraceId, aggRequestForm)
                } else {
                    logger.warn("Failed to build FlAggRequestForm. Cannot proceed with aggregation.")
                }
            }
        }
    }
}