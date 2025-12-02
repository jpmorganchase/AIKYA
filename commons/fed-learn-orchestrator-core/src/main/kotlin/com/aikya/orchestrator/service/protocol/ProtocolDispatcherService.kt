package com.aikya.orchestrator.service.protocol

import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
/**
 * A service that dispatches tasks to different protocol handlers based on application configuration. ⚙️
 *
 * This service acts as a router, selecting the appropriate handler (e.g., for blockchain or HTTP)
 * based on the `app.protocol` property. It implements a strategy pattern to delegate the
 * actual task handling to a specific [ProtocolHandler] implementation.
 *
 * @property blockchainProtocolHandler The handler for blockchain-based communication.
 * @property httpProtocolHandler The handler for standard HTTP-based communication.
 */
@Service
class ProtocolDispatcherService(
    private val blockchainProtocolHandler: BlockchainProtocolHandler,
    private val httpProtocolHandler: HttpProtocolHandler) {
    @Value("\${app.protocol}")
    private val protocol ="blockchain"
    private val logger: Logger = LoggerFactory.getLogger(ProtocolDispatcherService::class.java)
    /**
     * Dispatches the server-to-client handling task to the configured protocol handler.
     *
     * It determines the active protocol from the application configuration and uses the corresponding
     * handler to process the workflow detail.
     *
     * @param workflowDetail The entity containing details of the workflow task to be handled.
     */
    fun handleServerToClient(workflowDetail: WorkflowDetailEntity) {
        val workflowTraceId = workflowDetail.workflowTraceId!!
        logger.info("TRACE-ID: {} - shareGlobalAggregatedModelTask, protocol: {}", workflowTraceId, protocol)
        val handler = getHandlerForProtocol(protocol)
        handler.handleServerToClient(workflowDetail)
    }
    /**
     * Selects and returns the appropriate protocol handler based on the given protocol name.
     *
     * @param protocol The name of the protocol (e.g., "blockchain", "http").
     * @return The corresponding [ProtocolHandler] implementation.
     * @throws IllegalArgumentException if the specified protocol is not supported.
     */
    private fun getHandlerForProtocol(protocol: String): ProtocolHandler {
        return when (protocol) {
            "blockchain" -> blockchainProtocolHandler
            "http" -> httpProtocolHandler
            else -> throw IllegalArgumentException("Unsupported protocol: $protocol")
        }
    }
}