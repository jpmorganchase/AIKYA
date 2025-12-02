package com.aikya.orchestrator.service.protocol

import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
/**
 * Defines the contract for protocol-specific handlers.
 *
 * This interface is part of a strategy pattern, allowing the application to switch
 * between different communication protocols (e.g., HTTP, Blockchain) seamlessly.
 */
interface ProtocolHandler {
    /**
     * Handles the server-to-client communication step for a given workflow.
     *
     * Implementing classes should contain the specific logic required to transmit
     * information or signal completion to a client using that handler's protocol.
     *
     * @param workflowDetail The entity containing data for the current workflow task.
     */
    fun handleServerToClient(workflowDetail: WorkflowDetailEntity)
}