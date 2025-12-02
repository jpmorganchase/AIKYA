package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_2
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_3
import com.aikya.orchestrator.utils.AppUtils.displayStep
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Scheduled task that checks if federated learning client aggregation rounds have completed
 * and updates the server workflow state accordingly.
 *
 * This task periodically:
 * - Finds workflows pending at aggregation step (Flow_Server_2).
 * - Checks if client collaboration rounds are completed.
 * - Updates the current round in the model collaboration run.
 * - Verifies if model aggregation weights are available.
 * - Advances the workflow to the next step (Flow_Server_3) and marks current step as COMPLETE.
 */
@Component
class ClientAggRunCompleteTask: BaseServerWorkflowTask() {
    /**
     * Scheduled method to scan for pending client aggregation workflows
     * and complete them if aggregation results are available.
     *
     * This runs at a fixed interval defined by the property:
     * `app.schedule.client-aggregation-complete.interval`.
     */
    @Scheduled(fixedRateString = "\${app.schedule.client-aggregation-complete.interval}")
    fun findClientAggCompleteTask() {
        val pendingAggWorkflows = workflowService.findPendingAggWorkflows(Flow_Server_2.step)
        if(pendingAggWorkflows.isNotEmpty()) {
            for(pendingAggWorkflow in pendingAggWorkflows) {
                val workflowTraceId = pendingAggWorkflow.workflowTraceId!!
                val collaborationRunClients = modelService.findCollaborationRunClient(workflowTraceId)
                if (collaborationRunClients.isNotEmpty()) {
                    // Assuming the collaborationRunClient has a property to get the runId
                    val collaborationRunClient = collaborationRunClients[0]
                    val runId = collaborationRunClient.run!!.id
                    // Retrieve the FLModelCollaborationRun record using the runId
                    val collaborationRunEntity = modelService.findModelCollaborationRunById(runId)
                    if (collaborationRunEntity.isPresent) {
                        val round = collaborationRunClient.rounds
                        val collaborationRun = collaborationRunEntity.get()
                        if(collaborationRun.currentRound!=round) {
                            collaborationRun.currentRound = round
                            modelService.saveModelCollaborationRun(collaborationRun)
                        }
                        val modelAggregateWeights = modelService.findModelAggregateWeightsByWorkflowTraceId(workflowTraceId)
                        if(modelAggregateWeights.isPresent) {
                            displayStep(logger, workflowTraceId, Flow_Server_3)
                            workflowService.updateServerWorkflow(workflowTraceId, COMPLETE, Flow_Server_2)
                        }
                    }
                }
            }
        }
    }
}