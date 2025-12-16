package com.aikya.orchestrator.utils

import com.aikya.orchestrator.dto.message.Message
import com.aikya.orchestrator.dto.message.MessageBody
import com.aikya.orchestrator.dto.message.MessageHeader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.lang.reflect.Field
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
@SuppressWarnings("all")
class MessageBuilder {

    companion object {
        private val objectMapper = jacksonObjectMapper()
        @Suppress("ReplaceGetOrSet")
        fun buildHeader(
            timestamp: Long = System.currentTimeMillis(),
            encrypted: Boolean? = false,
            eventType: String? = null,
            workflowTraceId: String? = null,
            domain: String? = null,
            status: String? = null
        ): MessageHeader {
            return MessageHeader().apply {
                this.timestamp = timestamp
                this.encrypted = encrypted
                this.event_type = eventType
                this.workflow_trace_id = workflowTraceId
                this.domain = domain
                this.status= status
            }
        }

        fun <T> buildBody(items: List<T>): MessageBody {
            return MessageBody().apply {
                this.data = items.toList() // makes a defensive copy to ensure immutability
            }
        }
        fun <T> buildMessage(
            id: String,
            header: MessageHeader? = null,
            body: MessageBody? = null
        ): Message {
            return Message(id, header, body)
        }

        fun convertToJson(message: Message): String {
            return objectMapper.writeValueAsString(message)
        }

        fun <T> readJsonAsList(json: String, clazz: Class<T>): List<T> {
            val typeFactory = objectMapper.typeFactory
            val collectionType = typeFactory.constructCollectionType(List::class.java, clazz)
            return objectMapper.readValue(json, collectionType)
        }

        fun <T> objectToJson(obj: T): String {
            return objectMapper.writeValueAsString(obj)
        }

        fun <T> jsonToObject(jsonString: String, clazz: Class<T>): T {
            return objectMapper.readValue(jsonString, clazz)
        }

        // Function to dynamically set field values
        fun setField(obj: Any, fieldName: String, fieldValue: String) {
            try {
                val clazz: Class<*> = obj.javaClass
                var field: Field? = null

                var currentClass: Class<*>? = clazz
                while (currentClass != null) {
                    try {
                        field = currentClass.getDeclaredField(fieldName)
                        break
                    } catch (e: NoSuchFieldException) {
                        currentClass = currentClass.superclass
                    }
                }

                if (field == null) {
                    throw NoSuchFieldException("Field $fieldName not found on ${obj.javaClass}")
                }

                field.isAccessible = true
                val value: Any = when (field.type) {
                    Long::class.javaPrimitiveType, Long::class.javaObjectType -> fieldValue.toLong()
                    Int::class.javaPrimitiveType, Int::class.javaObjectType -> fieldValue.toInt()
                    Double::class.javaPrimitiveType, Double::class.javaObjectType -> fieldValue.toDouble()
                    Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> fieldValue.toBoolean()
                    else -> fieldValue
                }
                field.set(obj, value)
            } catch (e: Exception) {
                throw UnsupportedOperationException("Failure to set $fieldName", e)
            }
        }
        // Generic function to convert List<LinkedHashMap<String, Any>> to List<T>
        fun <T : Any> convertListOfLinkedHashMapToListOfObjects(
            list: List<Any?>,
            clazz: KClass<T>
        ): List<T> {
            // Convert the list of Any? to List<LinkedHashMap<String, Any>>
            val listOfLinkedHashMap: List<LinkedHashMap<String, Any>> = list.map { item ->
                if (item is LinkedHashMap<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    item as LinkedHashMap<String, Any>
                } else {
                    throw IllegalArgumentException("List contains non-LinkedHashMap item")
                }
            }

            // Convert List<LinkedHashMap<String, Any>> to List<T>
            return listOfLinkedHashMap.map { map ->
                val instance = clazz.createInstance()
                map.forEach { (key, value) ->
                    setField(instance, key, value.toString())
                }
                instance
            }
        }
    }
}