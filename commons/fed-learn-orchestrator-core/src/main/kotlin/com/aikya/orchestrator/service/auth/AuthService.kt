package com.aikya.orchestrator.service.auth

import com.aikya.orchestrator.dto.auth.SigninResponse
import com.aikya.orchestrator.dto.auth.SignupResponse
import com.aikya.orchestrator.dto.auth.User
import com.aikya.orchestrator.dto.auth.UserResponse
import com.aikya.orchestrator.shared.model.user.UserEntity
import com.aikya.orchestrator.shared.repository.user.UserRepository
import com.aikya.orchestrator.utils.MockUtils
import io.jsonwebtoken.Jwts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

/**
 * AuthService handles authentication and user management.
 * It supports both local mock-based and cloud-based authentication flows.
 *
 * Features:
 * - Sign-in with cloud or local memory-based logic.
 * - Sign-up and auto-registration of users into the database.
 * - Token-based user extraction and validation.
 *
 * Configuration:
 * - Controlled via `app.use-cloud-auth` flag.
 * - Uses `WebClient` to communicate with cloud endpoints.
 */
@Service
@Transactional
class AuthService (private val userRepository: UserRepository) {
    private val logger: Logger = LoggerFactory.getLogger(AuthService::class.java)
    @Autowired
    private lateinit var webClient: WebClient
    @Value("\${app.use-cloud-auth}")
    private val useCloudAuth =false
    /**
     * Signs in a user using the specified method (cloud or memory).
     *
     * @param node the node name for client identification.
     * @param username user's login identifier.
     * @param password user's password.
     * @param apiUrl authentication API endpoint.
     * @return a [Mono] of [SigninResponse] indicating success/failure and token.
     */
    @Transactional
    fun signin(node: String, username: String, password: String, apiUrl: String): Mono<SigninResponse> {
        return if (useCloudAuth) {
            Mono.fromCallable {
                cloudSignin(node, username, password, apiUrl)
            }.subscribeOn(Schedulers.boundedElastic())
        } else {
            MockUtils.memSignin(node, username, password, apiUrl)
        }
    }
    /**
     * Signs in a user using the specified method (cloud or memory).
     *
     * @param node the node name for client identification.
     * @param username user's login identifier.
     * @param password user's password.
     * @param apiUrl authentication API endpoint.
     * @return a [Mono] of [SigninResponse] indicating success/failure and token.
     */
    @Transactional
    fun cloudSignin(node: String, username: String, password: String, apiUrl: String): SigninResponse {
        val requestBody = mapOf("username" to username, "password" to password)
        logger.info("user: $username, sign-in url: $apiUrl")

        val signinResponse = webClient
            .post()
            .uri(apiUrl)
            .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .bodyToMono(SigninResponse::class.java)
            .block() ?: throw IllegalStateException("Signin response was null")

        val existingUser = userRepository.findByEmail(username)

        if (existingUser == null && signinResponse.idToken != null) {
            val userResponse = getUserResponse(signinResponse.idToken!!)

            if (userResponse.success == true && userResponse.user != null) {
                logger.info(
                    "User logged in - name: {}, email: {}, roles: {}",
                    userResponse.user!!.name,
                    userResponse.user!!.email,
                    userResponse.user!!.roles
                )

                val user = userResponse.user!!
                val newUser = UserEntity(
                    name = user.name,
                    email = user.email,
                    nickname = user.name,
                    status = "active"
                )

                userRepository.save(newUser)
                logger.info("New user saved - email: {}", newUser.email)
            }
        }

        return signinResponse
    }
    /**
     * Registers a new user to the system and optionally persists them.
     *
     * @param nodeInput client node name.
     * @param email user email.
     * @param password user password.
     * @param name user full name.
     * @param apiUrl cloud registration endpoint.
     * @return a [Mono] of [SignupResponse] indicating status.
     */
    @Transactional
    fun signup(nodeInput: String, email: String, password: String, name: String, apiUrl: String): Mono<SignupResponse> {
        val node = nodeInput.lowercase()
        val company = when (node) {
            "bank1", "jpm" -> "Bank 1"
            "bank2", "citi" -> "Bank 2"
            "bank3", "dbs" -> "Bank 3"
            "network" -> "Network"
            else -> ""
        }

        val requestBody = mapOf(
            "email" to email,
            "password" to password,
            "name" to name,
            "company" to company,
            "defaultGroup" to "ROLE_EVALUATOR"
        )

        return webClient.post()
            .uri(apiUrl)
            .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .bodyToMono(SignupResponse::class.java)
            .flatMap { signupResponse ->
                // Check if the signup was successful (status code in 2xx range)
                if (signupResponse.statusCode in 200..299) {
                    logger.info("Signup successful for email: $email, company: $company")
                    // Check if the user already exists
                    Mono.fromCallable {
                        val existingUser = userRepository.findByEmail(email)
                        if (existingUser == null) {
                            val newUser = UserEntity(
                                name = name,
                                email = email,
                                nickname = name,
                                status = "active"
                            )
                            userRepository.save(newUser)
                            logger.info("New user saved - email: {}", newUser.email)
                        } else {
                            logger.info("User already exists in the database - email: {}", existingUser.email)
                        }
                        signupResponse
                    }.subscribeOn(Schedulers.boundedElastic())
                } else {
                    logger.error("Signup failed for email: $email, reason: ${signupResponse.message}")
                    Mono.just(signupResponse)
                }

            }
    }
    /**
     * Retrieves the authenticated user based on the ID token.
     * Falls back to mock user if not using cloud auth.
     *
     * @param idToken JWT token string.
     * @return a [Mono] of [UserResponse] containing user profile.
     */
    fun getuser(idToken: String): Mono<UserResponse> {
        return if (useCloudAuth) {
            getCloudUser(idToken)
        } else {
            MockUtils.getMemUser(idToken)
        }
    }
    fun getCloudUser(idToken: String): Mono<UserResponse> {
        val userResponse =  getUserResponse(idToken)
        if(userResponse.success!!) {
            logger.info("user logged in - name: {}, email: {}, roles: {}", userResponse.user!!.name, userResponse.user!!.email, userResponse.user!!.roles)
        }
        return  Mono.just(userResponse);
    }
    fun getUserResponse(idToken: String): UserResponse {
        val userDefaultResponse = UserResponse()
        userDefaultResponse.success = false
        try {
            val i = idToken.lastIndexOf('.')
            val withoutSignature = idToken.substring(0, i + 1)
            val jwsClaims = Jwts.parser().parseClaimsJwt(withoutSignature)
            if (jwsClaims != null) {
                val claims = jwsClaims.body
                val name = claims["name"] as? String ?: ""
                val email =  claims["email"] as? String ?: ""
                val company = (claims["custom:company"] as? String)?.let {
                    when (it) {
                        "JPM" -> "Bank 1"
                        "CITI" -> "Bank 2"
                        "DBS" -> "Bank 3"
                        else -> it
                    }
                } ?: ""
                // Extracts the claims and retrieves the "azp" (authorized party) value as the client ID
                val userResponse = UserResponse().apply {
                    statusCode = 200
                    success = true
                    user = User(
                        company = company,
                        roles = (claims["cognito:groups"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        name = name,
                        email = email
                    )
                    message = "Success message" // Provide an appropriate success message
                }
                return userResponse
            }
        } catch(e: Exception) {
            logger.error("Invalid token")
            userDefaultResponse.message = "Invalid token"
            userDefaultResponse.statusCode = 403
        }
        return userDefaultResponse
    }

}