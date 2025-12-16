package com.aikya.orchestrator.service.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
/**
 * CallerService provides a generic utility layer to make HTTP requests (GET, POST, DELETE)
 * using a custom-configured [WebClient].
 *
 * This service is mainly used for internal microservice-to-microservice calls,
 * supporting JSON payloads and simple health checks.
 */
@Service
class CallerService {
    private val logger: Logger = LoggerFactory.getLogger(CallerService::class.java)

    @Autowired
    private lateinit var customWebClient: WebClient
    /**
     * Makes a synchronous GET request to the specified URL and maps the response to the given class type.
     *
     * @param url the target endpoint.
     * @param clazz the response type to deserialize into.
     * @return the deserialized object of type [T].
     */
    fun <T : Any> get(url: String, clazz: Class<T>): T {
        return customWebClient.get()
            .uri(url)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(clazz) // Use the class passed as parameter for deserialization
            .block()!! // Blocking call to get the result synchronously, consider handling this non-blocking
    }
    /**
     * Performs a simple health check by issuing a GET request to the provided URL.
     *
     * @param name a friendly name for logging purposes.
     * @param url the target service health endpoint.
     * @return `true` if the service returns a 2xx status code, `false` otherwise.
     */
    fun checkDependencyServiceHealth(name: String, url: String): Boolean {
        return try {
            logger.info("------- calling url for service $name at $url ------")
            val response = customWebClient.get()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .block()
            response?.statusCode?.is2xxSuccessful == true
        } catch (e: Exception) {
            logger.error("Error checking health for service $name at $url: ${e.message}")
            false
        }
    }
    /**
     * Makes a POST request with a request body and expects a typed response.
     *
     * @param url the target endpoint.
     * @param requestBody the request payload.
     * @param responseClass the expected response type.
     * @return the response deserialized to type [R].
     */
    fun <T : Any, R : Any> post(url: String, requestBody: T, responseClass: Class<R>): R {
        return customWebClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseClass)
            .block()!! // Blocking the Mono to directly return R, note the use of !! to assert non-null
    }
    /**
     * Makes a POST request over HTTPS with a body and expects a typed response.
     *
     * @param url the target HTTPS endpoint.
     * @param requestBody the request body to send.
     * @param responseClass the expected response type.
     * @return the response mapped to [R].
     */
    fun <T : Any, R : Any> httpsPost(url: String, requestBody: T, responseClass: Class<R>): R {
        return customWebClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(responseClass)
            .block()!! // Blocking the Mono to directly return R, note the use of !! to assert non-null
    }
    /**
     * Makes a POST request over HTTPS without any body and expects a response.
     *
     * @param url the HTTPS endpoint to call.
     * @param responseClass the response type to deserialize.
     * @return an instance of [R] representing the response.
     */
    fun <R : Any> httpsPost(url: String, responseClass: Class<R>): R {
        return customWebClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(responseClass)
            .block()!! // Blocking the Mono to directly return R, note the use of !! to assert non-null
    }
    /**
     * Sends a DELETE request to the given URL.
     *
     * @param url the target endpoint to delete from.
     * @return `true` if successful, otherwise `false`.
     */
    fun delete(url: String): Boolean {
        return try {
            customWebClient.delete()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .block()
            true
        } catch (e: Exception) {
            logger.error("Error performing DELETE request to $url: ${e.message}")
            false
        }
    }
}