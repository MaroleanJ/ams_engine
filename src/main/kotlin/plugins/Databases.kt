package com.techbros.plugins

import com.techbros.config.DatabaseConfig
import com.techbros.database.tables.AssetCategories
import com.techbros.database.tables.AssetHistory
import com.techbros.database.tables.AssetIssues
import com.techbros.database.tables.Assets
import com.techbros.database.tables.FileAttachments
import com.techbros.database.tables.Locations
import com.techbros.database.tables.MaintenanceRecords
import com.techbros.database.tables.MaintenanceSchedules
import com.techbros.database.tables.MaintenanceTypes
import com.techbros.database.tables.SoftwareLicenses
import com.techbros.database.tables.Subscriptions
import com.techbros.database.tables.Users
import com.techbros.database.tables.Vendors
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val database = DatabaseConfig.init()

    // Create tables
    transaction(database) {
        SchemaUtils.create(
            Users,
            Locations,
            AssetCategories,
            Vendors,
            Assets,
            AssetHistory,
            MaintenanceTypes,
            MaintenanceSchedules,
            MaintenanceRecords,
            SoftwareLicenses,
            Subscriptions,
            FileAttachments,
            AssetIssues
        )
    }
}