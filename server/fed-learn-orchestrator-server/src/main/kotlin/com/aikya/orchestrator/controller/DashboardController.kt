package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.common.ContributionsRes
import com.aikya.orchestrator.dto.common.DomainDTO
import com.aikya.orchestrator.dto.common.PerformancesRes
import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.AggregateStrategyDTO
import com.aikya.orchestrator.dto.fedlearn.FlRunModelGroupRequest
import com.aikya.orchestrator.dto.workflow.WorkflowNetworkSummary
import com.aikya.orchestrator.service.DashboardService
import com.aikya.orchestrator.service.ServerAdminService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * REST controller for handling dashboard-related operations, including domain and strategy queries,
 * model group registration, and federated learning model summaries and performance.
 *
 * Provides endpoints for:
 * - Health checks
 * - Domain and strategy metadata
 * - Registering model groups for federated learning
 * - Fetching latest summaries, contributions, and performance of models
 */
@RestController
class DashboardController(private val serverAdminService: ServerAdminService,
                      private val dashboardService: DashboardService)  {

    private val logger: Logger = LoggerFactory.getLogger(DashboardController::class.java)

    /**
     * Health check endpoint to verify that the service is running.
     *
     * @return A simple "UP" status string.
     */
    @RequestMapping(value = ["/health"], method = [RequestMethod.GET])
    fun health(): String {
        return "UP"
    }
    /**
     * Retrieves all available domain configurations from the dashboard.
     *
     * @return A list of [DomainDTO] objects representing the domains.
     */
    @RequestMapping(value = ["/domains"], method = arrayOf(RequestMethod.GET))
    fun getAllDomains(): List<DomainDTO> {
        return dashboardService.getAllDomains()
    }
    /**
     * Retrieves all configured aggregate strategies for model evaluation or combination.
     *
     * @return A list of [AggregateStrategyDTO] objects representing the strategies.
     */
    @RequestMapping(value = ["/strategies"], method = arrayOf(RequestMethod.GET))
    fun getAggregateStrategies(): List<AggregateStrategyDTO> {
        return dashboardService.getAggregateStrategies()
    }

    /**
     * Triggers a system-wide reset operation through the server admin service.
     *
     * @return A list of strings indicating reset status or affected components.
     */
    @RequestMapping(value = ["/reset"], method = [RequestMethod.GET])
    fun reset(): List<String> {
        return serverAdminService.reset()
    }
    /**
     * Registers a federated learning model group for execution.
     *
     * @param runModelGroupRequest The request containing group metadata such as name and client ID.
     * @return A [Mono] emitting [WebResponse] with registration result.
     */
    @RequestMapping(value = ["/model-group-register"], method = arrayOf(RequestMethod.POST))
    fun registerRunModelGroup(@RequestBody runModelGroupRequest: FlRunModelGroupRequest): Mono<WebResponse> {
        logger.info("register group: name: {}, clientId: {}", runModelGroupRequest.name, runModelGroupRequest.clientId);
        val webRes = dashboardService.registerRunModelGroup(runModelGroupRequest)
        return Mono.just(webRes)
    }
    /**
     * Fetches the latest federated workflow summary for a given model.
     *
     * @param modelName The name of the model.
     * @return A [WorkflowNetworkSummary] object summarizing the latest FL run, or null if unavailable.
     */
    @RequestMapping(value = ["/{modelName}/latestWorkflowSummary"], method = arrayOf(RequestMethod.GET))
    fun getLatestWorkflowSummary(@PathVariable(value = "modelName") modelName: String): WorkflowNetworkSummary? {
        return dashboardService.getLatestNetworkSummary(modelName)
    }
    /**
     * Retrieves the contributions made by various nodes for a given model.
     *
     * @param modelName The name of the model.
     * @return A [Mono] emitting a [ContributionsRes] object summarizing contributions.
     */
    @RequestMapping(value = ["/{modelName}/contributions"], method = arrayOf(RequestMethod.GET))
    fun getContributions(@PathVariable(value = "modelName") modelName: String): Mono<ContributionsRes>  {
        return Mono.just(dashboardService.getContributions(modelName))
    }
    /**
     * Retrieves the model's performance metrics (e.g., accuracy) across runs.
     *
     * @param modelName The name of the model.
     * @return A [Mono] emitting a [PerformancesRes] object containing performance metrics.
     */
    @RequestMapping(value = ["/{modelName}/performances"], method = arrayOf(RequestMethod.GET))
    fun getPerformances(@PathVariable(value = "modelName") modelName: String): Mono<PerformancesRes> {
        return Mono.just(dashboardService.getPerformances(modelName, "accuracy"))
    }

}