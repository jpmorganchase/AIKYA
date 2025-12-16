package com.aikya.orchestrator.scheduler

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
/**
 * A scheduled task responsible for periodically checking for and retrieving the latest
 * aggregated model version from the central orchestration server. 🔄
 *
 * This component plays a crucial role in keeping the client's global model up-to-date
 * by automatically polling the server at a fixed interval.
 */
@Component
class ServerLatestAggregationVersionTask : BaseClientWorkflowTask() {
    /**
     * Executes the task to request the latest global model from the remote server.
     *
     * This method is annotated with `@Scheduled` to run at a fixed rate. It calls the
     * `OrchestrationClientCallService` to handle the logic of making the request and
     * processing the response. Any exceptions during the process are caught and logged
     * to prevent the scheduler from halting.
     */
    //@Scheduled(initialDelay=0, fixedRateString = "\${app.schedule.aggregation.interval}")
    @Scheduled(initialDelay=0, fixedRate=1000)
    fun runCheckServerAggregationResult() {
        try {
            orchestrationClientCallService.requestLatestVersionFromRemoteOrchestrationServer()
        }  catch (e: Exception) {
            logger.error("run Request Remote Orchestration workflow task with error: {}", e.message)
        }
    }
}