package com.aikya.orchestrator.shared.model.user

import jakarta.persistence.*

@Entity
@Table(name = "user")
class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    val id: Long = 0,

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "email", nullable = false, length = 100, unique = true)
    val email: String,

    @Column(name = "nickname", length = 50)
    val nickname: String? = null,

    @Column(name = "status", nullable = false, length = 10)
    val status: String
)