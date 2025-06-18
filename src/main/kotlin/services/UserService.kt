package com.techbros.services

import com.techbros.exceptions.ApiException
import com.techbros.models.dto.CreateUserRequest
import com.techbros.models.dto.UpdatePasswordRequest
import com.techbros.models.dto.UpdateUserRequest
import com.techbros.models.dto.UserDto
import com.techbros.models.dto.UserLoginRequest
import com.techbros.models.dto.UserSafeDto
import com.techbros.repositories.UserRepository
import io.ktor.http.*
import org.mindrot.jbcrypt.BCrypt

class UserService(private val userRepository: UserRepository) {

    suspend fun createUser(request: CreateUserRequest): UserSafeDto {
        validateUserRequest(request)

        if (userRepository.emailExists(request.email)) {
            throw ApiException("Email already exists", HttpStatusCode.Conflict)
        }

        val userId = userRepository.create(request)
        return getUserByIdSafe(userId)
    }

    suspend fun getUserById(id: Int): UserDto {
        return userRepository.findById(id)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)
    }

    suspend fun getUserByIdSafe(id: Int): UserSafeDto {
        return userRepository.findByIdSafe(id)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)
    }

    suspend fun getUserByEmail(email: String): UserDto {
        return userRepository.findByEmail(email)
            ?: throw ApiException("User not found", HttpStatusCode.NotFound)
    }

    suspend fun getAllUsers(): List<UserSafeDto> {
        return userRepository.findAll()
    }

    suspend fun updateUser(id: Int, request: UpdateUserRequest): UserSafeDto {
        validateUpdateUserRequest(request)

        // Check if email already exists for another user
        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null && existingUser.id != id) {
            throw ApiException("Email already exists", HttpStatusCode.Conflict)
        }

        val updated = userRepository.update(id, request)
        if (!updated) {
            throw ApiException("User not found", HttpStatusCode.NotFound)
        }

        return getUserByIdSafe(id)
    }

    suspend fun updatePassword(id: Int, request: UpdatePasswordRequest): UserSafeDto {
        validatePasswordRequest(request)

        val user = getUserById(id)
        if (!BCrypt.checkpw(request.currentPassword, user.passwordHash)) {
            throw ApiException("Current password is incorrect", HttpStatusCode.BadRequest)
        }

        val updated = userRepository.updatePassword(id, request.newPassword)
        if (!updated) {
            throw ApiException("User not found", HttpStatusCode.NotFound)
        }

        return getUserByIdSafe(id)
    }

    suspend fun deleteUser(id: Int) {
        val deleted = userRepository.delete(id)
        if (!deleted) {
            throw ApiException("User not found", HttpStatusCode.NotFound)
        }
    }

    suspend fun getUsersByRole(role: String): List<UserSafeDto> {
        if (role.isBlank()) {
            throw ApiException("Role cannot be empty", HttpStatusCode.BadRequest)
        }
        return userRepository.findByRole(role)
    }

    suspend fun authenticateUser(request: UserLoginRequest): UserSafeDto {
        validateLoginRequest(request)

        val user = userRepository.verifyPassword(request.email, request.password)
            ?: throw ApiException("Invalid email or password", HttpStatusCode.Unauthorized)

        return UserSafeDto(
            id = user.id,
            email = user.email,
            phone = user.phone,
            firstName = user.firstName,
            lastName = user.lastName,
            profilePicture = user.profilePicture,
            role = user.role,
            createdAt = user.createdAt
        )
    }

    private fun validateUserRequest(request: CreateUserRequest) {
        when {
            request.email.isBlank() -> throw ApiException("Email cannot be empty", HttpStatusCode.BadRequest)
            request.email.length > 255 -> throw ApiException("Email is too long (max 255 characters)", HttpStatusCode.BadRequest)
            !isValidEmail(request.email) -> throw ApiException("Invalid email format", HttpStatusCode.BadRequest)
            request.password.isBlank() -> throw ApiException("Password cannot be empty", HttpStatusCode.BadRequest)
            request.password.length < 6 -> throw ApiException("Password must be at least 6 characters long", HttpStatusCode.BadRequest)
            request.phone?.let { it.length > 20 } == true -> throw ApiException("Phone number is too long (max 20 characters)", HttpStatusCode.BadRequest)
            request.firstName?.let { it.length > 100 } == true -> throw ApiException("First name is too long (max 100 characters)", HttpStatusCode.BadRequest)
            request.lastName?.let { it.length > 100 } == true -> throw ApiException("Last name is too long (max 100 characters)", HttpStatusCode.BadRequest)
            request.role?.let { it.length > 50 } == true -> throw ApiException("Role is too long (max 50 characters)", HttpStatusCode.BadRequest)
        }
    }

    private fun validateUpdateUserRequest(request: UpdateUserRequest) {
        when {
            request.email.isBlank() -> throw ApiException("Email cannot be empty", HttpStatusCode.BadRequest)
            request.email.length > 255 -> throw ApiException("Email is too long (max 255 characters)", HttpStatusCode.BadRequest)
            !isValidEmail(request.email) -> throw ApiException("Invalid email format", HttpStatusCode.BadRequest)
            request.phone?.let { it.length > 20 } == true -> throw ApiException("Phone number is too long (max 20 characters)", HttpStatusCode.BadRequest)
            request.firstName?.let { it.length > 100 } == true -> throw ApiException("First name is too long (max 100 characters)", HttpStatusCode.BadRequest)
            request.lastName?.let { it.length > 100 } == true -> throw ApiException("Last name is too long (max 100 characters)", HttpStatusCode.BadRequest)
            request.role?.let { it.length > 50 } == true -> throw ApiException("Role is too long (max 50 characters)", HttpStatusCode.BadRequest)
        }
    }

    private fun validatePasswordRequest(request: UpdatePasswordRequest) {
        when {
            request.currentPassword.isBlank() -> throw ApiException("Current password cannot be empty", HttpStatusCode.BadRequest)
            request.newPassword.isBlank() -> throw ApiException("New password cannot be empty", HttpStatusCode.BadRequest)
            request.newPassword.length < 6 -> throw ApiException("New password must be at least 6 characters long", HttpStatusCode.BadRequest)
            request.currentPassword == request.newPassword -> throw ApiException("New password must be different from current password", HttpStatusCode.BadRequest)
        }
    }

    private fun validateLoginRequest(request: UserLoginRequest) {
        when {
            request.email.isBlank() -> throw ApiException("Email cannot be empty", HttpStatusCode.BadRequest)
            request.password.isBlank() -> throw ApiException("Password cannot be empty", HttpStatusCode.BadRequest)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}