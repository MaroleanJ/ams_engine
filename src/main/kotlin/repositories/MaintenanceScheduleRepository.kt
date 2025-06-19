package com.techbros.repositories

import com.techbros.database.tables.*
import com.techbros.models.dto.CreateMaintenanceScheduleRequest
import com.techbros.models.dto.MaintenanceScheduleDto
import com.techbros.models.dto.MaintenanceScheduleSummaryDto
import com.techbros.models.dto.UpdateMaintenanceScheduleRequest
import com.techbros.database.tables.MaintenanceSchedules
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDate

class MaintenanceScheduleRepository {

    suspend fun create(request: CreateMaintenanceScheduleRequest): Int = dbQuery {
        MaintenanceSchedules.insert {
            it[assetId] = request.assetId
            it[maintenanceTypeId] = request.maintenanceTypeId
            it[maintenanceType] = request.maintenanceType
            it[frequencyDays] = request.frequencyDays
            it[lastPerformed] = request.lastPerformed?.let { date -> LocalDate.parse(date) }
            it[nextDue] = LocalDate.parse(request.nextDue)
            it[assignedTo] = request.assignedTo
            it[priority] = request.priority
            it[estimatedCost] = request.estimatedCost?.let { cost -> BigDecimal(cost) }
            it[notes] = request.notes
            it[isActive] = request.isActive
        }[MaintenanceSchedules.id]
    }

    suspend fun findById(id: Int): MaintenanceScheduleDto? = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users)
            .selectAll()
            .where { MaintenanceSchedules.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssetId(assetId: Int): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { MaintenanceSchedules.assetId eq assetId }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssignedUser(userId: Int): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { MaintenanceSchedules.assignedTo eq userId }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByPriority(priority: String): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { MaintenanceSchedules.priority eq priority }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findActiveSchedules(): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { MaintenanceSchedules.isActive eq true }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findOverdueSchedules(): List<MaintenanceScheduleDto> = dbQuery {
        val today = LocalDate.now()
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { 
                (MaintenanceSchedules.nextDue less today) and
                (MaintenanceSchedules.isActive eq true)
            }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findDueInRange(startDate: LocalDate, endDate: LocalDate): List<MaintenanceScheduleDto> = dbQuery {
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { 
                (MaintenanceSchedules.nextDue greaterEq startDate) and
                (MaintenanceSchedules.nextDue lessEq endDate) and
                (MaintenanceSchedules.isActive eq true)
            }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findDueToday(): List<MaintenanceScheduleDto> = dbQuery {
        val today = LocalDate.now()
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { 
                (MaintenanceSchedules.nextDue eq today) and
                (MaintenanceSchedules.isActive eq true)
            }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findDueThisWeek(): List<MaintenanceScheduleDto> = dbQuery {
        val today = LocalDate.now()
        val weekEnd = today.plusDays(7)
        MaintenanceSchedules
            .leftJoin(Assets)
            .leftJoin(MaintenanceTypes)
            .leftJoin(Users, { MaintenanceSchedules.assignedTo }, { Users.id })
            .selectAll()
            .where { 
                (MaintenanceSchedules.nextDue greaterEq today) and
                (MaintenanceSchedules.nextDue lessEq weekEnd) and
                (MaintenanceSchedules.isActive eq true)
            }
            .orderBy(MaintenanceSchedules.nextDue to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun getScheduleSummary(): MaintenanceScheduleSummaryDto = dbQuery {
        val today = LocalDate.now()
        val weekEnd = today.plusDays(7)

        val totalSchedules = MaintenanceSchedules.selectAll().count()
        val activeSchedules = MaintenanceSchedules.selectAll()
            .where { MaintenanceSchedules.isActive eq true }.count()
        val overdueSchedules = MaintenanceSchedules.selectAll()
            .where { 
                (MaintenanceSchedules.nextDue less today) and
                (MaintenanceSchedules.isActive eq true)
            }.count()
        val dueTodaySchedules = MaintenanceSchedules.selectAll()
            .where { 
                (MaintenanceSchedules.nextDue eq today) and
                (MaintenanceSchedules.isActive eq true)
            }.count()
        val dueThisWeekSchedules = MaintenanceSchedules.selectAll()
            .where { 
                (MaintenanceSchedules.nextDue greaterEq today) and
                (MaintenanceSchedules.nextDue lessEq weekEnd) and
                (MaintenanceSchedules.isActive eq true)
            }.count()

        val totalEstimatedCost = MaintenanceSchedules
            .select(MaintenanceSchedules.estimatedCost.sum())
            .where { MaintenanceSchedules.isActive eq true }
            .singleOrNull()?.get(MaintenanceSchedules.estimatedCost.sum())

        MaintenanceScheduleSummaryDto(
            totalSchedules = totalSchedules.toInt(),
            activeSchedules = activeSchedules.toInt(),
            overdueSchedules = overdueSchedules.toInt(),
            dueTodaySchedules = dueTodaySchedules.toInt(),
            dueThisWeekSchedules = dueThisWeekSchedules.toInt(),
            totalEstimatedCost = totalEstimatedCost?.toString()
        )
    }

    suspend fun update(id: Int, request: UpdateMaintenanceScheduleRequest): Boolean = dbQuery {
        MaintenanceSchedules.update({ MaintenanceSchedules.id eq id }) {
            it[maintenanceTypeId] = request.maintenanceTypeId
            it[maintenanceType] = request.maintenanceType
            it[frequencyDays] = request.frequencyDays
            it[lastPerformed] = request.lastPerformed?.let { date -> LocalDate.parse(date) }
            it[nextDue] = LocalDate.parse(request.nextDue)
            it[assignedTo] = request.assignedTo
            it[priority] = request.priority
            it[estimatedCost] = request.estimatedCost?.let { cost -> BigDecimal(cost) }
            it[notes] = request.notes
            it[isActive] = request.isActive
        } > 0
    }

    suspend fun updateNextDueDate(id: Int, nextDue: LocalDate, lastPerformed: LocalDate? = null): Boolean = dbQuery {
        MaintenanceSchedules.update({ MaintenanceSchedules.id eq id }) {
            it[MaintenanceSchedules.nextDue] = nextDue
            lastPerformed?.let { date -> it[MaintenanceSchedules.lastPerformed] = date }
        } > 0
    }

    suspend fun deactivateSchedule(id: Int): Boolean = dbQuery {
        MaintenanceSchedules.update({ MaintenanceSchedules.id eq id }) {
            it[isActive] = false
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        MaintenanceSchedules.deleteWhere { MaintenanceSchedules.id eq id } > 0
    }

    private fun mapRowToDto(row: ResultRow): MaintenanceScheduleDto {
        val firstName = row.getOrNull(Users.firstName)
        val lastName = row.getOrNull(Users.lastName)
        val assignedToName = when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> null
        }

        val nextDueDate = row[MaintenanceSchedules.nextDue]
        val today = LocalDate.now()
        val isOverdue = nextDueDate.isBefore(today)
        val daysUntilDue = today.until(nextDueDate).days.toInt()

        return MaintenanceScheduleDto(
            id = row[MaintenanceSchedules.id],
            assetId = row[MaintenanceSchedules.assetId],
            assetName = row.getOrNull(Assets.name),
            maintenanceTypeId = row[MaintenanceSchedules.maintenanceTypeId],
            maintenanceType = row[MaintenanceSchedules.maintenanceType],
            maintenanceTypeName = row.getOrNull(MaintenanceTypes.name),
            frequencyDays = row[MaintenanceSchedules.frequencyDays],
            lastPerformed = row[MaintenanceSchedules.lastPerformed]?.toString(),
            nextDue = row[MaintenanceSchedules.nextDue].toString(),
            assignedTo = row[MaintenanceSchedules.assignedTo],
            assignedToName = assignedToName,
            priority = row[MaintenanceSchedules.priority],
            estimatedCost = row[MaintenanceSchedules.estimatedCost]?.toString(),
            notes = row[MaintenanceSchedules.notes],
            isActive = row[MaintenanceSchedules.isActive],
            createdAt = row[MaintenanceSchedules.createdAt].toString(),
            isOverdue = isOverdue,
            daysUntilDue = if (isOverdue) null else daysUntilDue
        )
    }
}
