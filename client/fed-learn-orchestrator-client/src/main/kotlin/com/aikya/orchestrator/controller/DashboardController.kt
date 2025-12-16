package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.ShapleyValues
import com.aikya.orchestrator.dto.common.ClientRunModel
import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.workflow.PendingWorkflowResponse
import com.aikya.orchestrator.service.ClientFacadeService
import com.aikya.orchestrator.service.DashboardService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
/**
 * DashboardController provides REST endpoints to fetch and manage
 * federated learning dashboard data, including model predictions,
 * summary charts, workflow toggles, SHAP values, and manual reset operations.
 *
 * This controller acts as a client-facing API to query domain-specific data.
 */
@RestController
class DashboardController(
    private val dashboardService: DashboardService,
    private val clientFacadeService: ClientFacadeService
) {
    private val logger: Logger = LoggerFactory.getLogger(DashboardController::class.java)

    /**
     * Retrieves item details by ID for the given domain.
     *
     * @param domain the domain name
     * @param id the ID of the item
     * @return a list of item fields as maps
     */
    @RequestMapping(value = ["/{domain}/{id}"], method = arrayOf(RequestMethod.GET))
    fun getItemDetail(
        @PathVariable(value = "domain") domain: String,
        @PathVariable(value = "id") id: Long
    ): List<Map<String, Any?>> {
        logger.info("get {} Data, id: {}", domain, id);
        return dashboardService.getItemData(domain, id)
    }

    /**
     * Retrieves prediction grid data for a batch ID.
     *
     * @param domain the domain name
     * @param batchId the ID of the batch
     * @return prediction data formatted for ag-grid
     */
    @RequestMapping(value = ["/{domain}/batch-grid/{batchId}"], method = arrayOf(RequestMethod.GET))
    fun getItemPredictGridByBatch(
        @PathVariable(value = "domain") domain: String,
        @PathVariable(value = "batchId") batchId: String
    ): Map<String, Any> {
        logger.info("get {} Data, batch_id: {}", domain, batchId);
        return dashboardService.getItemPredictGridByBatch(domain, batchId)
    }

    /**
     * Returns the main dashboard grid data for a domain.
     *
     * @param domain the domain name
     * @return dashboard summary and prediction data
     */
    @RequestMapping(value = ["/{domain}/dashboard"], method = arrayOf(RequestMethod.GET))
    fun getModelDashBoard(@PathVariable(value = "domain") domain: String): Map<String, Any?> {
        return dashboardService.getModelDashboardGridData(domain)
    }

    /**
     * Searches dashboard data within the given date range.
     *
     * @param domain the domain name
     * @param start start date in YYYYMMDD format
     * @param end end date in YYYYMMDD format
     * @return filtered dashboard data
     */
    @RequestMapping(value = ["/{domain}/dashboard/search"], method = arrayOf(RequestMethod.GET))
    fun searchDashBoard(
        @PathVariable(value = "domain") domain: String,
        @RequestParam(value = "start") start: String,
        @RequestParam(value = "end") end: String
    ): Map<String, Any?> {
        validateDateFormat(start)
        validateDateFormat(end)
        return dashboardService.searchDashBoard(domain, start, end)
    }

    private fun validateDateFormat(date: String) {
        val dateFormat = Regex("^\\d{8}$") // Matches YYYYMMDD format
        require(date.matches(dateFormat)) {
            "Invalid date format: $date. Expected format is YYYYMMDD."
        }
    }
    /**
     * Resets dashboard data and local state.
     *
     * @return web response with success status
     */
    @RequestMapping(value = ["/reset"], method = arrayOf(RequestMethod.GET))
    fun reset(): WebResponse {
        return clientFacadeService.reset()
    }

    /**
     * Gets the current status of the workflow run model toggle.
     *
     * @param domain the domain name
     * @return current run model setting
     */
    @RequestMapping(value = ["/{domain}/worklflowRunModel"], method = arrayOf(RequestMethod.GET))
    fun getWorkflowRunModel(@PathVariable(value = "domain") domain: String): ClientRunModel {
        return dashboardService.getWorkflowRunModel(domain)
    }

    /**
     * Gets the current status of the global weight run model toggle.
     *
     * @param domain the domain name
     * @return current run model setting
     */
    @RequestMapping(value = ["/{domain}/globalWeightRunModel"], method = arrayOf(RequestMethod.GET))
    fun getGlobalWeightRunModel(@PathVariable(value = "domain") domain: String): ClientRunModel {
        return dashboardService.getWeightRunModel(domain)
    }

    /**
     * Toggles the global weight run model mode for a given ID.
     *
     * @param modeId the mode ID to toggle
     * @return updated run model
     */
    @RequestMapping(value = ["/toggle/globalWeightRunModel/{modeId}"], method = arrayOf(RequestMethod.POST))
    fun toggleWorkflowRunModel(@PathVariable(value = "modeId") modeId: Long): ClientRunModel {
        return dashboardService.toggleGlobalWeightRunModel(modeId)
    }


    /**
     * Toggles the workflow run model mode for a given ID.
     *
     * @param modeId the mode ID to toggle
     * @return updated run model
     */
    @RequestMapping(value = ["/toggle/worklflowRunModel/{modeId}"], method = arrayOf(RequestMethod.POST))
    fun toggleGlobalWeightRunModel(@PathVariable(value = "modeId") modeId: Long): ClientRunModel {
        return dashboardService.setWorkflowManualRunModel(modeId)
    }

    /**
     * Triggers global model aggregation manually for a specific workflow trace ID.
     *
     * @param workflowTraceId the workflow trace ID
     * @return pending workflow summary
     */
    @RequestMapping(value = ["/sendGlobalModelAggregate/{workflowTraceId}"], method = arrayOf(RequestMethod.POST))
    fun sendGlobalModelAggregate(@PathVariable(value = "workflowTraceId") workflowTraceId: String): PendingWorkflowResponse {
        clientFacadeService.skipSendFeedback(workflowTraceId)
        return clientFacadeService.getPendingWorkflowSummaryByTraceId(workflowTraceId)
    }
    /**
     * Retrieves SHAP explainer values for a given batch.
     *
     * @param domain the domain name
     * @param batchId the batch ID
     * @return list of SHAP value objects
     */
    @GetMapping("/{domain}/shap-values/{batchId}")
    fun getShapleyExplainerValues(
        @PathVariable domain: String,
        @PathVariable batchId: String
    ): List<ShapleyValues> {
        return dashboardService.getShapExplainerValues(domain, batchId);
    }
}