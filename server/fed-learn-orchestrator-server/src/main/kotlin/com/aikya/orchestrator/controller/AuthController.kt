package com.aikya.orchestrator.controller

import com.aikya.orchestrator.dto.auth.*
import com.aikya.orchestrator.service.auth.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * flow controller api for UI
 *
 * @author bwu
 */
@RestController
class AuthController (val authService: AuthService) {
    @Value("\${app.url.signin}")
    val signinUrl = ""
    @Value("\${app.url.signup}")
    val signupUrl = ""
    private val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)
    /**
     * Handles user sign-in for a specific node.
     *
     * @param node The node identifier (e.g., BANK1, BANK2) used to construct the API endpoint.
     * @param signinForm The sign-in form containing the user's username and password.
     * @return A [Mono] emitting a [SigninResponse] containing authentication information.
     */
    @RequestMapping(value = ["/signin/{node}"], method = arrayOf(RequestMethod.POST))
    fun signin(@PathVariable(value = "node") node: String, @RequestBody signinForm: SigninForm): Mono<SigninResponse> {
        logger.info("user - {} signin in ({}) node", signinForm.username!!, node);
        val apiUrl = signinUrl.replace("{node}", node)
        return authService.signin(node, signinForm.username!!, signinForm.password!!, apiUrl);
    }
    /**
     * Handles user sign-up for a specific node.
     *
     * @param node The node identifier used to construct the API endpoint.
     * @param signupForm The sign-up form containing the user's name, email, and password.
     * @return A [Mono] emitting a [SignupResponse] after successful registration.
     */
    @RequestMapping(value = ["/signup/{node}"], method = arrayOf(RequestMethod.POST))
    fun signup(@PathVariable(value = "node") node: String, @RequestBody signupForm: SignupForm): Mono<SignupResponse> {
        logger.info("user - {} signup in ({}) node", signupForm.email!!, node);
        val apiUrl = signupUrl.replace("{node}", node)
        return authService.signup(node, signupForm.email!!, signupForm.password!!, signupForm.name!!, apiUrl);
    }
    /**
     * Retrieves user information based on the provided ID token.
     *
     * @param userForm The form containing the ID token (e.g., JWT).
     * @return A [Mono] emitting a [UserResponse] containing user profile details.
     */
    @RequestMapping(value = ["/getuser"], method = arrayOf(RequestMethod.POST))
    fun getuser(@RequestBody userForm: UserForm): Mono<UserResponse> {
        return authService.getuser(userForm.idToken!!);
    }
}