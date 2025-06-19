package com.techbros.repositories

import com.techbros.database.tables.MaintenanceTypes
import com.techbros.models.dto.CreateMaintenanceTypeRequest
import com.techbros.models.dto.MaintenanceTypeDto
import com.techbros.models.dto.UpdateMaintenanceTypeRequest
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import java.math.BigDecimal

class MaintenanceTypeRepository {

    suspend fun create(request: CreateMaintenanceTypeRequest): Int = dbQuery {
        MaintenanceTypes.insert {
            it[name] = request.name
            it[description] = request.description
            it[estimatedDurationHours] = request.estimatedDurationHours
            it[costEstimate] = request.costEstimate?.let { cost -> BigDecimal(cost) }
        }[MaintenanceTypes.id]
    }

    suspend fun findById(id: Int): MaintenanceTypeDto? = dbQuery {
        MaintenanceTypes.selectAll()
            .where { MaintenanceTypes.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<MaintenanceTypeDto> = dbQuery {
        MaintenanceTypes.selectAll()
            .orderBy(MaintenanceTypes.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByName(name: String): MaintenanceTypeDto? = dbQuery {
        MaintenanceTypes.selectAll()
            .where { MaintenanceTypes.name eq name }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findByNameContaining(namePattern: String): List<MaintenanceTypeDto> = dbQuery {
        MaintenanceTypes.selectAll()
            .where { MaintenanceTypes.name.lowerCase() like "%${namePattern.lowercase()}%" }
            .orderBy(MaintenanceTypes.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByDurationRange(minHours: Int?, maxHours: Int?): List<MaintenanceTypeDto> = dbQuery {
        val query = MaintenanceTypes.selectAll()

        val conditions = mutableListOf<Op<Boolean>>()
        
        minHours?.let { min ->
            conditions.add(MaintenanceTypes.estimatedDurationHours greaterEq min)
        }
        
        maxHours?.let { max ->
            conditions.add(MaintenanceTypes.estimatedDurationHours lessEq max)
        }

        if (conditions.isNotEmpty()) {
            query.where { conditions.reduce { acc, condition -> acc and condition } }
        }

        query.orderBy(MaintenanceTypes.estimatedDurationHours to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByCostRange(minCost: BigDecimal?, maxCost: BigDecimal?): List<MaintenanceTypeDto> = dbQuery {
        val query = MaintenanceTypes.selectAll()

        val conditions = mutableListOf<Op<Boolean>>()
        
        minCost?.let { min ->
            conditions.add(MaintenanceTypes.costEstimate greaterEq min)
        }
        
        maxCost?.let { max ->
            conditions.add(MaintenanceTypes.costEstimate lessEq max)
        }

        if (conditions.isNotEmpty()) {
            query.where { conditions.reduce { acc, condition -> acc and condition } }
        }

        query.orderBy(MaintenanceTypes.costEstimate to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateMaintenanceTypeRequest): Boolean = dbQuery {
        MaintenanceTypes.update({ MaintenanceTypes.id eq id }) {
            it[name] = request.name
            it[description] = request.description
            it[estimatedDurationHours] = request.estimatedDurationHours
            it[costEstimate] = request.costEstimate?.let { cost -> BigDecimal(cost) }
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        MaintenanceTypes.deleteWhere { MaintenanceTypes.id eq id } > 0
    }

    suspend fun existsByName(name: String, excludeId: Int? = null): Boolean = dbQuery {
        val query = MaintenanceTypes.selectAll().where { MaintenanceTypes.name eq name }
        
        excludeId?.let { id ->
            query.andWhere { MaintenanceTypes.id neq id }
        }
        
        query.count() > 0
    }

    private fun mapRowToDto(row: ResultRow): MaintenanceTypeDto {
        return MaintenanceTypeDto(
            id = row[MaintenanceTypes.id],
            name = row[MaintenanceTypes.name],
            description = row[MaintenanceTypes.description],
            estimatedDurationHours = row[MaintenanceTypes.estimatedDurationHours],
            costEstimate = row[MaintenanceTypes.costEstimate]?.toString()
        )
    }
}

// ===== SERVICE =====