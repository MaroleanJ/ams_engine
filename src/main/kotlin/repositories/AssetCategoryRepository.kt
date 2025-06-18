package com.techbros.repositories

import com.techbros.database.tables.AssetCategories
import com.techbros.models.dto.AssetCategoryDto
import com.techbros.models.dto.CreateAssetCategoryRequest
import com.techbros.models.dto.UpdateAssetCategoryRequest
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class AssetCategoryRepository {

    suspend fun create(request: CreateAssetCategoryRequest): Int = dbQuery {
        AssetCategories.insert {
            it[name] = request.name
            it[description] = request.description
        }[AssetCategories.id]
    }

    suspend fun findById(id: Int): AssetCategoryDto? = dbQuery {
        AssetCategories.selectAll()
            .where { AssetCategories.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findByName(name: String): AssetCategoryDto? = dbQuery {
        AssetCategories.selectAll()
            .where { AssetCategories.name eq name }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<AssetCategoryDto> = dbQuery {
        AssetCategories.selectAll()
            .orderBy(AssetCategories.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateAssetCategoryRequest): Boolean = dbQuery {
        AssetCategories.update({ AssetCategories.id eq id }) {
            it[name] = request.name
            it[description] = request.description
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        AssetCategories.deleteWhere { AssetCategories.id eq id } > 0
    }

    suspend fun searchByName(searchTerm: String): List<AssetCategoryDto> = dbQuery {
        AssetCategories.selectAll()
            .where { AssetCategories.name.lowerCase() like "%${searchTerm.lowercase()}%" }
            .orderBy(AssetCategories.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun exists(id: Int): Boolean = dbQuery {
        AssetCategories.selectAll()
            .where { AssetCategories.id eq id }
            .count() > 0
    }

    suspend fun existsByName(name: String, excludeId: Int? = null): Boolean = dbQuery {
        val query = AssetCategories.selectAll()
            .where { AssetCategories.name eq name }

        if (excludeId != null) {
            query.andWhere { AssetCategories.id neq excludeId }
        } else {
            query
        }.count() > 0
    }

    suspend fun getCount(): Long = dbQuery {
        AssetCategories.selectAll().count()
    }

    private fun mapRowToDto(row: ResultRow): AssetCategoryDto {
        return AssetCategoryDto(
            id = row[AssetCategories.id],
            name = row[AssetCategories.name],
            description = row[AssetCategories.description]
        )
    }
}