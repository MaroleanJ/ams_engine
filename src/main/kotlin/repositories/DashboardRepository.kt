package com.techbros.repositories

import com.techbros.database.tables.*
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class DashboardRepository {

    suspend fun getAssetMetrics(filters: DashboardFiltersDto? = null): AssetMetricsDto = dbQuery {
        var query = Assets.selectAll()

        // Apply filters
        filters?.locationId?.let { locationId ->
            query = query.andWhere { Assets.locationId eq locationId }
        }
        filters?.categoryId?.let { categoryId ->
            query = query.andWhere { Assets.categoryId eq categoryId }
        }

        val assets = query.toList()
        val totalAssets = assets.size

        val assetsByStatus = assets.groupBy { it[Assets.status] ?: "Unknown" }
            .mapValues { it.value.size }

        // Fix: Get categories with proper join and count
        val assetsByCategory = Assets
            .join(AssetCategories, JoinType.LEFT, Assets.categoryId, AssetCategories.id)
            .selectAll()
            .toList()
            .groupBy { it.getOrNull(AssetCategories.name) ?: "Unknown" }
            .mapValues { it.value.size }

        // Fix: Get locations with proper join and count
        val assetsByLocation = Assets
            .join(Locations, JoinType.LEFT, Assets.locationId, Locations.id)
            .selectAll()
            .toList()
            .groupBy { it.getOrNull(Locations.name) ?: "Unknown" }
            .mapValues { it.value.size }

        val totalValue = assets.mapNotNull { it[Assets.currentValue] }
            .fold(BigDecimal.ZERO) { acc, value -> acc + value }

        val today = LocalDate.now()
        val assetsNearWarrantyExpiry = assets.count { asset ->
            asset[Assets.warrantyExpiry]?.let { expiry ->
                ChronoUnit.DAYS.between(today, expiry) <= 30 && expiry.isAfter(today)
            } ?: false
        }

        val unassignedAssets = assets.count { it[Assets.assignedTo] == null }

        val avgAssetAge = assets.mapNotNull { it[Assets.purchaseDate] }
            .takeIf { it.isNotEmpty() }
            ?.map { ChronoUnit.DAYS.between(it, today) }
            ?.average()

        AssetMetricsDto(
            totalAssets = totalAssets,
            assetsByStatus = assetsByStatus,
            assetsByCategory = assetsByCategory,
            assetsByLocation = assetsByLocation,
            totalValue = if (totalValue > BigDecimal.ZERO) totalValue.toString() else null,
            avgAssetAge = avgAssetAge?.let { "${"%.1f".format(it / 365.25)} years" },
            assetsNearWarrantyExpiry = assetsNearWarrantyExpiry,
            unassignedAssets = unassignedAssets
        )
    }

    suspend fun getMaintenanceMetrics(filters: DashboardFiltersDto? = null): MaintenanceMetricsDto = dbQuery {
        val today = LocalDate.now()
        val thisMonth = today.withDayOfMonth(1)

        // Get all maintenance records
        val maintenanceRecords = MaintenanceRecords.selectAll().toList()
        val totalMaintenanceRecords = maintenanceRecords.size

        val maintenanceThisMonth = maintenanceRecords.count {
            it[MaintenanceRecords.performedDate].isAfter(thisMonth.minusDays(1))
        }

        // Get pending and overdue maintenance from schedules
        val schedules = MaintenanceSchedules.selectAll()
            .where { MaintenanceSchedules.isActive eq true }
            .toList()

        val pendingMaintenance = schedules.count {
            it[MaintenanceSchedules.nextDue].isAfter(today)
        }

        val overdueMaintenance = schedules.count {
            it[MaintenanceSchedules.nextDue].isBefore(today)
        }

        val maintenanceCostThisMonth = maintenanceRecords
            .filter { it[MaintenanceRecords.performedDate].isAfter(thisMonth.minusDays(1)) }
            .mapNotNull { it[MaintenanceRecords.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        val avgMaintenanceCost = maintenanceRecords
            .mapNotNull { it[MaintenanceRecords.cost] }
            .takeIf { it.isNotEmpty() }
            ?.let { costs -> costs.fold(BigDecimal.ZERO) { acc, cost -> acc + cost } / BigDecimal(costs.size) }

        val maintenanceByType = maintenanceRecords.groupBy { it[MaintenanceRecords.maintenanceType] }
            .mapValues { it.value.size }

        val maintenanceByStatus = maintenanceRecords.groupBy { it[MaintenanceRecords.status] }
            .mapValues { it.value.size }

        // Fix: Get top maintenance assets with proper aggregation
        val maintenanceWithAssets = MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .selectAll()
            .toList()

        val topMaintenanceAssets = maintenanceWithAssets
            .groupBy { it[MaintenanceRecords.assetId] }
            .map { (assetId, records) ->
                val firstRecord = records.first()
                val maintenanceCount = records.size
                val totalCost = records.mapNotNull { it[MaintenanceRecords.cost] }
                    .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }
                val lastMaintenanceDate = records.maxOfOrNull { it[MaintenanceRecords.performedDate] }

                AssetMaintenanceSummaryDto(
                    assetId = assetId,
                    assetName = firstRecord[Assets.name],
                    serialNumber = firstRecord[Assets.serialNumber],
                    maintenanceCount = maintenanceCount,
                    totalCost = if (totalCost > BigDecimal.ZERO) totalCost.toString() else null,
                    lastMaintenanceDate = lastMaintenanceDate?.toString()
                )
            }
            .sortedByDescending { it.maintenanceCount }
            .take(5)

        MaintenanceMetricsDto(
            totalMaintenanceRecords = totalMaintenanceRecords,
            maintenanceThisMonth = maintenanceThisMonth,
            pendingMaintenance = pendingMaintenance,
            overdueMaintenance = overdueMaintenance,
            maintenanceCostThisMonth = if (maintenanceCostThisMonth > BigDecimal.ZERO) maintenanceCostThisMonth.toString() else null,
            avgMaintenanceCost = avgMaintenanceCost?.toString(),
            maintenanceByType = maintenanceByType,
            maintenanceByStatus = maintenanceByStatus,
            topMaintenanceAssets = topMaintenanceAssets
        )
    }

    suspend fun getSoftwareLicenseMetrics(): SoftwareLicenseMetricsDto = dbQuery {
        val licenses = SoftwareLicenses.selectAll().toList()
        val totalLicenses = licenses.size

        val today = LocalDate.now()
        val activeLicenses = licenses.count { license ->
            val expiry = license[SoftwareLicenses.expiryDate]
            expiry == null || expiry.isAfter(today)
        }

        val expiredLicenses = licenses.count { license ->
            license[SoftwareLicenses.expiryDate]?.isBefore(today) == true
        }

        val expiringLicenses = licenses.count { license ->
            license[SoftwareLicenses.expiryDate]?.let { expiry ->
                ChronoUnit.DAYS.between(today, expiry) <= 30 && expiry.isAfter(today)
            } ?: false
        }

        val totalSeats = licenses.sumOf { it[SoftwareLicenses.numberOfSeats] ?: 0 }
        val usedSeats = licenses.sumOf { it[SoftwareLicenses.seatsUsed] ?: 0 }
        val licenseUtilization = if (totalSeats > 0) {
            "${"%.1f".format((usedSeats.toDouble() / totalSeats) * 100)}%"
        } else "0%"

        // Fix: Get licenses by vendor with proper aggregation
        val licensesWithVendors = SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .selectAll()
            .toList()

        val licensesByVendor = licensesWithVendors
            .groupBy { it.getOrNull(Vendors.name) ?: "Unknown" }
            .mapValues { it.value.size }

        SoftwareLicenseMetricsDto(
            totalLicenses = totalLicenses,
            activeLicenses = activeLicenses,
            expiredLicenses = expiredLicenses,
            expiringLicenses = expiringLicenses,
            licenseUtilization = licenseUtilization,
            totalSeats = totalSeats,
            usedSeats = usedSeats,
            licensesByVendor = licensesByVendor
        )
    }

    suspend fun getSubscriptionMetrics(): SubscriptionMetricsDto = dbQuery {
        val subscriptions = Subscriptions.selectAll().toList()
        val totalSubscriptions = subscriptions.size

        val today = LocalDate.now()
        val activeSubscriptions = subscriptions.count { subscription ->
            val expiry = subscription[Subscriptions.expiryDate]
            expiry == null || expiry.isAfter(today)
        }

        val expiredSubscriptions = subscriptions.count { subscription ->
            subscription[Subscriptions.expiryDate]?.isBefore(today) == true
        }

        val expiringSubscriptions = subscriptions.count { subscription ->
            subscription[Subscriptions.expiryDate]?.let { expiry ->
                ChronoUnit.DAYS.between(today, expiry) <= 30 && expiry.isAfter(today)
            } ?: false
        }

        val monthlyCost = subscriptions
            .filter { it[Subscriptions.billingCycle] == "monthly" }
            .mapNotNull { it[Subscriptions.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        val annualCost = subscriptions
            .filter { it[Subscriptions.billingCycle] == "yearly" }
            .mapNotNull { it[Subscriptions.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        // Fix: Get subscriptions by vendor with proper aggregation
        val subscriptionsWithVendors = Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .selectAll()
            .toList()

        val subscriptionsByVendor = subscriptionsWithVendors
            .groupBy { it.getOrNull(Vendors.name) ?: "Unknown" }
            .mapValues { it.value.size }

        SubscriptionMetricsDto(
            totalSubscriptions = totalSubscriptions,
            activeSubscriptions = activeSubscriptions,
            expiredSubscriptions = expiredSubscriptions,
            expiringSubscriptions = expiringSubscriptions,
            monthlyCost = if (monthlyCost > BigDecimal.ZERO) monthlyCost.toString() else null,
            annualCost = if (annualCost > BigDecimal.ZERO) annualCost.toString() else null,
            subscriptionsByVendor = subscriptionsByVendor
        )
    }

    suspend fun getIssueMetrics(): IssueMetricsDto = dbQuery {
        val issues = AssetIssues.selectAll().toList()
        val totalIssues = issues.size

        val openIssues = issues.count { it[AssetIssues.status] in listOf("OPEN", "IN_PROGRESS") }
        val resolvedIssues = issues.count { it[AssetIssues.status] == "RESOLVED" }
        val criticalIssues = issues.count { it[AssetIssues.severity] == "CRITICAL" }

        val issuesByType = issues.groupBy { it[AssetIssues.issueType] ?: "Unknown" }
            .mapValues { it.value.size }

        val issuesBySeverity = issues.groupBy { it[AssetIssues.severity] ?: "Unknown" }
            .mapValues { it.value.size }

        val thisMonth = LocalDate.now().withDayOfMonth(1)
        val issuesThisMonth = issues.count {
            it[AssetIssues.reportedAt].toLocalDate().isAfter(thisMonth.minusDays(1))
        }

        // Calculate average resolution time for resolved issues
        val avgResolutionTime = issues
            .filter { it[AssetIssues.resolvedAt] != null }
            .mapNotNull { issue ->
                val reported = issue[AssetIssues.reportedAt]
                val resolved = issue[AssetIssues.resolvedAt]
                if (resolved != null) ChronoUnit.HOURS.between(reported, resolved) else null
            }
            .takeIf { it.isNotEmpty() }
            ?.average()

        IssueMetricsDto(
            totalIssues = totalIssues,
            openIssues = openIssues,
            resolvedIssues = resolvedIssues,
            criticalIssues = criticalIssues,
            issuesByType = issuesByType,
            issuesBySeverity = issuesBySeverity,
            avgResolutionTime = avgResolutionTime?.let { "${"%.1f".format(it)} hours" },
            issuesThisMonth = issuesThisMonth
        )
    }

    suspend fun getUpcomingEvents(): UpcomingEventsDto = dbQuery {
        val today = LocalDate.now()
        val thirtyDaysFromNow = today.plusDays(30)

        // Upcoming maintenance
        val maintenanceDue = MaintenanceSchedules
            .join(Assets, JoinType.LEFT, MaintenanceSchedules.assetId, Assets.id)
            .selectAll()
            .where {
                (MaintenanceSchedules.isActive eq true) and
                        (MaintenanceSchedules.nextDue lessEq thirtyDaysFromNow)
            }
            .orderBy(MaintenanceSchedules.nextDue)
            .map { row ->
                val dueDate = row[MaintenanceSchedules.nextDue]
                val daysUntil = ChronoUnit.DAYS.between(today, dueDate).toInt()

                UpcomingMaintenanceDto(
                    scheduleId = row[MaintenanceSchedules.id],
                    assetId = row[MaintenanceSchedules.assetId],
                    assetName = row[Assets.name],
                    maintenanceType = row[MaintenanceSchedules.maintenanceType] ?: "Unknown",
                    dueDate = dueDate.toString(),
                    priority = row[MaintenanceSchedules.priority] ?: "MEDIUM",
                    daysUntilDue = daysUntil,
                    isOverdue = dueDate.isBefore(today)
                )
            }

        // Expiring licenses
        val licenseExpiring = SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .selectAll()
            .where {
                (SoftwareLicenses.expiryDate.isNotNull()) and
                        (SoftwareLicenses.expiryDate lessEq thirtyDaysFromNow) and
                        (SoftwareLicenses.expiryDate greaterEq today)
            }
            .orderBy(SoftwareLicenses.expiryDate)
            .map { row ->
                val expiryDate = row[SoftwareLicenses.expiryDate]!!
                val daysUntil = ChronoUnit.DAYS.between(today, expiryDate).toInt()

                ExpiringLicenseDto(
                    licenseId = row[SoftwareLicenses.id],
                    name = row[SoftwareLicenses.name],
                    vendorName = row[Vendors.name],
                    expiryDate = expiryDate.toString(),
                    daysUntilExpiry = daysUntil,
                    seatsUsed = row[SoftwareLicenses.seatsUsed] ?: 0,
                    totalSeats = row[SoftwareLicenses.numberOfSeats] ?: 0
                )
            }

        // Expiring subscriptions
        val subscriptionExpiring = Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .selectAll()
            .where {
                (Subscriptions.expiryDate.isNotNull()) and
                        (Subscriptions.expiryDate lessEq thirtyDaysFromNow) and
                        (Subscriptions.expiryDate greaterEq today)
            }
            .orderBy(Subscriptions.expiryDate)
            .map { row ->
                val expiryDate = row[Subscriptions.expiryDate]!!
                val daysUntil = ChronoUnit.DAYS.between(today, expiryDate).toInt()

                ExpiringSubscriptionDto(
                    subscriptionId = row[Subscriptions.id],
                    name = row[Subscriptions.name],
                    vendorName = row[Vendors.name],
                    expiryDate = expiryDate.toString(),
                    daysUntilExpiry = daysUntil,
                    cost = row[Subscriptions.cost]?.toString(),
                    autoRenewal = row[Subscriptions.autoRenewal] ?: false
                )
            }

        // Expiring warranties
        val warrantyExpiring = Assets
            .join(Vendors, JoinType.LEFT, Assets.vendorId, Vendors.id)
            .selectAll()
            .where {
                (Assets.warrantyExpiry.isNotNull()) and
                        (Assets.warrantyExpiry lessEq thirtyDaysFromNow) and
                        (Assets.warrantyExpiry greaterEq today)
            }
            .orderBy(Assets.warrantyExpiry)
            .map { row ->
                val expiryDate = row[Assets.warrantyExpiry]!!
                val daysUntil = ChronoUnit.DAYS.between(today, expiryDate).toInt()

                ExpiringWarrantyDto(
                    assetId = row[Assets.id],
                    assetName = row[Assets.name],
                    serialNumber = row[Assets.serialNumber],
                    warrantyExpiryDate = expiryDate.toString(),
                    daysUntilExpiry = daysUntil,
                    vendor = row[Vendors.name]
                )
            }

        UpcomingEventsDto(
            maintenanceDue = maintenanceDue,
            licenseExpiring = licenseExpiring,
            subscriptionExpiring = subscriptionExpiring,
            warrantyExpiring = warrantyExpiring
        )
    }

    suspend fun getFinancialSummary(): FinancialSummaryDto = dbQuery {
        val today = LocalDate.now()
        val thisMonth = today.withDayOfMonth(1)

        // Total asset value
        val totalAssetValue = Assets.selectAll()
            .mapNotNull { it[Assets.currentValue] }
            .fold(BigDecimal.ZERO) { acc, value -> acc + value }

        // Maintenance cost this month
        val maintenanceCostThisMonth = MaintenanceRecords.selectAll()
            .where { MaintenanceRecords.performedDate greaterEq thisMonth }
            .mapNotNull { it[MaintenanceRecords.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        // Subscription cost this month
        val subscriptionCostThisMonth = Subscriptions.selectAll()
            .where { Subscriptions.billingCycle eq "monthly" }
            .mapNotNull { it[Subscriptions.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        val totalMonthlyCosts = maintenanceCostThisMonth + subscriptionCostThisMonth

        val projectedAnnualCosts = totalMonthlyCosts * BigDecimal(12)

        // Cost by category
        val costByCategory = mapOf(
            "Asset Value" to totalAssetValue.toString(),
            "Maintenance (This Month)" to maintenanceCostThisMonth.toString(),
            "Subscriptions (Monthly)" to subscriptionCostThisMonth.toString()
        )

        FinancialSummaryDto(
            totalAssetValue = if (totalAssetValue > BigDecimal.ZERO) totalAssetValue.toString() else null,
            maintenanceCostThisMonth = if (maintenanceCostThisMonth > BigDecimal.ZERO) maintenanceCostThisMonth.toString() else null,
            subscriptionCostThisMonth = if (subscriptionCostThisMonth > BigDecimal.ZERO) subscriptionCostThisMonth.toString() else null,
            totalMonthlyCosts = if (totalMonthlyCosts > BigDecimal.ZERO) totalMonthlyCosts.toString() else null,
            projectedAnnualCosts = if (projectedAnnualCosts > BigDecimal.ZERO) projectedAnnualCosts.toString() else null,
            costByCategory = costByCategory.filter { it.value != "0" }
        )
    }
}