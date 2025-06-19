package com.techbros.repositories

import com.techbros.database.tables.AssetHistory
import com.techbros.database.tables.Users
import com.techbros.models.dto.AssetHistoryDto
import com.techbros.models.dto.CreateAssetHistoryRequest
import com.techbros.models.dto.UpdateAssetHistoryRequest
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

class AssetHistoryRepository {

    suspend fun create(request: CreateAssetHistoryRequest): Int = dbQuery {
        AssetHistory.insert {
            it[assetId] = request.assetId
            it[changedBy] = request.changedBy
            it[actionType] = request.actionType
            it[fieldChanged] = request.fieldChanged
            it[oldValue] = request.oldValue
            it[newValue] = request.newValue
            it[description] = request.description
        }[AssetHistory.id]
    }

    suspend fun findById(id: Int): AssetHistoryDto? = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .where { AssetHistory.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<AssetHistoryDto> = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .orderBy(AssetHistory.changedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssetId(assetId: Int): List<AssetHistoryDto> = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .where { AssetHistory.assetId eq assetId }
            .orderBy(AssetHistory.changedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByUser(userId: Int): List<AssetHistoryDto> = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .where { AssetHistory.changedBy eq userId }
            .orderBy(AssetHistory.changedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByActionType(actionType: String): List<AssetHistoryDto> = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .where { AssetHistory.actionType eq actionType }
            .orderBy(AssetHistory.changedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByDateRange(startDate: Instant, endDate: Instant): List<AssetHistoryDto> = dbQuery {
        AssetHistory.leftJoin(Users)
            .selectAll()
            .where {
                (AssetHistory.changedAt greaterEq startDate) and
                        (AssetHistory.changedAt lessEq endDate)
            }
            .orderBy(AssetHistory.changedAt to SortOrder.DESC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateAssetHistoryRequest): Boolean = dbQuery {
        AssetHistory.update({ AssetHistory.id eq id }) {
            it[actionType] = request.actionType
            it[fieldChanged] = request.fieldChanged
            it[oldValue] = request.oldValue
            it[newValue] = request.newValue
            it[description] = request.description
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        AssetHistory.deleteWhere { AssetHistory.id eq id } > 0
    }

    suspend fun deleteByAssetId(assetId: Int): Int = dbQuery {
        AssetHistory.deleteWhere { AssetHistory.assetId eq assetId }
    }

    private fun mapRowToDto(row: ResultRow): AssetHistoryDto {
        val firstName = row.getOrNull(Users.firstName)
        val lastName = row.getOrNull(Users.lastName)
        val changedByName = when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> null
        }

        return AssetHistoryDto(
            id = row[AssetHistory.id],
            assetId = row[AssetHistory.assetId],
            changedBy = row[AssetHistory.changedBy],
            changedByName = changedByName,
            actionType = row[AssetHistory.actionType],
            fieldChanged = row[AssetHistory.fieldChanged],
            oldValue = row[AssetHistory.oldValue],
            newValue = row[AssetHistory.newValue],
            description = row[AssetHistory.description],
            changedAt = row[AssetHistory.changedAt].toString()
        )
    }
}