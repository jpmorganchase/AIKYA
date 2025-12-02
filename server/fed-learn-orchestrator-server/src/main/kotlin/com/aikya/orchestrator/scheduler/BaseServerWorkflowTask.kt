package com.aikya.orchestrator.scheduler

import com.aikya.orchestrator.service.ModelService
import com.aikya.orchestrator.service.OrchestrationServerCallService
import com.aikya.orchestrator.service.OrchestrationServerService
import com.aikya.orchestrator.service.protocol.ProtocolDispatcherService
import com.aikya.orchestrator.service.workflow.WorkflowService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

abstract class BaseServerWorkflowTask {
    @Autowired
    protected lateinit var workflowService: WorkflowService
    @Autowired
    protected lateinit var modelService: ModelService
    @Autowired
    protected lateinit var protocolDispatcherService: ProtocolDispatcherService
    @Autowired
    protected lateinit var orchestrationServerCallService: OrchestrationServerCallService

    @Autowired
    protected lateinit var orchestrationServerService: OrchestrationServerService

    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
}