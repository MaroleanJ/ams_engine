package com.techbros.repositories

import com.techbros.database.tables.Subscriptions
import com.techbros.database.tables.Vendors
import com.techbros.database.tables.Users
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDate

class SubscriptionRepository {

    suspend fun create(request: CreateSubscriptionRequest): Int = dbQuery {
        Subscriptions.insert {
            it[name] = request.name
            it[vendorId] = request.vendorId
            it[plan] = request.plan
            it[startDate] = request.startDate?.let { LocalDate.parse(it) }
            it[expiryDate] = request.expiryDate?.let { LocalDate.parse(it) }
            it[renewalDate] = request.renewalDate?.let { LocalDate.parse(it) }
            it[cost] = request.cost?.let { BigDecimal(it) }
            it[billingCycle] = request.billingCycle
            it[assignedTo] = request.assignedTo
            it[notes] = request.notes
            it[status] = request.status
            it[autoRenewal] = request.autoRenewal
        }[Subscriptions.id]
    }

    suspend fun bulkCreate(requests: List<CreateSubscriptionRequest>): List<Int> = dbQuery {
        requests.map { request ->
            Subscriptions.insert {
                it[name] = request.name
                it[vendorId] = request.vendorId
                it[plan] = request.plan
                it[startDate] = request.startDate?.let { LocalDate.parse(it) }
                it[expiryDate] = request.expiryDate?.let { LocalDate.parse(it) }
                it[renewalDate] = request.renewalDate?.let { LocalDate.parse(it) }
                it[cost] = request.cost?.let { BigDecimal(it) }
                it[billingCycle] = request.billingCycle
                it[assignedTo] = request.assignedTo
                it[notes] = request.notes
                it[status] = request.status
                it[autoRenewal] = request.autoRenewal
            }[Subscriptions.id]
        }
    }

    suspend fun findById(id: Int): SubscriptionDto? = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateSubscriptionRequest): Boolean = dbQuery {
        Subscriptions.update({ Subscriptions.id eq id }) {
            it[name] = request.name
            it[vendorId] = request.vendorId
            it[plan] = request.plan
            it[startDate] = request.startDate?.let { LocalDate.parse(it) }
            it[expiryDate] = request.expiryDate?.let { LocalDate.parse(it) }
            it[renewalDate] = request.renewalDate?.let { LocalDate.parse(it) }
            it[cost] = request.cost?.let { BigDecimal(it) }
            it[billingCycle] = request.billingCycle
            it[assignedTo] = request.assignedTo
            it[notes] = request.notes
            it[status] = request.status
            it[autoRenewal] = request.autoRenewal
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Subscriptions.deleteWhere { Subscriptions.id eq id } > 0
    }

    suspend fun findByVendorId(vendorId: Int): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.vendorId eq vendorId }
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssignedTo(assignedTo: Int): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.assignedTo eq assignedTo }
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByStatus(status: String): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.status eq status }
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByBillingCycle(billingCycle: String): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.billingCycle eq billingCycle }
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findExpiringBetween(startDate: String, endDate: String): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where {
                (Subscriptions.expiryDate greaterEq LocalDate.parse(startDate)) and
                        (Subscriptions.expiryDate lessEq LocalDate.parse(endDate))
            }
            .orderBy(Subscriptions.expiryDate to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findExpiringThisMonth(): List<SubscriptionRenewalDto> = dbQuery {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).minusDays(1)

        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where {
                (Subscriptions.expiryDate greaterEq startOfMonth) and
                        (Subscriptions.expiryDate lessEq endOfMonth) and
                        (Subscriptions.status eq "ACTIVE")
            }
            .orderBy(Subscriptions.expiryDate to SortOrder.ASC)
            .map { mapRowToRenewalDto(it) }
    }

    suspend fun findByAutoRenewal(autoRenewal: Boolean): List<SubscriptionDto> = dbQuery {
        Subscriptions
            .join(Vendors, JoinType.LEFT, Subscriptions.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, Subscriptions.assignedTo, Users.id)
            .selectAll()
            .where { Subscriptions.autoRenewal eq autoRenewal }
            .orderBy(Subscriptions.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun deleteByVendorId(vendorId: Int): Int = dbQuery {
        Subscriptions.deleteWhere { Subscriptions.vendorId eq vendorId }
    }

    suspend fun deleteByAssignedTo(assignedTo: Int): Int = dbQuery {
        Subscriptions.deleteWhere { Subscriptions.assignedTo eq assignedTo }
    }

    suspend fun getSubscriptionStats(): SubscriptionStatsDto = dbQuery {
        val allSubscriptions = Subscriptions.selectAll().toList()

        val totalCost = allSubscriptions.mapNotNull { it[Subscriptions.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        val subscriptionsByStatus = allSubscriptions.groupBy { it[Subscriptions.status] }
            .mapValues { it.value.size }

        val subscriptionsByBillingCycle = allSubscriptions.groupBy { it[Subscriptions.billingCycle] ?: "N/A" }
            .mapValues { it.value.size }

        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).minusDays(1)

        val expiringThisMonth = allSubscriptions.count { row ->
            val expiryDate = row[Subscriptions.expiryDate]
            expiryDate != null && expiryDate >= startOfMonth && expiryDate <= endOfMonth
        }

        val autoRenewalCount = allSubscriptions.count { it[Subscriptions.autoRenewal] }

        SubscriptionStatsDto(
            totalSubscriptions = allSubscriptions.size,
            totalCost = if (totalCost > BigDecimal.ZERO) totalCost.toString() else null,
            averageCost = if (totalCost > BigDecimal.ZERO && allSubscriptions.isNotEmpty())
                (totalCost / BigDecimal(allSubscriptions.size)).toString() else null,
            subscriptionsByStatus = subscriptionsByStatus,
            subscriptionsByBillingCycle = subscriptionsByBillingCycle,
            expiringThisMonth = expiringThisMonth,
            autoRenewalCount = autoRenewalCount
        )
    }

    private fun mapRowToDto(row: ResultRow): SubscriptionDto {
        return SubscriptionDto(
            id = row[Subscriptions.id],
            name = row[Subscriptions.name],
            vendorId = row[Subscriptions.vendorId],
            plan = row[Subscriptions.plan],
            startDate = row[Subscriptions.startDate]?.toString(),
            expiryDate = row[Subscriptions.expiryDate]?.toString(),
            renewalDate = row[Subscriptions.renewalDate]?.toString(),
            cost = row[Subscriptions.cost]?.toString(),
            billingCycle = row[Subscriptions.billingCycle],
            assignedTo = row[Subscriptions.assignedTo],
            notes = row[Subscriptions.notes],
            status = row[Subscriptions.status],
            autoRenewal = row[Subscriptions.autoRenewal],
            createdAt = row[Subscriptions.createdAt].toString(),
            vendorName = row.getOrNull(Vendors.name),
            vendorEmail = row.getOrNull(Vendors.contactEmail),
            assignedToName = "${row.getOrNull(Users.firstName) ?: ""} ${row.getOrNull(Users.lastName) ?: ""}".trim(),
            assignedToEmail = row.getOrNull(Users.email)
        )
    }

    private fun mapRowToRenewalDto(row: ResultRow): SubscriptionRenewalDto {
        val expiryDate = row[Subscriptions.expiryDate]
        val daysUntilExpiry = if (expiryDate != null) {
            java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
        } else 0L

        return SubscriptionRenewalDto(
            id = row[Subscriptions.id],
            name = row[Subscriptions.name],
            expiryDate = expiryDate?.toString() ?: "",
            renewalDate = row[Subscriptions.renewalDate]?.toString(),
            cost = row[Subscriptions.cost]?.toString(),
            billingCycle = row[Subscriptions.billingCycle],
            autoRenewal = row[Subscriptions.autoRenewal],
            daysUntilExpiry = daysUntilExpiry,
            assignedToName = "${row.getOrNull(Users.firstName) ?: ""} ${row.getOrNull(Users.lastName) ?: ""}".trim(),
            vendorName = row.getOrNull(Vendors.name)
        )
    }
}