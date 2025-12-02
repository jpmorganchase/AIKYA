package com.aikya.orchestrator.service

import com.aikya.orchestrator.agent.model.ClientRunModeEntity
import com.aikya.orchestrator.agent.repository.WorkflowRunModeDetailRepository
import com.aikya.orchestrator.agent.repository.ClientRunModeRepository
import com.aikya.orchestrator.client.model.fedlearn.WorkflowModelLogsEntity
import com.aikya.orchestrator.client.repository.model.WorkflowModelLogsRepository
import com.aikya.orchestrator.utils.AppConstants
import com.aikya.orchestrator.utils.AppConstants.COMPLETE
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_2
import com.aikya.orchestrator.utils.AppConstants.Flow_Client_4
import com.aikya.orchestrator.utils.AppConstants.INITIAL
import com.aikya.orchestrator.utils.AppConstants.PREDICT
import com.aikya.orchestrator.utils.AppConstants.TRAINING
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
/**
 * Service for managing logs related to specific workflow events and handling the
 * run modes (e.g., AUTO/MANUAL) for different processes.
 *
 * This class provides an abstraction layer over the repositories for `WorkflowModelLogsEntity`
 * and `ClientRunModeEntity`, offering methods to query for logs in specific states and to
 * manage the automatic or manual execution settings for workflows and model weight updates.
 *
 * @param workflowModelLogsRepository Repository for `WorkflowModelLogsEntity`.
 * @param workflowRunModeDetailRepository Repository for `WorkflowRunModeDetailEntity`.
 * @param clientRunModeRepository Repository for `ClientRunModeEntity`.
 */
@Transactional
@Service
class WorkflowModelLogsService(
    private val workflowModelLogsRepository: WorkflowModelLogsRepository,
    private val workflowRunModeDetailRepository: WorkflowRunModeDetailRepository,
    private val clientRunModeRepository: ClientRunModeRepository
) {
    /**
     * Retrieves all workflow logs that are at the initial prediction step (`Flow_Client_2`)
     * and have a status of `INITIAL`.
     *
     * @return A list of `WorkflowModelLogsEntity`.
     */
    fun getInitialWorkflowModelLogs(): List<WorkflowModelLogsEntity> {
        return workflowModelLogsRepository.findWorkflowModelLogsByEventStatus(Flow_Client_2.event, INITIAL)
    }
    /**
     * Retrieves all workflow logs where the `PREDICT` event has been completed.
     *
     * @return A list of `WorkflowModelLogsEntity`.
     */
    fun getCompletedPredictWorkflowModelLogs(): List<WorkflowModelLogsEntity> {
        return workflowModelLogsRepository.findWorkflowModelLogsByEventStatus(PREDICT, COMPLETE)
    }
    /**
     * Retrieves all workflow logs where the `TRAINING` event has been completed.
     *
     * @return A list of `WorkflowModelLogsEntity`.
     */
    fun getCompletedTrainingWorkflowModelLogs(): List<WorkflowModelLogsEntity> {
        return workflowModelLogsRepository.findWorkflowModelLogsByEventStatus(TRAINING, COMPLETE)
    }
    /**
     * Saves or updates a `WorkflowModelLogsEntity`.
     *
     * @param workflowModelLogsEntity The entity to save.
     */
    @Transactional
    fun updateWorkflowModelLogs(workflowModelLogsEntity: WorkflowModelLogsEntity) {
        workflowModelLogsRepository.save(workflowModelLogsEntity)
    }
    /**
     * Finds a specific workflow log by its trace ID and the associated event type.
     *
     * @param workflowTraceId The unique trace ID of the workflow.
     * @param event The event type (e.g., "TRAINING", "PREDICT").
     * @return The corresponding `WorkflowModelLogsEntity`.
     */
    fun getWorkflowModelLogs(workflowTraceId: String, event: String): WorkflowModelLogsEntity {
        return workflowModelLogsRepository.findWorkflowModelLogsByWorkflowTraceId(workflowTraceId, event)
    }
    /**
     * Finds a specific workflow log by its trace ID and event, returning null if not found.
     *
     * @param workflowTraceId The unique trace ID of the workflow.
     * @param event The event type (e.g., "TRAINING", "PREDICT").
     * @return The `WorkflowModelLogsEntity` or `null` if it does not exist.
     */
    fun findOptionalWorkflowModelLogs(workflowTraceId: String, event: String): WorkflowModelLogsEntity? {
        val optionalLog = workflowModelLogsRepository.findOptionalWorkflowModelLogsByWorkflowTraceId(workflowTraceId, event)
        return optionalLog.orElse(null)
    }
    /**
     * Convenience method to get the log for the initial prediction step of a specific workflow.
     *
     * @param workflowTraceId The unique trace ID of the workflow.
     * @return The `WorkflowModelLogsEntity` for the initial prediction step.
     */
    fun getInitialWorkflowModelLogs(workflowTraceId: String): WorkflowModelLogsEntity {
        return getWorkflowModelLogs(workflowTraceId, Flow_Client_2.event)
    }
    /**
     * Convenience method to get the log for the training step of a specific workflow.
     *
     * @param workflowTraceId The unique trace ID of the workflow.
     * @return The `WorkflowModelLogsEntity` for the training step.
     */
    fun getTrainingWorkflowModelLogs(workflowTraceId: String): WorkflowModelLogsEntity {
        return getWorkflowModelLogs(workflowTraceId, Flow_Client_4.event)
    }
    /**
     * Retrieves the run mode entity (which contains AUTO/MANUAL status) for the general
     * workflow process of a given domain.
     *
     * @param domainType The name of the domain.
     * @return The `ClientRunModeEntity` for the workflow process.
     */
    fun getWorkflowRunModel(domainType: String): ClientRunModeEntity {
        return clientRunModeRepository.findByDomainAndName(domainType, AppConstants.RunModeEnum.WORKFLOW.name)
    }
    /**
     * Retrieves the run mode entity (which contains AUTO/MANUAL status) for the model weight
     * update process of a given domain.
     *
     * @param domainType The name of the domain.
     * @return The `ClientRunModeEntity` for the weight update process.
     */
    fun getWeightRunModel(domainType: String): ClientRunModeEntity {
        return clientRunModeRepository.findByDomainAndName(domainType, AppConstants.RunModeEnum.WEIGHT.type)
    }
    /**
     * Fetches a `ClientRunModeEntity` by its primary key.
     *
     * @param modeId The ID of the entity.
     * @return The found `ClientRunModeEntity`.
     * @throws EntityNotFoundException if no entity with the given ID is found.
     */
    fun getClientRunModelById(modeId: Long): ClientRunModeEntity {
        return clientRunModeRepository.findById(modeId).orElseThrow {
            EntityNotFoundException("WorkflowRunMode with id $modeId not found")
        }
    }

    /**
     * Saves or updates a `ClientRunModeEntity`.
     *
     * @param clientRunModeEntity The entity to save.
     * @return The saved entity.
     */
    @Transactional
    fun save(clientRunModeEntity: ClientRunModeEntity) : ClientRunModeEntity{
        clientRunModeRepository.save(clientRunModeEntity)
        return clientRunModeEntity
    }
    /**
     * Checks if the general workflow process for the given domain is set to AUTO run mode.
     *
     * @param domain The name of the domain.
     * @return `true` if the mode is AUTO, `false` otherwise.
     */
    fun isWorkflowAutoRunModel(domain: String): Boolean {
        return clientRunModeRepository.isAutoRunModel(domain, AppConstants.RunModeEnum.WORKFLOW.name)
    }
    /**
     * Checks if the model weight update process for the given domain is set to AUTO run mode.
     *
     * @param domain The name of the domain.
     * @return `true` if the mode is AUTO, `false` otherwise.
     */
    fun isWeightAutoRunModel(domain: String): Boolean {
        return clientRunModeRepository.isAutoRunModel(domain, AppConstants.RunModeEnum.WEIGHT.type)
    }
}