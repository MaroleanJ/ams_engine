package com.techbros.models.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardOverviewDto(
    val assetMetrics: AssetMetricsDto,
    val maintenanceMetrics: MaintenanceMetricsDto,
    val softwareLicenseMetrics: SoftwareLicenseMetricsDto,
    val subscriptionMetrics: SubscriptionMetricsDto,
    val issueMetrics: IssueMetricsDto,
    val upcomingEvents: UpcomingEventsDto,
    val financialSummary: FinancialSummaryDto,
    val lastUpdated: String
)

@Serializable
data class AssetMetricsDto(
    val totalAssets: Int,
    val assetsByStatus: Map<String, Int>,
    val assetsByCategory: Map<String, Int>,
    val assetsByLocation: Map<String, Int>,
    val totalValue: String? = null,
    val avgAssetAge: String? = null,
    val assetsNearWarrantyExpiry: Int,
    val unassignedAssets: Int
)

@Serializable
data class MaintenanceMetricsDto(
    val totalMaintenanceRecords: Int,
    val maintenanceThisMonth: Int,
    val pendingMaintenance: Int,
    val overdueMaintenance: Int,
    val maintenanceCostThisMonth: String? = null,
    val avgMaintenanceCost: String? = null,
    val maintenanceByType: Map<String, Int>,
    val maintenanceByStatus: Map<String, Int>,
    val topMaintenanceAssets: List<AssetMaintenanceSummaryDto>
)

@Serializable
data class SoftwareLicenseMetricsDto(
    val totalLicenses: Int,
    val activeLicenses: Int,
    val expiredLicenses: Int,
    val expiringLicenses: Int, // Within 30 days
    val licenseUtilization: String, // Percentage of seats used
    val totalSeats: Int,
    val usedSeats: Int,
    val licensesByVendor: Map<String, Int>
)

@Serializable
data class SubscriptionMetricsDto(
    val totalSubscriptions: Int,
    val activeSubscriptions: Int,
    val expiredSubscriptions: Int,
    val expiringSubscriptions: Int, // Within 30 days
    val monthlyCost: String? = null,
    val annualCost: String? = null,
    val subscriptionsByVendor: Map<String, Int>
)

@Serializable
data class IssueMetricsDto(
    val totalIssues: Int,
    val openIssues: Int,
    val resolvedIssues: Int,
    val criticalIssues: Int,
    val issuesByType: Map<String, Int>,
    val issuesBySeverity: Map<String, Int>,
    val avgResolutionTime: String? = null,
    val issuesThisMonth: Int
)

@Serializable
data class UpcomingEventsDto(
    val maintenanceDue: List<UpcomingMaintenanceDto>,
    val licenseExpiring: List<ExpiringLicenseDto>,
    val subscriptionExpiring: List<ExpiringSubscriptionDto>,
    val warrantyExpiring: List<ExpiringWarrantyDto>
)

@Serializable
data class FinancialSummaryDto(
    val totalAssetValue: String? = null,
    val maintenanceCostThisMonth: String? = null,
    val subscriptionCostThisMonth: String? = null,
    val totalMonthlyCosts: String? = null,
    val projectedAnnualCosts: String? = null,
    val costByCategory: Map<String, String>
)

@Serializable
data class AssetMaintenanceSummaryDto(
    val assetId: Int,
    val assetName: String,
    val serialNumber: String? = null,
    val maintenanceCount: Int,
    val totalCost: String? = null,
    val lastMaintenanceDate: String? = null
)

@Serializable
data class UpcomingMaintenanceDto(
    val scheduleId: Int,
    val assetId: Int,
    val assetName: String,
    val maintenanceType: String,
    val dueDate: String,
    val priority: String,
    val daysUntilDue: Int,
    val isOverdue: Boolean
)

@Serializable
data class ExpiringLicenseDto(
    val licenseId: Int,
    val name: String,
    val vendorName: String? = null,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val seatsUsed: Int,
    val totalSeats: Int
)

@Serializable
data class ExpiringSubscriptionDto(
    val subscriptionId: Int,
    val name: String,
    val vendorName: String? = null,
    val expiryDate: String,
    val daysUntilExpiry: Int,
    val cost: String? = null,
    val autoRenewal: Boolean
)

@Serializable
data class ExpiringWarrantyDto(
    val assetId: Int,
    val assetName: String,
    val serialNumber: String? = null,
    val warrantyExpiryDate: String,
    val daysUntilExpiry: Int,
    val vendor: String? = null
)

@Serializable
data class DashboardFiltersDto(
    val locationId: Int? = null,
    val categoryId: Int? = null,
    val dateRange: DateRangeDto? = null
)

@Serializable
data class DateRangeDto(
    val startDate: String,
    val endDate: String
)