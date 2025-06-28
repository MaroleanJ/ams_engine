package com.techbros.repositories

import com.techbros.database.tables.Assets
import com.techbros.database.tables.MaintenanceRecords
import com.techbros.database.tables.MaintenanceSchedules
import com.techbros.database.tables.Users
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDate

class MaintenanceRecordRepository {

    suspend fun create(request: CreateMaintenanceRecordRequest): Int = dbQuery {
        MaintenanceRecords.insert {
            it[assetId] = request.assetId
            it[scheduleId] = request.scheduleId
            it[performedBy] = request.performedBy
            it[maintenanceType] = request.maintenanceType
            it[performedDate] = LocalDate.parse(request.performedDate)
            it[durationHours] = request.durationHours?.let { BigDecimal(it) }
            it[cost] = request.cost?.let { BigDecimal(it) }
            it[description] = request.description
            it[partsReplaced] = request.partsReplaced
            it[nextMaintenanceDue] = request.nextMaintenanceDue?.let { LocalDate.parse(it) }
            it[status] = request.status
        }[MaintenanceRecords.id]
    }

    suspend fun bulkCreate(requests: List<CreateMaintenanceRecordRequest>): List<Int> = dbQuery {
        requests.map { request ->
            MaintenanceRecords.insert {
                it[assetId] = request.assetId
                it[scheduleId] = request.scheduleId
                it[performedBy] = request.performedBy
                it[maintenanceType] = request.maintenanceType
                it[performedDate] = LocalDate.parse(request.performedDate)
                it[durationHours] = request.durationHours?.let { BigDecimal(it) }
                it[cost] = request.cost?.let { BigDecimal(it) }
                it[description] = request.description
                it[partsReplaced] = request.partsReplaced
                it[nextMaintenanceDue] = request.nextMaintenanceDue?.let { LocalDate.parse(it) }
                it[status] = request.status
            }[MaintenanceRecords.id]
        }
    }

    suspend fun findById(id: Int): MaintenanceRecordDto? = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateMaintenanceRecordRequest): Boolean = dbQuery {
        MaintenanceRecords.update({ MaintenanceRecords.id eq id }) {
            it[assetId] = request.assetId
            it[scheduleId] = request.scheduleId
            it[performedBy] = request.performedBy
            it[maintenanceType] = request.maintenanceType
            it[performedDate] = LocalDate.parse(request.performedDate)
            it[durationHours] = request.durationHours?.let { BigDecimal(it) }
            it[cost] = request.cost?.let { BigDecimal(it) }
            it[description] = request.description
            it[partsReplaced] = request.partsReplaced
            it[nextMaintenanceDue] = request.nextMaintenanceDue?.let { LocalDate.parse(it) }
            it[status] = request.status
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        MaintenanceRecords.deleteWhere { MaintenanceRecords.id eq id } > 0
    }

    suspend fun findByAssetId(assetId: Int): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.assetId eq assetId }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByPerformedBy(performedBy: Int): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.performedBy eq performedBy }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByScheduleId(scheduleId: Int): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.scheduleId eq scheduleId }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByMaintenanceType(maintenanceType: String): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.maintenanceType eq maintenanceType }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByStatus(status: String): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where { MaintenanceRecords.status eq status }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByDateRange(startDate: String, endDate: String): List<MaintenanceRecordDto> = dbQuery {
        MaintenanceRecords
            .join(Assets, JoinType.LEFT, MaintenanceRecords.assetId, Assets.id)
            .join(Users, JoinType.LEFT, MaintenanceRecords.performedBy, Users.id)
            .join(MaintenanceSchedules, JoinType.LEFT, MaintenanceRecords.scheduleId, MaintenanceSchedules.id)
            .selectAll()
            .where {
                (MaintenanceRecords.performedDate greaterEq LocalDate.parse(startDate)) and
                        (MaintenanceRecords.performedDate lessEq LocalDate.parse(endDate))
            }
            .orderBy(MaintenanceRecords.performedDate to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun deleteByAssetId(assetId: Int): Int = dbQuery {
        MaintenanceRecords.deleteWhere { MaintenanceRecords.assetId eq assetId }
    }

    suspend fun deleteByScheduleId(scheduleId: Int): Int = dbQuery {
        MaintenanceRecords.deleteWhere { MaintenanceRecords.scheduleId eq scheduleId }
    }

    suspend fun getAssetMaintenanceHistory(assetId: Int): AssetMaintenanceHistoryDto? = dbQuery {
        val records = findByAssetId(assetId)
        if (records.isEmpty()) return@dbQuery null

        val asset = Assets.selectAll()
            .where { Assets.id eq assetId }
            .singleOrNull()
            ?: return@dbQuery null

        val totalCost = records.mapNotNull { it.cost?.toBigDecimalOrNull() }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        AssetMaintenanceHistoryDto(
            assetId = assetId,
            assetName = asset[Assets.name],
            assetSerialNumber = asset[Assets.serialNumber],
            totalMaintenanceRecords = records.size,
            totalCost = if (totalCost > BigDecimal.ZERO) totalCost.toString() else null,
            lastMaintenanceDate = records.firstOrNull()?.performedDate,
            records = records
        )
    }

    suspend fun getMaintenanceStats(): MaintenanceStatsDto = dbQuery {
        val allRecords = MaintenanceRecords.selectAll().toList()

        val totalCost = allRecords.mapNotNull { it[MaintenanceRecords.cost] }
            .fold(BigDecimal.ZERO) { acc, cost -> acc + cost }

        val recordsByStatus = allRecords.groupBy { it[MaintenanceRecords.status] }
            .mapValues { it.value.size }

        val recordsByType = allRecords.groupBy { it[MaintenanceRecords.maintenanceType] }
            .mapValues { it.value.size }

        val recordsByMonth = allRecords.groupBy {
            "${it[MaintenanceRecords.performedDate].year}-${it[MaintenanceRecords.performedDate].monthValue.toString().padStart(2, '0')}"
        }.mapValues { it.value.size }

        MaintenanceStatsDto(
            totalRecords = allRecords.size,
            totalCost = if (totalCost > BigDecimal.ZERO) totalCost.toString() else null,
            averageCost = if (totalCost > BigDecimal.ZERO && allRecords.isNotEmpty())
                (totalCost / BigDecimal(allRecords.size)).toString() else null,
            recordsByStatus = recordsByStatus,
            recordsByType = recordsByType,
            recordsByMonth = recordsByMonth
        )
    }

    private fun mapRowToDto(row: ResultRow): MaintenanceRecordDto {
        return MaintenanceRecordDto(
            id = row[MaintenanceRecords.id],
            assetId = row[MaintenanceRecords.assetId],
            scheduleId = row[MaintenanceRecords.scheduleId],
            performedBy = row[MaintenanceRecords.performedBy],
            maintenanceType = row[MaintenanceRecords.maintenanceType],
            performedDate = row[MaintenanceRecords.performedDate].toString(),
            durationHours = row[MaintenanceRecords.durationHours]?.toString(),
            cost = row[MaintenanceRecords.cost]?.toString(),
            description = row[MaintenanceRecords.description],
            partsReplaced = row[MaintenanceRecords.partsReplaced],
            nextMaintenanceDue = row[MaintenanceRecords.nextMaintenanceDue]?.toString(),
            status = row[MaintenanceRecords.status],
            createdAt = row[MaintenanceRecords.createdAt].toString(),
            assetName = row.getOrNull(Assets.name),
            assetSerialNumber = row.getOrNull(Assets.serialNumber),
            performedByName = "${row.getOrNull(Users.firstName) ?: ""} ${row.getOrNull(Users.lastName) ?: ""}".trim(),
            performedByEmail = row.getOrNull(Users.email),
            scheduleName = row.getOrNull(MaintenanceSchedules.maintenanceType)
        )
    }
}