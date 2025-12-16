package com.aikya.orchestrator.dto.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
data class User (
    val company: String,
    val roles: List<String>,
    val name: String,
    val email: String
)