package com.techbros.plugins

import com.techbros.repositories.LocationRepository
import com.techbros.repositories.UserRepository
import com.techbros.routes.api.locationRoutes
import com.techbros.routes.api.userRoutes
import com.techbros.services.LocationService
import com.techbros.services.UserService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Initialize dependencies
    val userRepository = UserRepository()
    val userService = UserService(userRepository)

    val locationRepository = LocationRepository()
    val locationService = LocationService(locationRepository)

    // API routes

    routing {
        get("/") {
            call.respondText("Asset Management API Server")
        }

        get("/health") {
            call.respondText("OK")
        }

        // API routes
        userRoutes(userService)
        locationRoutes(locationService)
    }
}