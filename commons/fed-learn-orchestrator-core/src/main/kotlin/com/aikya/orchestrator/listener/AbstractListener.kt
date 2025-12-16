package com.aikya.orchestrator.listener

import com.aikya.orchestrator.utils.MessageBuilder
import io.nats.client.Connection
import io.nats.client.Message
import io.nats.client.Nats
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.nio.charset.StandardCharsets

/**
 * AbstractListener is an abstract base class for handling messages from a NATS server.
 * It sets up a connection to the NATS server and provides an abstract method for processing messages.
 */
abstract class AbstractListener<T> {
    protected val logger: Logger = LoggerFactory.getLogger(AbstractListener::class.java)
    /**
     * NATS server URL, injected from the application properties.
     */
    @Value("\${nats.server}")
    val natsServer = "nats://localhost:4222"
    /**
     * NATS connection object.
     */
    private var nc: Connection? = null
    /**
     * Initializes the NATS connection after the bean's construction.
     *
     * @throws Exception if the connection to the NATS server fails.
     */
    @PostConstruct
    @Throws(Exception::class)
    private fun postConstruct() {
       nc = Nats.connect(natsServer)
    }

    /**
     * Abstract property representing the NATS subject (topic) to subscribe to.
     */
    protected abstract val subject: String

    /**
     * Abstract property representing the NATS subject (topic) to subscribe to.
     */
    protected abstract fun processMessage(obj: com.aikya.orchestrator.dto.message.Message)

    /**
     * Method to receive messages from the NATS subject.
     * Sets up a dispatcher to handle incoming messages and logs any errors.
     */
    fun receive() {
        try {
            val dispatcher = nc!!.createDispatcher { msg: Message ->
                val response = String(msg.data, StandardCharsets.UTF_8)
                try {
                    val message = MessageBuilder.jsonToObject(response, com.aikya.orchestrator.dto.message.Message::class.java)
                    val eventType = message.header?.event_type
                    val workflowTraceId = message.header?.workflow_trace_id
                    if (eventType != null && workflowTraceId != null) {
                        logger.info("--- received message, eventType: {}, workflowTraceId: {} ---", eventType, workflowTraceId)
                        // Process the message (implementation to be provided in subclasses)
                        processMessage(message)
                    } else {
                        logger.error("Missing required fields in the message header")
                    }
                } catch (e: Exception) {
                    logger.error("Nats message error, response: {}, error: {}", response, e)
                }
            }
            dispatcher.subscribe(subject)
        } catch (e: Exception) {
            logger.error("Nats receive error: {}", e)
        }
    }
}