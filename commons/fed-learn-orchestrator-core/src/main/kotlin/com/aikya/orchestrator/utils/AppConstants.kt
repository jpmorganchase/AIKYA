package com.aikya.orchestrator.utils

sealed class FlowEvent(
    open val step: Int,
    open val event: String,
    open val description: String,
    open val label: String,
    open val source: String,
    open val target: String,
    open val tier: String,
    open val isLastStep: Boolean
)

sealed class UIFlowEvent(
    override val step: Int,
    override val event: String,
    override val description: String,
    override val label: String,
    override val source: String,
    override val target: String,
    override val tier: String,
    override val isLastStep: Boolean,
    val flowEvents: Map<Int, FlowEvent> // map containing a list of FlowEvents with their corresponding step
) : FlowEvent(step, event, description, label, source, target, tier, isLastStep)

object AppConstants {

    const val FLOW_DATA_PROCESSOR = "DATA-PROCESSOR"
    const val FLOW_FED_LEARN_ORCHESTRATOR_CLIENT = "FED-LEARN-ORCHESTRATOR-CLIENT"
    const val FLOW_FED_LEARN_AGENT_CLIENT = "FED-LEARN-AGENT-CLIENT"
    const val FLOW_FED_LEARN_ORCHESTRATOR_SERVER = "FED-LEARN-ORCHESTRATOR-SERVER"
    const val FLOW_FED_LEARN_MODEL_AGGREGATOR = "FED-LEARN-MODEL-AGGREGATOR"
    const val FLOW_NONE = "NONE"
    const val CLIENT = "CLIENT"
    const val SERVER = "SERVER"
    const val BLOCKCHAIN = "BLOCKCHAIN"
    //http/nats
    object Flow_Client_1 : FlowEvent(1, "OFL-C1", "Initiate Data load", "Load Transactions", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_DATA_PROCESSOR, CLIENT, false)
    object Flow_Client_2 : FlowEvent(2, "OFL-C2", "Request for Prediction", "Predict", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_AGENT_CLIENT, CLIENT, false)
    object Flow_Client_3 : FlowEvent(3, "OFL-C3", "User Feedback", "Sent Feedback", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_AGENT_CLIENT, CLIENT, false)
    object Flow_Client_4 : FlowEvent(4, "OFL-C4", "Local Train Model", "Train Model", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_AGENT_CLIENT, CLIENT, false)
    //blockchain client
    object Flow_Client_5 : FlowEvent(5, "OFL-C5", "Sent Local Model Evaluation", "Share Local Aggregate Model", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_ORCHESTRATOR_SERVER, BLOCKCHAIN, false)
    object Flow_Client_6 : FlowEvent(6, "OFL-C6", "Global Aggregate Model", "Share Insights", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_ORCHESTRATOR_SERVER, SERVER, false)
    object Flow_Client_7 : FlowEvent(7, "OFL-C7", "Receive Global Aggregated Model", "Receive Global Model", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, BLOCKCHAIN, false)
    object Flow_Client_8 : FlowEvent(8, "OFL-C8", "Update Global Aggregated Model", "Update Global Model", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, CLIENT, true)


    object Flow_Server_1 : FlowEvent(1, "OFL-S1", "Receive Client Aggregation Result", "Data LoadReceive Client Aggregation Result", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_ORCHESTRATOR_SERVER, BLOCKCHAIN, false)
    object Flow_Server_2 : FlowEvent(2, "OFL-S2", "Model Aggregation", "Model Aggregation", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_MODEL_AGGREGATOR, SERVER, false)
    object Flow_Server_3 : FlowEvent(3, "OFL-S3", "Update Global Aggregated Model", "Update Global Model", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_ORCHESTRATOR_SERVER, SERVER, false)
    object Flow_Server_4 : FlowEvent(4, "OFL-S4", "Share Global Aggregated Model", "Share Global Aggregated Model", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, BLOCKCHAIN, true)

    // Client_UI_Flow_1: Based on Flow_Client_1
    object Client_UI_Flow_1 : UIFlowEvent(
        step = 1,
        event = "OFL-CU1",
        description = "Initiate Data load",
        label = "Load Transactions",
        source = FLOW_FED_LEARN_ORCHESTRATOR_CLIENT,
        target = FLOW_DATA_PROCESSOR,
        tier = CLIENT,
        isLastStep = false,
        flowEvents = mapOf(
            1 to Flow_Client_1
        )
    )

    // Client_UI_Flow_2: Based on Flow_Client_2
    object Client_UI_Flow_2 : UIFlowEvent(
        step = 2,
        event = "OFL-CU2",
        description = "Request for Prediction",
        label = "Predict",
        source = FLOW_FED_LEARN_ORCHESTRATOR_CLIENT,
        target = FLOW_FED_LEARN_AGENT_CLIENT,
        tier = CLIENT,
        isLastStep = false,
        flowEvents = mapOf(
            2 to Flow_Client_2
        )
    )

    // Client_UI_Flow_3: Combination of Flow_Client_3 (step 3), Flow_Client_6 (step 6), and Flow_Client_7 (step 7)
    object Client_UI_Flow_3 : UIFlowEvent(
        step = 3,
        event = "OFL-CU3",
        description = "Request for Aggregate",
        label = "Share Insights",
        source = FLOW_FED_LEARN_ORCHESTRATOR_CLIENT,
        target = FLOW_FED_LEARN_ORCHESTRATOR_SERVER,
        tier = BLOCKCHAIN,
        isLastStep = false,
        flowEvents = mapOf(
            3 to Flow_Client_3,
            4 to Flow_Client_4,
            5 to Flow_Client_5,
            6 to Flow_Client_6,
            7 to Flow_Client_7
        )
    )

    // Client_UI_Flow_4: Based on Flow_Client_8
    object Client_UI_Flow_4 : UIFlowEvent(
        step = 4,
        event = "OFL-CU4",
        description = "Receive and Update Global Aggregated Model",
        label = "Receive & Update Model",
        source = FLOW_FED_LEARN_ORCHESTRATOR_CLIENT,
        target = FLOW_FED_LEARN_AGENT_CLIENT,
        tier = CLIENT,
        isLastStep = true,
        flowEvents = mapOf(
            8 to Flow_Client_8
        )
    )

    object Flow_00_000 : FlowEvent(-100, "OFL-000", "None", "None", FLOW_NONE, FLOW_NONE, FLOW_NONE, true)
    object Flow_100_10 : FlowEvent(100, "OFL-1000", "Model Publish", "Model Publish", FLOW_FED_LEARN_ORCHESTRATOR_SERVER, FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, BLOCKCHAIN, false)
    object Flow_200_20 : FlowEvent(200, "OFL-2000", "Model Client Registration", "Model Client Registration", FLOW_FED_LEARN_ORCHESTRATOR_CLIENT, FLOW_FED_LEARN_ORCHESTRATOR_SERVER, BLOCKCHAIN, true)

    val allClientEventsFlow = listOf(Flow_Client_1, Flow_Client_2, Flow_Client_3, Flow_Client_4, Flow_Client_5, Flow_Client_6, Flow_Client_7, Flow_Client_8)
    val allServerEventsFlow = listOf(Flow_Server_1, Flow_Server_2, Flow_Server_3, Flow_Server_4)

    val allClientEventsUIFlow = listOf(Client_UI_Flow_1, Client_UI_Flow_2, Client_UI_Flow_3, Client_UI_Flow_4)

    const val PENDING = "Pending"
    const val FAIL = "Fail"
    const val COMPLETE = "Complete"
    const val INITIAL = "Initial"
    const val FINAL = "Final"
    const val RESET = "Reset"

    const val WORKFLOW_TRACE_ID_NONE = "0000000000000000000000000000"

    const val DOMAIN_PAYMENT = "payment"
    const val DOMAIN_CREDIT_CARD = "credit_card_fraud"

    const val STATUS_ACTIVE = "active"
    const val STATUS_INACTIVE = "inactive"

    const val TRAINING = "training"
    const val PREDICT = "predict"

    enum class RunModeEnum(val type: String) {
        WORKFLOW("workflow"),
        WEIGHT("version");
        companion object {
            fun fromString(type: String): RunModeEnum {
                return RunModeEnum.values().firstOrNull { it.name == type }
                    ?: throw IllegalArgumentException("Invalid client run mode: $type")
            }
        }
    }
    enum class RunModeOptionsEnum(val mode: String) {
        MANUAL("manual"),
        AUTO("auto");
        companion object {
            fun fromString(mode: String): RunModeOptionsEnum {
                return values().firstOrNull { it.mode == mode }
                    ?: throw IllegalArgumentException("Invalid workflow mode: $mode")
            }
        }
    }

    const val ANSI_RESET = "\u001B[0m"
    const val ANSI_RED = "\u001B[31m"
    const val ANSI_GREEN = "\u001B[32m"
    const val ANSI_PINK = "\u001B[38;5;205m"
    const val ANSI_ORANGE = "\u001B[38;5;214m"
    const val ANSI_CYAN = "\u001B[36m"
    const val ANSI_PURPLE = "\u001B[35m"
    const val TRACE_ID = "TRACE-ID"
}