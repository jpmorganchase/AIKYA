package com.aikya.orchestrator.service.protocol

import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
/**
 * The [ProtocolHandler] implementation for blockchain-based communication. ⛓️
 *
 * This service is responsible for handling all server-to-client interactions
 * that occur over a blockchain network, such as submitting transactions or
 * interacting with smart contracts.
 */
@Service
class BlockchainProtocolHandler: ProtocolHandler {
    private val logger: Logger = LoggerFactory.getLogger(BlockchainProtocolHandler::class.java)
    /**
     * Executes the server-to-client workflow step using blockchain-specific logic.
     *
     * @param workflowDetail The entity containing details of the workflow task.
     */
    override fun handleServerToClient(workflowDetail: WorkflowDetailEntity) {
        val workflowTraceId = workflowDetail.workflowTraceId!!
        logger.info("TRACE-ID: {} - Handling blockchain protocol", workflowTraceId)
        // Blockchain specific logic here
    }
}