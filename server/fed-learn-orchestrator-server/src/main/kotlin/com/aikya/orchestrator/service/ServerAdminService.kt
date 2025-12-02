package com.aikya.orchestrator.service

import com.aikya.orchestrator.aggregate.model.FlClient
import com.aikya.orchestrator.aggregate.repository.FlClientRepository
import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.ClientRegisterRequest
import com.aikya.orchestrator.repository.server.OrchestratorServerQueryRepository
import com.aikya.orchestrator.utils.AppConstants
import com.aikya.orchestrator.utils.AppUtils
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

/**
 * Service responsible for administrative operations at the server level,
 * including:
 * - Initial client registration from environment variables
 * - Dynamic registration of clients via API
 * - Resetting and truncating key database tables for clean state management
 *
 * Typically used for initializing federated learning clients and resetting orchestrator data.
 *
 * @param flClientRepository Repository for accessing and persisting [FlClient] data.
 * @param env Spring [Environment] for reading configuration properties.
 */
@Service
class ServerAdminService (val flClientRepository: FlClientRepository,
                          val env: Environment
){
    private val logger: Logger = LoggerFactory.getLogger(ServerAdminService::class.java)
    @Autowired
    @Qualifier("serverQueryRepository")
    private lateinit var serverQueryRepository: OrchestratorServerQueryRepository
    /**
     * Initializes clients based on the environment property `clients.list`.
     *
     * Format: `clientName1,clientId1,clientEmail1;clientName2,clientId2,clientEmail2`
     *
     * Each client in the list is automatically registered if not already present.
     */
    @PostConstruct
    fun initClients() {
        val clientListString = env.getProperty("clients.list", "")
        val clients = clientListString.split(";").map { it.split(",") }
        clients.forEach { client ->
            val (clientName, clientId, clientEmail) = client
            val clientRegisterRequest = ClientRegisterRequest()
            clientRegisterRequest.clientName = clientName
            clientRegisterRequest.clientId = clientId.toInt()
            clientRegisterRequest.email = clientEmail
            registerClient(clientRegisterRequest)
        }
    }
    /**
     * Resets the orchestrator database by truncating critical federated learning tables.
     * Disables foreign key checks temporarily to ensure truncation works without violations.
     *
     * @return A list of table names that were truncated.
     */
    @Transactional(readOnly = false)
    fun reset(): List<String> {
        logger.info("truncate Tables...")
        val start = System.currentTimeMillis()
        val tables = listOf(
            "collaboration_run_client",
            "model_client_training_result",
            "run_model_aggregation",
            "workflow_detail",
            "workflow",
            "model_collaboration_run",
            "model_aggregate_weights",
            "metrics"
        )
        // Disable foreign key checks
        serverQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
        for(table in tables) {
            truncateTable(table)
        }
        // Enable foreign key checks
        serverQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
        logger.info("--------  truncated all tables ----------------> duration: {} sec", TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - start)))
        return tables
    }
    /**
     * Truncates a single table with foreign key constraints temporarily disabled.
     *
     * @param table The name of the table to truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun truncateTable(table: String) {
        logger.info("--------  truncating table  {} ----------------", table)
        serverQueryRepository.truncateTable(table)
    }
    /**
     * Registers a new federated learning client by checking if the client ID or email
     * already exists. If not, it creates and stores a new [FlClient] record.
     *
     * @param clientRegisterRequest The request containing client ID, name, and email.
     * @return A [WebResponse] indicating whether the registration was successful.
     */
    @Transactional
    fun registerClient(clientRegisterRequest: ClientRegisterRequest): WebResponse {
        val res = WebResponse()
        val clientId = clientRegisterRequest.clientId!!
        val email = clientRegisterRequest.email!!
        // Check if clientId or clientEmail already exist
        val existingClient = flClientRepository.findByClientIdOrClientEmail(
            clientId, email
        )

        if (existingClient != null) {
            res.success = false
            res.message = "Client already registered"
            return res
        }
        val now = AppUtils.getCurrent()
        // Create new FlClient
        val newClient = FlClient(
            clientId = clientId,
            clientName = clientRegisterRequest.clientName!!,
            clientEmail = email,
            registeredAt = now,
            consentRecord = clientRegisterRequest.consentRecord,
            complianceStatus = clientRegisterRequest.complianceStatus,
            status = AppConstants.STATUS_ACTIVE
        )
        flClientRepository.save(newClient)

        res.success = true
        res.message = "Client successfully registered"
        return res
    }
}