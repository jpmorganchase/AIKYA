package com.aikya.orchestrator.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.type.TypeFactory

object JsonUtils {

    fun convertToJson(`object`: Any): String {
        val ow: ObjectWriter = ObjectMapper().writer()
        return try {
            ow.writeValueAsString(`object`)
        } catch (e: JsonProcessingException) {
            throw Exception(e)
        }
    }

    fun <T> readJsonValue(`object`: String, cls: Class<T>): T {
        return try {
            ObjectMapper().readValue(`object`, cls)
        } catch (e: JsonProcessingException) {
            throw Exception(e)
        }
    }

    fun <T> convertRawObject(`object`: Any, cls: Class<T>): T {
        return readJsonValue(convertToJson(`object`), cls)
    }

    fun <T> readJsonAsList(`object`: String, cls: Class<T>): List<T> {
        return try {
            val objectMapper = ObjectMapper()
            val typeFactory: TypeFactory = objectMapper.typeFactory
            objectMapper.readValue(`object`, typeFactory.constructCollectionType(List::class.java, cls))
        } catch (e: JsonProcessingException) {
            throw Exception(e)
        }
    }
}