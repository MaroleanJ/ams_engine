package com.techbros.repositories

import com.techbros.database.tables.Locations
import com.techbros.models.dto.CreateLocationRequest
import com.techbros.models.dto.LocationDto
import com.techbros.models.dto.UpdateLocationRequest
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class LocationRepository {

    suspend fun create(request: CreateLocationRequest): Int = dbQuery {
        Locations.insert {
            it[name] = request.name
            it[description] = request.description
            it[address] = request.address
            it[parentLocationId] = request.parentLocationId
        }[Locations.id]
    }

    suspend fun findById(id: Int): LocationDto? = dbQuery {
        Locations.selectAll()
            .where { Locations.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<LocationDto> = dbQuery {
        Locations.selectAll()
            .orderBy(Locations.createdAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByParentId(parentId: Int?): List<LocationDto> = dbQuery {
        if (parentId == null) {
            Locations.selectAll()
                .where { Locations.parentLocationId.isNull() }
                .orderBy(Locations.name to SortOrder.ASC)
                .map { mapRowToDto(it) }
        } else {
            Locations.selectAll()
                .where { Locations.parentLocationId eq parentId }
                .orderBy(Locations.name to SortOrder.ASC)
                .map { mapRowToDto(it) }
        }
    }

    suspend fun findRootLocations(): List<LocationDto> = dbQuery {
        Locations.selectAll()
            .where { Locations.parentLocationId.isNull() }
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateLocationRequest): Boolean = dbQuery {
        Locations.update({ Locations.id eq id }) {
            it[name] = request.name
            it[description] = request.description
            it[address] = request.address
            it[parentLocationId] = request.parentLocationId
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Locations.deleteWhere { Locations.id eq id } > 0
    }

    suspend fun findByName(name: String): List<LocationDto> = dbQuery {
        Locations.selectAll()
            .where { Locations.name.lowerCase() like "%${name.lowercase()}%" }
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun nameExists(name: String, excludeId: Int? = null): Boolean = dbQuery {
        val query = Locations.selectAll()
            .where { Locations.name.lowerCase() eq name.lowercase() }

        if (excludeId != null) {
            query.andWhere { Locations.id neq excludeId }
        } else {
            query
        }.count() > 0
    }

    suspend fun locationExists(id: Int): Boolean = dbQuery {
        Locations.selectAll()
            .where { Locations.id eq id }
            .count() > 0
    }

    suspend fun hasChildren(id: Int): Boolean = dbQuery {
        Locations.selectAll()
            .where { Locations.parentLocationId eq id }
            .count() > 0
    }

    suspend fun getAllChildren(parentId: Int): List<LocationDto> = dbQuery {
        Locations.selectAll()
            .where { Locations.parentLocationId eq parentId }
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    private fun mapRowToDto(row: ResultRow): LocationDto {
        return LocationDto(
            id = row[Locations.id],
            name = row[Locations.name],
            description = row[Locations.description],
            address = row[Locations.address],
            parentLocationId = row[Locations.parentLocationId],
            createdAt = row[Locations.createdAt].toString()
        )
    }
}