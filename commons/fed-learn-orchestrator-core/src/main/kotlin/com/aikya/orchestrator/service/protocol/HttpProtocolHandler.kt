package com.aikya.orchestrator.service.protocol

import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.shared.model.workflow.WorkflowDetailEntity
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_4
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
/**
 * The [ProtocolHandler] implementation for standard HTTP communication. 🌐
 *
 * This service handles the server-to-client workflow step by marking the server's workflow
 * as complete. This signals that clients can now pull the latest data via standard HTTP requests.
 *
 * @property workflowService The service used to update workflow states.
 */
@Service
class HttpProtocolHandler(private val workflowService: WorkflowService): ProtocolHandler {
    private val logger: Logger = LoggerFactory.getLogger(HttpProtocolHandler::class.java)
    /**
     * Completes the server workflow step within a database transaction.
     *
     * This implementation updates the workflow's status to 'COMPLETE', allowing clients
     * to poll for and retrieve the final results.
     *
     * @param workflowDetail The entity containing details of the workflow to be completed.
     */
    @Transactional
    override fun handleServerToClient(workflowDetail: WorkflowDetailEntity) {
        val workflowTraceId = workflowDetail.workflowTraceId!!
        logger.info("TRACE-ID: {} - Handling http protocol", workflowTraceId)
        workflowService.updateServerWorkflow(workflowTraceId, COMPLETE, Flow_Server_4)
        //mark server workflow complere, let client to get lastest version
        logger.info("TRACE-ID: {} - Share Global Aggregated Model Completed", workflowTraceId)
    }
}