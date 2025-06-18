package com.techbros.routes.api

import com.techbros.exceptions.ApiException
import com.techbros.models.responses.ApiResponse
import com.techbros.models.dto.CreateUserRequest
import com.techbros.models.dto.UpdatePasswordRequest
import com.techbros.models.dto.UpdateUserRequest
import com.techbros.models.dto.UserLoginRequest
import com.techbros.models.dto.UserSafeDto
import com.techbros.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(userService: UserService) {
    route("/api/v1/users") {

        // Get all users
        get {
            val users = userService.getAllUsers()
            call.respond(
                ApiResponse(
                success = true,
                data = users
                )
            )
        }

        // Get users by role
        get("/role/{role}") {
            val role = call.parameters["role"]
                ?: throw ApiException("Role parameter is required", HttpStatusCode.BadRequest)

            val users = userService.getUsersByRole(role)
            call.respond(
                ApiResponse(
                success = true,
                data = users
            )
            )
        }

        // Get user by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val user = userService.getUserByIdSafe(id)
            call.respond(
                ApiResponse(
                success = true,
                data = user
            )
            )
        }

        // Create user
        post {
            val request = call.receive<CreateUserRequest>()
            val user = userService.createUser(request)
            call.respond(HttpStatusCode.Created, ApiResponse(
                success = true,
                data = user,
                message = "User created successfully"
            )
            )
        }

        // Update user
        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdateUserRequest>()
            val user = userService.updateUser(id, request)
            call.respond(
                ApiResponse(
                success = true,
                data = user,
                message = "User updated successfully"
            )
            )
        }

        // Update user password
        put("/{id}/password") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            val request = call.receive<UpdatePasswordRequest>()
            val user = userService.updatePassword(id, request)
            call.respond(
                ApiResponse(
                success = true,
                data = user,
                message = "Password updated successfully"
            )
            )
        }

        // Delete user
        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: throw ApiException("Invalid user ID", HttpStatusCode.BadRequest)

            userService.deleteUser(id)
            call.respond(
                ApiResponse<Unit>(
                success = true,
                message = "User deleted successfully"
            )
            )
        }

        // Login endpoint
        post("/login") {
            val request = call.receive<UserLoginRequest>()
            val user = userService.authenticateUser(request)
            call.respond(
                ApiResponse(
                success = true,
                data = user,
                message = "Login successful"
            )
            )
        }

        // Get user by email
        get("/email/{email}") {
            val email = call.parameters["email"]
                ?: throw ApiException("Email parameter is required", HttpStatusCode.BadRequest)

            val user = userService.getUserByEmail(email)
            val safeUser = UserSafeDto(
                id = user.id,
                email = user.email,
                phone = user.phone,
                firstName = user.firstName,
                lastName = user.lastName,
                profilePicture = user.profilePicture,
                role = user.role,
                createdAt = user.createdAt
            )
            call.respond(
                ApiResponse(
                success = true,
                data = safeUser
            )
            )
        }
    }
}