package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.ClientRequestWithWorkflowStatus
import com.aikya.orchestrator.dto.fedlearn.WorkflowTraceIdsRequest
import com.aikya.orchestrator.dto.message.Message
import com.aikya.orchestrator.service.OrchestrationServerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
/**
 * REST controller for orchestrating the federated learning workflow at the server side.
 *
 * This controller handles client communication, model distribution, aggregation requests,
 * and diagnostic operations such as checking pending or stalled client states.
 */
@RestController
class ServerOrchestrationController(private val orchestrationServerService: OrchestrationServerService) {
    private val logger: Logger = LoggerFactory.getLogger(ServerOrchestrationController::class.java)

    /**
     * Receives a federated learning message from a client.
     * This could contain model updates, participation requests, or status events.
     *
     * @param message The incoming [Message] from a client.
     * @return A [Mono] emitting a [WebResponse] acknowledging receipt.
     */
    @PostMapping("/receive")
    fun receiveMessage(@RequestBody message: Message): Mono<WebResponse> {
        val webRes = WebResponse()
        webRes.message = "Received message -" + message.header!!.workflow_trace_id
        // Handle the message here
        logger.info("Received message: {}", webRes.message)
        orchestrationServerService.processClientMessage(message)
        return Mono.just(webRes)
    }
    /**
     * Returns the latest aggregated global model to the requesting client.
     *
     * @param message A [Message] that includes the client context and workflow ID.
     * @return A [Mono] emitting the global [Message] containing the aggregated model.
     */
    @PostMapping("/global-model")
    fun getGlobalModel(@RequestBody message: Message): Mono<Message> {
        val message = orchestrationServerService.getAggregateGlobalModel(message)
        return Mono.just(message)
    }
    /**
     * Provides the initial global model weights for a given model name.
     * Typically used during the first round of training for a new client or session.
     *
     * @param modelName The name of the model.
     * @return A [Mono] emitting a [Message] with initial global weights.
     */
    @PostMapping("/{modelName}/initialGlobalModelWeight")
    fun getInitialGlobalModelWeight(@PathVariable(value = "modelName") modelName: String): Mono<Message> {
        val message = orchestrationServerService.getInitialGlobalModelWeight(modelName)
        return Mono.just(message)
    }
    /**
     * Triggers expiration logic to clean up or reject pending client requests
     * that have stalled or failed to respond within a valid timeframe.
     *
     * @return A [Mono] emitting a [WebResponse] indicating completion.
     */
    @PostMapping(value = ["expire-pending-clients"])
    fun expirePendingClients(): Mono<WebResponse> {
        val webRes = WebResponse()
        webRes.message = ""
        orchestrationServerService.manageStalledClientRequestProcesses()
        return Mono.just(webRes)
    }
    /**
     * Checks the status of specific client requests using provided workflow trace IDs.
     * Useful for debugging or tracking client participation in federated workflows.
     *
     * @param request The [WorkflowTraceIdsRequest] containing a list of trace IDs.
     * @return A [Mono] emitting a list of [ClientRequestWithWorkflowStatus] with current statuses.
     */
    @PostMapping(value = ["/check-requests"])
    fun checkClientRequests(@RequestBody request: WorkflowTraceIdsRequest): Mono<List<ClientRequestWithWorkflowStatus>> {
        return Mono.just(orchestrationServerService.checkClientRequest(request.workflowTraceIds))
    }
}