package com.techbros.plugins

import com.techbros.repositories.AssetCategoryRepository
import com.techbros.repositories.AssetHistoryRepository
import com.techbros.repositories.AssetRepository
import com.techbros.repositories.LocationRepository
import com.techbros.repositories.MaintenanceScheduleRepository
import com.techbros.repositories.MaintenanceTypeRepository
import com.techbros.repositories.UserRepository
import com.techbros.repositories.VendorRepository
import com.techbros.routes.api.assetCategoryRoutes
import com.techbros.routes.api.assetHistoryRoutes
import com.techbros.routes.api.assetRoutes
import com.techbros.routes.api.locationRoutes
import com.techbros.routes.api.maintenanceScheduleRoutes
import com.techbros.routes.api.maintenanceTypeRoutes
import com.techbros.routes.api.userRoutes
import com.techbros.routes.api.vendorRoutes
import com.techbros.services.AssetCategoryService
import com.techbros.services.AssetHistoryService
import com.techbros.services.AssetService
import com.techbros.services.LocationService
import com.techbros.services.MaintenanceScheduleService
import com.techbros.services.MaintenanceTypeService
import com.techbros.services.UserService
import com.techbros.services.VendorService
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // Initialize dependencies
    val userRepository = UserRepository()
    val userService = UserService(userRepository)

    val locationRepository = LocationRepository()
    val locationService = LocationService(locationRepository)

    val assetCategoryRepository = AssetCategoryRepository()
    val assetCategoryService = AssetCategoryService(assetCategoryRepository)

    val vendorRepository = VendorRepository()
    val vendorService = VendorService(vendorRepository)

    val assetRepository = AssetRepository()
    val assetService = AssetService(assetRepository, assetCategoryService, vendorService, locationService, userService)

    val assetHistoryRepository = AssetHistoryRepository()
    val assetHistoryService = AssetHistoryService(assetHistoryRepository, assetRepository, userRepository)

    val maintenanceTypeRepository = MaintenanceTypeRepository()
    val maintenanceTypeService = MaintenanceTypeService(maintenanceTypeRepository)

    val maintenanceScheduleRepository = MaintenanceScheduleRepository()
    val maintenanceScheduleService = MaintenanceScheduleService(maintenanceScheduleRepository, assetRepository, maintenanceTypeRepository, userRepository)

    // API routes

    routing {
        get("/") {
            call.respondText("Asset Management API Server")
        }

        get("/health") {
            call.respondText("OK")
        }

        post("/post-check") {
            call.respondText("Post Working")
        }

        // API routes
        userRoutes(userService)
        locationRoutes(locationService)
        assetCategoryRoutes(assetCategoryService)
        vendorRoutes(vendorService)
        assetRoutes(assetService)
        assetHistoryRoutes(assetHistoryService)
        maintenanceTypeRoutes(maintenanceTypeService)
        maintenanceScheduleRoutes(maintenanceScheduleService)
    }
}