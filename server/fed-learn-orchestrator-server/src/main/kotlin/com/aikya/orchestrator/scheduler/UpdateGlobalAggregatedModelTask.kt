package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_3
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * Scheduled task responsible for finalizing the global aggregation step
 * in the federated learning workflow (Flow_Server_3).
 *
 * This task:
 * - Identifies workflows at the global aggregation update step.
 * - Marks the aggregation step (Flow_Server_3) as complete.
 * - Logs completion for traceability.
 */
@Component
class UpdateGlobalAggregatedModelTask: BaseServerWorkflowTask() {
    /**
     * Periodically checks for workflows ready to finalize global aggregation (Flow_Server_3),
     * and marks them as complete in the server-side workflow tracking system.
     *
     * Runs at a fixed interval defined by the property:
     * `app.schedule.client-aggregation-complete.interval`.
     */
    @Scheduled(fixedRateString = "\${app.schedule.client-aggregation-complete.interval}")
    fun updateGlobalAggregatedModelTask() {
        val initUpdateGlobalAggregatedAggWorkflows = workflowService.findUpdateGlobalAggregatedModel(Flow_Server_3.step)
        if(initUpdateGlobalAggregatedAggWorkflows.isNotEmpty()) {
            for(initUpdateGlobalAggregatedAgg in initUpdateGlobalAggregatedAggWorkflows) {
                val workflowTraceId = initUpdateGlobalAggregatedAgg.workflowTraceId!!
                workflowService.updateServerWorkflow(workflowTraceId, COMPLETE, Flow_Server_3)
                logger.info("UpdateGlobalAggregatedAgg $workflowTraceId")
            }
        }
    }
}