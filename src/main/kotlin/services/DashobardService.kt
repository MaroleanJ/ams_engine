package com.techbros.services

import com.techbros.models.dto.*
import com.techbros.repositories.DashboardRepository
import java.time.LocalDateTime

class DashboardService(
    private val dashboardRepository: DashboardRepository
) {

    suspend fun getDashboardOverview(filters: DashboardFiltersDto? = null): DashboardOverviewDto {
        val assetMetrics = dashboardRepository.getAssetMetrics(filters)
        val maintenanceMetrics = dashboardRepository.getMaintenanceMetrics(filters)
        val softwareLicenseMetrics = dashboardRepository.getSoftwareLicenseMetrics()
        val subscriptionMetrics = dashboardRepository.getSubscriptionMetrics()
        val issueMetrics = dashboardRepository.getIssueMetrics()
        val upcomingEvents = dashboardRepository.getUpcomingEvents()
        val financialSummary = dashboardRepository.getFinancialSummary()

        return DashboardOverviewDto(
            assetMetrics = assetMetrics,
            maintenanceMetrics = maintenanceMetrics,
            softwareLicenseMetrics = softwareLicenseMetrics,
            subscriptionMetrics = subscriptionMetrics,
            issueMetrics = issueMetrics,
            upcomingEvents = upcomingEvents,
            financialSummary = financialSummary,
            lastUpdated = LocalDateTime.now().toString()
        )
    }

    suspend fun getAssetMetrics(filters: DashboardFiltersDto? = null): AssetMetricsDto {
        return dashboardRepository.getAssetMetrics(filters)
    }

    suspend fun getMaintenanceMetrics(filters: DashboardFiltersDto? = null): MaintenanceMetricsDto {
        return dashboardRepository.getMaintenanceMetrics(filters)
    }

    suspend fun getSoftwareLicenseMetrics(): SoftwareLicenseMetricsDto {
        return dashboardRepository.getSoftwareLicenseMetrics()
    }

    suspend fun getSubscriptionMetrics(): SubscriptionMetricsDto {
        return dashboardRepository.getSubscriptionMetrics()
    }

    suspend fun getIssueMetrics(): IssueMetricsDto {
        return dashboardRepository.getIssueMetrics()
    }

    suspend fun getUpcomingEvents(): UpcomingEventsDto {
        return dashboardRepository.getUpcomingEvents()
    }

    suspend fun getFinancialSummary(): FinancialSummaryDto {
        return dashboardRepository.getFinancialSummary()
    }
}