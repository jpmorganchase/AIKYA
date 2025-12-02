package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.DataSeedLabelRequest
import com.aikya.orchestrator.dto.common.DomainDTO
import com.aikya.orchestrator.dto.common.SummaryChartRes
import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.FeedbackRequest
import com.aikya.orchestrator.dto.fedlearn.ModelResponse
import com.aikya.orchestrator.dto.fedlearn.ModelVersionTrackResponse
import com.aikya.orchestrator.dto.seeds.DataLoadRequest
import com.aikya.orchestrator.dto.seeds.DataSeedMetaData
import com.aikya.orchestrator.dto.workflow.PendingWorkflowResponse
import com.aikya.orchestrator.service.ClientFacadeService
import com.aikya.orchestrator.service.DashboardService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono


@RestController
class ClientOrchestratorController(
    private val clientFacadeService: ClientFacadeService,
    private val dashboardService: DashboardService
) {
    private val logger: Logger = LoggerFactory.getLogger(ClientOrchestratorController::class.java)

    @RequestMapping(value = ["/domains"], method = arrayOf(RequestMethod.GET))
    fun getAllDomains(): List<DomainDTO> {
        return clientFacadeService.getAllDomains()
    }

    /**
     * get initial data seed meta information
     */
    @RequestMapping(value = ["/{domain}/getInitialDataSeeds"], method = arrayOf(RequestMethod.GET))
    fun getInitialDataSeeds(@PathVariable(value = "domain") domain: String): Mono<List<DataSeedMetaData>> {
        return clientFacadeService.getInitialDataSeeds(domain)
    }

    /**
     * init load seed
     */
    @RequestMapping(value = ["/loadLocalData"], method = arrayOf(RequestMethod.POST))
    fun loadLocalData(@RequestBody dataLoadRequest: DataLoadRequest): Mono<WebResponse> {
        logger.info("loadLocalData, fileName: {}", dataLoadRequest.fileName);
        val webRes = clientFacadeService.loadLocalData(dataLoadRequest)
        return Mono.just(webRes)
    }

    @RequestMapping(value = ["/sendFeedback"], method = arrayOf(RequestMethod.POST))
    fun sendFeedback(@RequestBody feedbackReq: FeedbackRequest): String {
        logger.info("sendFeedback, batch ID: {}", feedbackReq.batchId)
        val feedbackRes = clientFacadeService.sendFeedback(feedbackReq)
        if (feedbackRes.success!!) {
            val res = clientFacadeService.callTraining(feedbackRes)
            return res.message!!
        }
        return feedbackRes.message!!
    }

    @RequestMapping(value = ["/{domain}/model"], method = arrayOf(RequestMethod.GET))
    fun getModel(@PathVariable(value = "domain") domainType: String): Mono<ModelResponse> {
        return Mono.just(clientFacadeService.getModel(domainType))
    }

    /**
     * load detail of batch data per batch id
     */
    @RequestMapping(value = ["/{domain}/getPendingWorkflow"], method = arrayOf(RequestMethod.GET))
    fun getPendingWorkflow(@PathVariable(value = "domain") domain: String): PendingWorkflowResponse {
        return clientFacadeService.getPendingWorkflowSummary(domain)
    }

    /**
     * get latest model version
     */
    @RequestMapping(value = ["/{domain}/getLatestGlobalModel"], method = arrayOf(RequestMethod.GET))
    fun getLatestGlobalModel(
        @PathVariable(value = "domain") domain: String,
        @RequestHeader(name = "idToken", required = false) idToken: String?,
        @RequestParam(name = "version") version: Int
    ): Mono<ModelVersionTrackResponse> {
        return clientFacadeService.getLatestGlobalModel(idToken, version, domain)
    }

    @RequestMapping(value = ["/{domain}/getSummaryChart"], method = arrayOf(RequestMethod.GET))
    fun getLocalNodeSummaryChart(@PathVariable(value = "domain") domain: String): Mono<SummaryChartRes> {
        return clientFacadeService.getLocalNodeSummaryChart(domain)
    }

    @PostMapping("/updateDataSeedMeta")
    fun updateDataSeedMeta(@RequestBody dataSeedLabelRequests: List<DataSeedLabelRequest>): ResponseEntity<*> {
        // Iterate over each DataSeedLabelRequest in the list and call the service method for each
        for (request in dataSeedLabelRequests) {
            try {
                // Call the save method for each request
                clientFacadeService.saveDataSeedLabel(request)
            } catch (e: Exception) {
                // Handle exception and return error response if needed
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing request for file: " + request.fileName)
            }
        }
        return ResponseEntity.ok("All DataSeedMeta updated successfully")
    }

    // If you want to handle a single request as well
    @PostMapping("/updateDataSeedMeta/single")
    fun updateSingleDataSeedMeta(@RequestBody dataSeedLabelRequest: DataSeedLabelRequest): ResponseEntity<*> {
        clientFacadeService.saveDataSeedLabel(dataSeedLabelRequest)
        return ResponseEntity.ok("Single DataSeedMeta updated successfully")
    }
}