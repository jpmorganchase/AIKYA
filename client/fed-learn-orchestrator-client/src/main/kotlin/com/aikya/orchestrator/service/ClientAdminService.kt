package com.aikya.orchestrator.service

import com.aikya.orchestrator.repository.agent.OrchestratorAgentQueryRepository
import com.aikya.orchestrator.repository.client.FLClientQueryRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit
/**
 * Administrative service for managing the client-side federated learning environment. 🛠️
 *
 * This service provides functionality to reset the client's state by truncating
 * relevant tables in both the `fedlearn_client` and `fedlearn_orchestrator_agent` databases.
 * It is primarily used for testing, development, or resetting a demonstration environment.
 */
@Service
class ClientAdminService() {

    private val logger: Logger = LoggerFactory.getLogger(ClientAdminService::class.java)

    @Autowired
    @Qualifier("clientQueryRepository")
    private lateinit var flClientQueryRepository: FLClientQueryRepository

    @Autowired
    @Qualifier("orchestratorAgentQueryRepository")
    private lateinit var agentQueryRepository: OrchestratorAgentQueryRepository
    /**
     * Resets the entire client-side environment by truncating all relevant tables
     * for a specific domain.
     *
     * @param domain The specific learning domain (e.g., "payment", "credit_card_fraud") which
     * determines which domain-specific data table to truncate.
     * @return A list of all table names that were successfully truncated.
     */
    @Transactional(readOnly = false)
    fun reset(domain: String): List<String> {
        val tables = mutableListOf<String>()
        val truncatedFedLearnClients = resetFedLearnClientTables(domain)
        tables.addAll(truncatedFedLearnClients)
        val truncatedAgentTables = resetOrchestratorAgentTables();
        tables.addAll(truncatedAgentTables)
        return tables
    }
    /**
     * Truncates tables within the `fedlearn_client` database schema.
     *
     * This method disables foreign key checks, truncates a predefined list of tables
     * plus a domain-specific table, and then re-enables foreign key checks. Each operation
     * runs in its own new transaction.
     *
     * @param domain The learning domain used to identify the specific domain data table to clear.
     * @return A list of the `fedlearn_client` table names that were truncated.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun resetFedLearnClientTables(domain: String): List<String> {
        val start = System.currentTimeMillis()
        val fedLearnClientTables = mutableListOf(
            "fedlearn_client.model_training_result",
            "fedlearn_client.metrics",
            "fedlearn_client.model_feedback",
            "fedlearn_client.model_predict_data",
            "fedlearn_client.data_seed",
            "fedlearn_client.workflow_model_logs",
            "fedlearn_client.model_client_records",
            "fedlearn_client.model_client_record_history"
        )
        when (domain) {
            "payment" -> fedLearnClientTables.add("fedlearn_client.domain_payment_data")
            "credit_card_fraud" -> fedLearnClientTables.add("fedlearn_client.domain_credit_card_fraud_data")
            "payment_fraud" -> fedLearnClientTables.add("fedlearn_client.domain_payment_fraud_data")
        }
        // Disable foreign key checks
        flClientQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
        for (table in fedLearnClientTables) {
            truncateFedLearnClientTable(table)
        }
        // Enable foreign key checks
        flClientQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
        logger.info(
            "--------  truncated all fedLearn Client tables ----------------> duration: {} sec",
            TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - start))
        )
        return fedLearnClientTables
    }
    /**
     * Truncates tables within the `fedlearn_orchestrator_agent` database schema.
     *
     * This method disables foreign key checks, truncates a predefined list of workflow tables,
     * and then re-enables foreign key checks. The operation runs in a new transaction.
     *
     * @return A list of the `fedlearn_orchestrator_agent` table names that were truncated.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun resetOrchestratorAgentTables(): List<String> {
        val start = System.currentTimeMillis()
        val orchestratorAgentTables = mutableListOf(
            "fedlearn_orchestrator_agent.workflow", "fedlearn_orchestrator_agent.workflow_detail"
        )
        // Disable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 0")
        for (table in orchestratorAgentTables) {
            truncateOrchestratorAgentTable(table)
        }
        // Enable foreign key checks
        agentQueryRepository.executeQuery("SET FOREIGN_KEY_CHECKS = 1")
        logger.info(
            "--------  truncated all Orchestrator Agent tables ----------------> duration: {} sec",
            TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - start))
        )
        return orchestratorAgentTables
    }
    /**
     * Executes a TRUNCATE command on a single table in the `fedlearn_client` database.
     * This operation runs in a new transaction.
     *
     * @param table The fully qualified name of the table to truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun truncateFedLearnClientTable(table: String) {
        logger.info("--------  truncating fedLearn Client table  {} ----------------", table)
        flClientQueryRepository.truncateTable(table)
    }
    /**
     * Executes a TRUNCATE command on a single table in the `fedlearn_orchestrator_agent` database.
     * This operation runs in a new transaction.
     *
     * @param table The fully qualified name of the table to truncate.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun truncateOrchestratorAgentTable(table: String) {
        logger.info("--------  truncating OrchestratorAgent table  {} ----------------", table)
        agentQueryRepository.truncateTable(table)
    }

}