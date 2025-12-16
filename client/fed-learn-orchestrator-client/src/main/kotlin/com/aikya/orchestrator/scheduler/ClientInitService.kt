package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.service.ClientFacadeService
import com.aikya.orchestrator.service.DashboardService
import com.aikya.orchestrator.utils.AppUtils.display
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
/**
 * ClientInitService is responsible for initializing the client node
 * during application startup. It performs client registration and ensures
 * that the global model weights are set up correctly for the first run.
 *
 * This service is triggered automatically via the `@PostConstruct` lifecycle hook.
 */
@Service
class ClientInitService(
    private val clientFacadeService: ClientFacadeService,
    private val dashboardService: DashboardService
) {
    private val logger: Logger = LoggerFactory.getLogger(ClientInitService::class.java)
    /**
     * Initialization hook that executes after the bean is constructed.
     *
     * This method performs the following:
     * - Registers the client with the aggregation server or coordinator.
     * - Ensures initial global model weights are set up properly.
     */
    @PostConstruct
    fun init() {
        registerClient()
        initialGlobalModelWeight()
    }
    /**
     * Registers the client with the orchestration or aggregation server.
     *
     * This setup step is required to ensure the client is known
     * to the server for federated learning coordination.
     */
    fun registerClient() {
        display(logger, "check and register client with aggregate")
        //this behavior will be updated later
        clientFacadeService.registerClient()
    }
    /**
     * Initializes or checks the global model weights for this client node.
     *
     * This ensures that the model state is correctly seeded before
     * participating in federated learning rounds.
     */
    fun initialGlobalModelWeight() {
        display(logger, "check and Setup global model weight data")
        clientFacadeService.initialGlobalModelWeight()
    }
}