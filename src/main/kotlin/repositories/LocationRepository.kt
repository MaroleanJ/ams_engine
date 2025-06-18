package com.techbros.repositories

import com.techbros.database.tables.Locations
import com.techbros.models.dto.CreateLocationRequest
import com.techbros.models.dto.LocationDto
import com.techbros.models.dto.LocationHierarchyDto
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
        val parentAlias = Locations.alias("parent")

        Locations.leftJoin(parentAlias, { Locations.parentLocationId }, { parentAlias[Locations.id] })
            .selectAll()
            .where { Locations.id eq id }
            .map { mapRowToDto(it, parentAlias) }
            .singleOrNull()
    }

    suspend fun findAll(): List<LocationDto> = dbQuery {
        val parentAlias = Locations.alias("parent")

        Locations.leftJoin(parentAlias, { Locations.parentLocationId }, { parentAlias[Locations.id] })
            .selectAll()
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it, parentAlias) }
    }

    suspend fun findByParentId(parentId: Int?): List<LocationDto> = dbQuery {
        val parentAlias = Locations.alias("parent")

        Locations.leftJoin(parentAlias, { Locations.parentLocationId }, { parentAlias[Locations.id] })
            .selectAll()
            .where {
                if (parentId == null) {
                    Locations.parentLocationId.isNull()
                } else {
                    Locations.parentLocationId eq parentId
                }
            }
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it, parentAlias) }
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
        // First check if location has children
        val hasChildren = Locations.selectAll()
            .where { Locations.parentLocationId eq id }
            .count() > 0

        if (hasChildren) {
            false // Cannot delete location with children
        } else {
            Locations.deleteWhere { Locations.id eq id } > 0
        }
    }

    suspend fun searchByName(searchTerm: String): List<LocationDto> = dbQuery {
        val parentAlias = Locations.alias("parent")

        Locations.leftJoin(parentAlias, { Locations.parentLocationId }, { parentAlias[Locations.id] })
            .selectAll()
            .where { Locations.name.lowerCase() like "%${searchTerm.lowercase()}%" }
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToDto(it, parentAlias) }
    }

    suspend fun getLocationHierarchy(): List<LocationHierarchyDto> = dbQuery {
        val allLocations = Locations.selectAll()
            .orderBy(Locations.name to SortOrder.ASC)
            .map { mapRowToHierarchyDto(it) }

        buildHierarchy(allLocations)
    }

    private fun mapRowToDto(row: ResultRow, parentAlias: Alias<Table>? = null): LocationDto {
        return LocationDto(
            id = row[Locations.id],
            name = row[Locations.name],
            description = row[Locations.description],
            address = row[Locations.address],
            parentLocationId = row[Locations.parentLocationId],
            parentLocationName = parentAlias?.let { row.getOrNull(it[Locations.name]) },
            createdAt = row[Locations.createdAt].toString()
        )
    }

    private fun mapRowToHierarchyDto(row: ResultRow): LocationHierarchyDto {
        return LocationHierarchyDto(
            id = row[Locations.id],
            name = row[Locations.name],
            description = row[Locations.description],
            address = row[Locations.address],
            parentLocationId = row[Locations.parentLocationId],
            createdAt = row[Locations.createdAt].toString()
        )
    }

    private fun buildHierarchy(locations: List<LocationHierarchyDto>): List<LocationHierarchyDto> {
        val locationMap = locations.associateBy { it.id }
        val rootLocations = mutableListOf<LocationHierarchyDto>()

        locations.forEach { location ->
            if (location.parentLocationId == null) {
                rootLocations.add(location.copy(children = getChildren(location.id, locationMap)))
            }
        }

        return rootLocations
    }

    private fun getChildren(parentId: Int, locationMap: Map<Int, LocationHierarchyDto>): List<LocationHierarchyDto> {
        return locationMap.values
            .filter { it.parentLocationId == parentId }
            .map { it.copy(children = getChildren(it.id, locationMap)) }
            .sortedBy { it.name }
    }
}