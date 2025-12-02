package com.aikya.orchestrator.service

import com.aikya.orchestrator.dto.fedlearn.AggregateResponse
import com.aikya.orchestrator.dto.fedlearn.FlAggRequestForm
import com.aikya.orchestrator.service.common.CallerService
import com.aikya.orchestrator.service.workflow.WorkflowService
import com.aikya.orchestrator.utils.AppConstants.FAIL
import com.aikya.orchestrator.utils.AppConstants.Flow_Server_2
import com.aikya.orchestrator.utils.AppConstants.PENDING
import com.aikya.orchestrator.utils.AppUtils
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
/**
 * Service responsible for calling the external federated learning aggregation endpoint.
 *
 * This class prepares and sends aggregation requests to the aggregation service,
 * handles responses, and updates the workflow status accordingly.
 *
 * It acts as a bridge between the server orchestration logic and the external aggregation logic.
 *
 * @param callerService The service used for making HTTP POST calls.
 * @param workflowService The service for updating workflow status.
 * @param modelService The service for accessing model-related information.
 */
@Transactional
@Service
class OrchestrationServerCallService(private val callerService: CallerService,
                                     private val workflowService: WorkflowService,
                                     private val modelService: ModelService,) {
    private val logger: Logger = LoggerFactory.getLogger(OrchestrationServerCallService::class.java)
    @Value("\${app.url.fedagg}")
    val fedAggUrl = ""

    /**
     * Sends a federated learning aggregation request to the aggregation endpoint.
     *
     * The request body is converted to JSON and sent via [CallerService]. Depending on the response,
     * the workflow step ([Flow_Server_2]) is updated to either `PENDING` or `FAIL`.
     *
     * @param workflowTraceId The workflow trace ID for tracking the request.
     * @param aggRequestForm The request payload containing model aggregation data.
     */
    fun callFedLearnAggregate(workflowTraceId: String, aggRequestForm: FlAggRequestForm) {
        try {
            val requestForm = convertToJson(aggRequestForm)
            // Log the request body at debug level (avoid logging sensitive data)
            AppUtils.display(logger, "TRACE-ID: ${workflowTraceId} - calling aggregate function Request body JSON: ${requestForm}")
            val response: AggregateResponse = callerService.post(
                fedAggUrl,
                requestForm,
                AggregateResponse::class.java
            )
            logger.info("TRACE-ID: {} -received aggregate result, status: {}", response.workflowTraceId, response.status)
            if (response.status != "success") {
                workflowService.updateServerWorkflow(workflowTraceId, FAIL, Flow_Server_2)
            } else {
                workflowService.updateServerWorkflow(workflowTraceId, PENDING, Flow_Server_2)
            }
        } catch (ex: Exception) {
            // Log the exception with an appropriate error message
            logger.error("TRACE-ID: $workflowTraceId - Error during aggregate function call", ex)
            // Update the workflow as failed
            workflowService.updateServerWorkflow(workflowTraceId, FAIL, Flow_Server_2)
        }
    }
    /**
     * Converts the [FlAggRequestForm] to its JSON string representation using Jackson.
     *
     * @param requestForm The aggregation request form.
     * @return A JSON string representation of the form.
     */
    fun convertToJson(requestForm: FlAggRequestForm): String {
        val objectMapper = jacksonObjectMapper()
        return objectMapper.writeValueAsString(requestForm)
    }
}