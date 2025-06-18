package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int? = null,
    val email: String,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val role: String? = null,
    val createdAt: String,
    val passwordHash: String,
)

@Serializable
data class CreateUserRequest(
    val email: String,
    val phone: String? = null,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val role: String? = null
)

@Serializable
data class UpdateUserRequest(
    val email: String,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val role: String? = null
)

@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserSafeDto(
    val id: Int? = null,
    val email: String,
    val phone: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePicture: String? = null,
    val role: String? = null,
    val createdAt: String
)