package com.aikya.orchestrator.shared.repository.user

import com.aikya.orchestrator.shared.model.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
}