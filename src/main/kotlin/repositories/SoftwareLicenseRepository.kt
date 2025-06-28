package com.techbros.repositories

import com.techbros.database.tables.SoftwareLicenses
import com.techbros.database.tables.Vendors
import com.techbros.database.tables.Users
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate

class SoftwareLicenseRepository {

    suspend fun create(request: CreateSoftwareLicenseRequest): Int = dbQuery {
        SoftwareLicenses.insert {
            it[name] = request.name
            it[vendorId] = request.vendorId
            it[licenseKey] = request.licenseKey
            it[startDate] = request.startDate?.let { LocalDate.parse(it) }
            it[expiryDate] = request.expiryDate?.let { LocalDate.parse(it) }
            it[numberOfSeats] = request.numberOfSeats
            it[seatsUsed] = request.seatsUsed
            it[assignedTo] = request.assignedTo
            it[notes] = request.notes
            it[status] = request.status
        }[SoftwareLicenses.id]
    }

    suspend fun findById(id: Int): SoftwareLicenseDto? = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where { SoftwareLicenses.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .orderBy(SoftwareLicenses.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateSoftwareLicenseRequest): Boolean = dbQuery {
        SoftwareLicenses.update({ SoftwareLicenses.id eq id }) {
            it[name] = request.name
            it[vendorId] = request.vendorId
            it[licenseKey] = request.licenseKey
            it[startDate] = request.startDate?.let { LocalDate.parse(it) }
            it[expiryDate] = request.expiryDate?.let { LocalDate.parse(it) }
            it[numberOfSeats] = request.numberOfSeats
            it[seatsUsed] = request.seatsUsed
            it[assignedTo] = request.assignedTo
            it[notes] = request.notes
            it[status] = request.status
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        SoftwareLicenses.deleteWhere { SoftwareLicenses.id eq id } > 0
    }

    suspend fun findByVendorId(vendorId: Int): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where { SoftwareLicenses.vendorId eq vendorId }
            .orderBy(SoftwareLicenses.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssignedTo(assignedTo: Int): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where { SoftwareLicenses.assignedTo eq assignedTo }
            .orderBy(SoftwareLicenses.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByStatus(status: String): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where { SoftwareLicenses.status eq status }
            .orderBy(SoftwareLicenses.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findExpiredLicenses(): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where {
                (SoftwareLicenses.expiryDate.isNotNull()) and
                        (SoftwareLicenses.expiryDate less LocalDate.now())
            }
            .orderBy(SoftwareLicenses.expiryDate to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findExpiringInDays(days: Int): List<SoftwareLicenseDto> = dbQuery {
        val targetDate = LocalDate.now().plusDays(days.toLong())
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where {
                (SoftwareLicenses.expiryDate.isNotNull()) and
                        (SoftwareLicenses.expiryDate greaterEq LocalDate.now()) and
                        (SoftwareLicenses.expiryDate lessEq targetDate)
            }
            .orderBy(SoftwareLicenses.expiryDate to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByDateRange(startDate: String, endDate: String): List<SoftwareLicenseDto> = dbQuery {
        SoftwareLicenses
            .join(Vendors, JoinType.LEFT, SoftwareLicenses.vendorId, Vendors.id)
            .join(Users, JoinType.LEFT, SoftwareLicenses.assignedTo, Users.id)
            .selectAll()
            .where {
                (SoftwareLicenses.expiryDate.isNotNull()) and
                        (SoftwareLicenses.expiryDate greaterEq LocalDate.parse(startDate)) and
                        (SoftwareLicenses.expiryDate lessEq LocalDate.parse(endDate))
            }
            .orderBy(SoftwareLicenses.expiryDate to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun updateSeatUsage(id: Int, seatsUsed: Int): Boolean = dbQuery {
        SoftwareLicenses.update({ SoftwareLicenses.id eq id }) {
            it[SoftwareLicenses.seatsUsed] = seatsUsed
        } > 0
    }

    suspend fun deleteByVendorId(vendorId: Int): Int = dbQuery {
        SoftwareLicenses.deleteWhere { SoftwareLicenses.vendorId eq vendorId }
    }

    suspend fun getSoftwareLicenseStats(): SoftwareLicenseStatsDto = dbQuery {
        val allLicenses = SoftwareLicenses.selectAll().toList()
        val now = LocalDate.now()
        val in30Days = now.plusDays(30)

        val activeLicenses = allLicenses.count { it[SoftwareLicenses.status] == "ACTIVE" }
        val expiredLicenses = allLicenses.count {
            val expiryDate = it[SoftwareLicenses.expiryDate]
            expiryDate != null && expiryDate.isBefore(now)
        }
        val expiringIn30Days = allLicenses.count {
            val expiryDate = it[SoftwareLicenses.expiryDate]
            expiryDate != null && !expiryDate.isBefore(now) && !expiryDate.isAfter(in30Days)
        }

        val totalSeats = allLicenses.mapNotNull { it[SoftwareLicenses.numberOfSeats] }.sum()
        val usedSeats = allLicenses.sumOf { it[SoftwareLicenses.seatsUsed] }
        val availableSeats = totalSeats - usedSeats

        val licensesByStatus = allLicenses.groupBy { it[SoftwareLicenses.status] }
            .mapValues { it.value.size }

        val licensesByVendor = allLicenses.mapNotNull { license ->
            license[SoftwareLicenses.vendorId]?.let { vendorId ->
                Vendors.selectAll().where { Vendors.id eq vendorId }
                    .singleOrNull()?.get(Vendors.name)
            }
        }.groupBy { it }.mapValues { it.value.size }

        val upcomingExpirations = findExpiringInDays(30)

        SoftwareLicenseStatsDto(
            totalLicenses = allLicenses.size,
            activeLicenses = activeLicenses,
            expiredLicenses = expiredLicenses,
            expiringIn30Days = expiringIn30Days,
            totalSeats = totalSeats,
            usedSeats = usedSeats,
            availableSeats = availableSeats,
            licensesByStatus = licensesByStatus,
            licensesByVendor = licensesByVendor,
            upcomingExpirations = upcomingExpirations
        )
    }

    private fun mapRowToDto(row: ResultRow): SoftwareLicenseDto {
        val numberOfSeats = row[SoftwareLicenses.numberOfSeats]
        val seatsUsed = row[SoftwareLicenses.seatsUsed]
        val expiryDate = row[SoftwareLicenses.expiryDate]
        val now = LocalDate.now()

        return SoftwareLicenseDto(
            id = row[SoftwareLicenses.id],
            name = row[SoftwareLicenses.name],
            vendorId = row[SoftwareLicenses.vendorId],
            licenseKey = row[SoftwareLicenses.licenseKey],
            startDate = row[SoftwareLicenses.startDate]?.toString(),
            expiryDate = expiryDate?.toString(),
            numberOfSeats = numberOfSeats,
            seatsUsed = seatsUsed,
            assignedTo = row[SoftwareLicenses.assignedTo],
            notes = row[SoftwareLicenses.notes],
            status = row[SoftwareLicenses.status],
            createdAt = row[SoftwareLicenses.createdAt].toString(),
            vendorName = row.getOrNull(Vendors.name),
            vendorContactEmail = row.getOrNull(Vendors.contactEmail),
            assignedToName = "${row.getOrNull(Users.firstName) ?: ""} ${row.getOrNull(Users.lastName) ?: ""}".trim().takeIf { it.isNotEmpty() },
            assignedToEmail = row.getOrNull(Users.email),
            availableSeats = numberOfSeats?.let { total -> total - seatsUsed },
            isExpired = expiryDate?.isBefore(now),
            daysUntilExpiry = expiryDate?.let {
                if (it.isBefore(now)) 0L else java.time.temporal.ChronoUnit.DAYS.between(now, it)
            }
        )
    }
}