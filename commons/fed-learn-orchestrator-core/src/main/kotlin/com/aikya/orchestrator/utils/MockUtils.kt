package com.aikya.orchestrator.utils

import com.aikya.orchestrator.dto.auth.SigninResponse
import com.aikya.orchestrator.dto.auth.User
import com.aikya.orchestrator.dto.auth.UserResponse
import com.fasterxml.jackson.databind.ObjectMapper
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import com.fasterxml.jackson.core.type.TypeReference

object MockUtils {
    private val objectMapper = ObjectMapper()

    fun memSignin(node: String, username: String, password: String, apiUrl: String): Mono<SigninResponse> {
        val mockResponse = SigninResponse().apply {
            statusCode = 200
            message = "Sign-in successful"
            accessToken = generateAccessToken(username)
            tokenType = "Bearer"
            refreshToken = generateRefreshToken()
            idToken = generateIdToken(
                username = username,
                email = "$username",
                name = username,
                company = node.uppercase() // e.g., "BANK1"
            )
            expiresIn = 3600
        }
        return Mono.just(mockResponse)
    }
    private fun encodeBase64(data: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data.toByteArray())

    private fun jwt(header: String, payload: String): String {
        val encodedHeader = encodeBase64(header)
        val encodedPayload = encodeBase64(payload)
        val dummySignature = UUID.randomUUID().toString().replace("-", "")
        return "$encodedHeader.$encodedPayload.$dummySignature"
    }

    fun generateAccessToken(username: String): String {
        val header = """{"alg":"RS256","typ":"JWT"}"""
        val payload = """
        {
          "sub": "$username",
          "username": "$username",
          "scope": "aws.cognito.signin.user.admin",
          "cognito:groups": ["ROLE_ADMIN", "ROLE_EVALUATOR"],
          "iat": ${Instant.now().epochSecond},
          "exp": 4070908800
        }
    """.trimIndent()
        return jwt(header, payload)
    }

    fun generateIdToken(username: String, email: String, name: String, company: String): String {
        val header = """{"alg":"RS256","typ":"JWT"}"""
        val payload = """
        {
          "email": "$email",
          "name": "$name",
          "username": "$username",
          "aud": "mock-client-id",
          "iat": ${Instant.now().epochSecond},
          "exp": 4070908800,
          "email_verified": true,
          "custom:company": "$company",
          "cognito:groups": ["ROLE_ADMIN", "ROLE_EVALUATOR"]
        }
    """.trimIndent()
        return jwt(header, payload)
    }

    fun generateRefreshToken(): String {
        val header = """{"alg":"RS256","enc":"A256GCM"}"""
        val payload = """{"refresh": "${UUID.randomUUID()}"}"""
        return jwt(header, payload)
    }
    fun getMemUser(idToken: String): Mono<UserResponse> {
        val userDefaultResponse = UserResponse().apply {
            success = false
            statusCode = 403
            message = "Invalid token"
        }

        return try {
            val parts = idToken.split(".")
            if (parts.size < 2) return Mono.just(userDefaultResponse)

            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val claims: Map<String, Any> = objectMapper.readValue(payloadJson, object : TypeReference<Map<String, Any>>() {})

            val name = claims["name"] as? String ?: ""
            val email = claims["email"] as? String ?: ""
            val company = (claims["custom:company"] as? String)?.takeIf { it.isNotBlank() } ?: "BANK"
            val roles = (claims["cognito:groups"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

            val userResponse = UserResponse().apply {
                success = true
                statusCode = 200
                message = "Mock user loaded successfully"
                user = User(
                    name = name,
                    email = email,
                    company = company,
                    roles = roles
                )
            }

            Mono.just(userResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            Mono.just(userDefaultResponse)
        }
    }
}