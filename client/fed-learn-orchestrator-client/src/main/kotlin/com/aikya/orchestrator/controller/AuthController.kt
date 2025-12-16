package com.aikya.orchestrator.controller


import com.aikya.orchestrator.dto.auth.*
import com.aikya.orchestrator.service.UrlService
import com.aikya.orchestrator.service.auth.AuthService

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * flow controller api for UI
 *
 * @author bwu
 */
@RestController
class AuthController(
    val authService: AuthService,
    private val urlService: UrlService
) {
    private val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)
    /**
     * Handles user sign-in requests.
     * This endpoint proxies the sign-in request to the appropriate authentication node.
     *
     * @param node The authentication node or realm to sign into.
     * @param signinForm The request body containing the user's username and password.
     * @return A reactive `Mono` that will emit a `SigninResponse` containing authentication tokens upon successful sign-in.
     */
    @RequestMapping(value = ["/signin/{node}"], method = arrayOf(RequestMethod.POST))
    fun signin(@PathVariable(value = "node") node: String, @RequestBody signinForm: SigninForm): Mono<SigninResponse> {
        logger.info("user - {} signin in ({}) node", signinForm.username!!, node);
        val signinUrl = urlService.getAuthUrl("signin")
        val apiUrl = signinUrl.replace("{node}", node)
        return authService.signin(node, signinForm.username!!, signinForm.password!!, apiUrl);
    }
    /**
     * Handles new user registration requests.
     * This endpoint proxies the signup request to the specified authentication node.
     *
     * @param node The authentication node or realm where the user will be registered.
     * @param signupForm The request body containing the new user's details (email, password, name).
     * @return A reactive `Mono` that will emit a `SignupResponse` indicating the result of the registration attempt.
     */
    @RequestMapping(value = ["/signup/{node}"], method = arrayOf(RequestMethod.POST))
    fun signup(@PathVariable(value = "node") node: String, @RequestBody signupForm: SignupForm): Mono<SignupResponse> {
        logger.info("user - {} signup in ({}) node", signupForm.email!!, node);
        val signupUrl = urlService.getAuthUrl("signup")
        val apiUrl = signupUrl.replace("{node}", node)
        return authService.signup(node, signupForm.email!!, signupForm.password!!, signupForm.name!!, apiUrl);
    }
    /**
     * Retrieves user details based on a provided authentication ID token.
     *
     * @param userForm The request body containing the user's `idToken`.
     * @return A reactive `Mono` that will emit a `UserResponse` with the user's profile information.
     */
    @RequestMapping(value = ["/getuser"], method = arrayOf(RequestMethod.POST))
    fun getuser(@RequestBody userForm: UserForm): Mono<UserResponse> {
        return authService.getuser(userForm.idToken!!);
    }
}