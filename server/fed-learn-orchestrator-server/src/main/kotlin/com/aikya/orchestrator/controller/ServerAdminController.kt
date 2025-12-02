package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.common.WebResponse
import com.aikya.orchestrator.dto.fedlearn.ClientRegisterRequest
import com.aikya.orchestrator.service.ServerAdminService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for handling server-side administrative operations related to client registration.
 *
 * All endpoints are prefixed with `/server-auth`.
 */
@RequestMapping(value = ["/server-auth"])
@RestController
class ServerAdminController (private val serverAdminService: ServerAdminService){
    private val logger: Logger = LoggerFactory.getLogger(ServerAdminController::class.java)
    /**
     * Registers a client with the server. This is typically called when a client node (e.g., bank node)
     * starts up and wants to register itself with the orchestrator or central service.
     *
     * @param clientRegisterRequest The request payload containing client metadata such as name, ID, and email.
     * @return A [Mono] emitting a [WebResponse] indicating whether the registration was successful.
     */
    @RequestMapping(value = ["/client-register"], method = arrayOf(RequestMethod.POST))
    fun registerClient(@RequestBody clientRegisterRequest: ClientRegisterRequest): Mono<WebResponse> {
        logger.info("register client: clientName: {}, clientId: {}", clientRegisterRequest.clientName, clientRegisterRequest.clientId);
        val webRes = serverAdminService.registerClient(clientRegisterRequest)
        return Mono.just(webRes)
    }
}