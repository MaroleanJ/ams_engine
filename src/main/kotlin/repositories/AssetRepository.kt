package com.techbros.repositories

import com.techbros.database.tables.AssetCategories
import com.techbros.database.tables.Assets
import com.techbros.database.tables.Locations
import com.techbros.database.tables.Users
import com.techbros.database.tables.Vendors
import com.techbros.models.dto.AssetDetailDto
import com.techbros.models.dto.AssetDto
import com.techbros.models.dto.AssetSummaryDto
import com.techbros.models.dto.CreateAssetRequest
import com.techbros.models.dto.UpdateAssetRequest
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AssetRepository {

    suspend fun create(request: CreateAssetRequest): Int = dbQuery {
        Assets.insert {
            it[categoryId] = request.categoryId
            it[vendorId] = request.vendorId
            it[locationId] = request.locationId
            it[name] = request.name
            it[model] = request.model
            it[serialNumber] = request.serialNumber
            it[barcode] = request.barcode
            it[purchaseDate] = request.purchaseDate?.let { LocalDate.parse(it) }
            it[purchasePrice] = request.purchasePrice?.let { BigDecimal(it) }
            it[currentValue] = request.currentValue?.let { BigDecimal(it) }
            it[depreciationRate] = request.depreciationRate?.let { BigDecimal(it) }
            it[warrantyExpiry] = request.warrantyExpiry?.let { LocalDate.parse(it) }
            it[assignedTo] = request.assignedTo
            it[status] = request.status
            it[location] = request.location
            it[notes] = request.notes
        }[Assets.id]
    }

    suspend fun findById(id: Int): AssetDto? = dbQuery {
        Assets.selectAll()
            .where { Assets.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findDetailById(id: Int): AssetDetailDto? = dbQuery {
        (Assets leftJoin AssetCategories leftJoin Vendors leftJoin Locations leftJoin Users)
            .selectAll()
            .where { Assets.id eq id }
            .map { mapRowToDetailDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findAllSummary(): List<AssetSummaryDto> = dbQuery {
        Assets.selectAll()
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToSummaryDto(it) }
    }

    suspend fun findByCategory(categoryId: Int): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.categoryId eq categoryId }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByVendor(vendorId: Int): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.vendorId eq vendorId }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByLocation(locationId: Int): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.locationId eq locationId }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByAssignedUser(userId: Int): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.assignedTo eq userId }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByStatus(status: String): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.status.lowerCase() eq status.lowercase() }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findBySerialNumber(serialNumber: String): AssetDto? = dbQuery {
        Assets.selectAll()
            .where { Assets.serialNumber eq serialNumber }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findByBarcode(barcode: String): AssetDto? = dbQuery {
        Assets.selectAll()
            .where { Assets.barcode eq barcode }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun searchByName(searchTerm: String): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.name.lowerCase() like "%${searchTerm.lowercase()}%" }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun searchByNameOrModel(searchTerm: String): List<AssetDto> = dbQuery {
        val term = "%${searchTerm.lowercase()}%"
        Assets.selectAll()
            .where {
                (Assets.name.lowerCase() like term) or
                        (Assets.model.lowerCase() like term)
            }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findAssetsWithWarrantyExpiring(daysFromNow: Long): List<AssetDto> = dbQuery {
        val targetDate = LocalDate.now().plusDays(daysFromNow)
        Assets.selectAll()
            .where {
                Assets.warrantyExpiry.isNotNull() and
                        (Assets.warrantyExpiry lessEq targetDate)
            }
            .orderBy(Assets.warrantyExpiry to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findUnassignedAssets(): List<AssetDto> = dbQuery {
        Assets.selectAll()
            .where { Assets.assignedTo.isNull() }
            .orderBy(Assets.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun update(id: Int, request: UpdateAssetRequest): Boolean = dbQuery {
        Assets.update({ Assets.id eq id }) {
            it[categoryId] = request.categoryId
            it[vendorId] = request.vendorId
            it[locationId] = request.locationId
            it[name] = request.name
            it[model] = request.model
            it[serialNumber] = request.serialNumber
            it[barcode] = request.barcode
            it[purchaseDate] = request.purchaseDate?.let { LocalDate.parse(it) }
            it[purchasePrice] = request.purchasePrice?.let { BigDecimal(it) }
            it[currentValue] = request.currentValue?.let { BigDecimal(it) }
            it[depreciationRate] = request.depreciationRate?.let { BigDecimal(it) }
            it[warrantyExpiry] = request.warrantyExpiry?.let { LocalDate.parse(it) }
            it[assignedTo] = request.assignedTo
            it[status] = request.status
            it[location] = request.location
            it[notes] = request.notes
            it[updatedAt] = java.time.Instant.now()
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Assets.deleteWhere { Assets.id eq id } > 0
    }

    suspend fun exists(id: Int): Boolean = dbQuery {
        Assets.selectAll()
            .where { Assets.id eq id }
            .count() > 0
    }

    suspend fun serialNumberExists(serialNumber: String, excludeId: Int? = null): Boolean = dbQuery {
        val query = Assets.selectAll().where { Assets.serialNumber eq serialNumber }
        if (excludeId != null) {
            query.andWhere { Assets.id neq excludeId }
        } else {
            query
        }.count() > 0
    }

    suspend fun barcodeExists(barcode: String, excludeId: Int? = null): Boolean = dbQuery {
        val query = Assets.selectAll().where { Assets.barcode eq barcode }
        if (excludeId != null) {
            query.andWhere { Assets.id neq excludeId }
        } else {
            query
        }.count() > 0
    }

    suspend fun getCount(): Long = dbQuery {
        Assets.selectAll().count()
    }

    suspend fun getCountByStatus(): Map<String, Long> = dbQuery {
        Assets.select(Assets.status, Assets.id.count())
            .groupBy(Assets.status)
            .associate { row ->
                (row[Assets.status] ?: "Unknown") to row[Assets.id.count()]
            }
    }

    suspend fun getCountByCategory(): Map<String, Long> = dbQuery {
        (Assets leftJoin AssetCategories)
            .select(AssetCategories.name, Assets.id.count())
            .groupBy(AssetCategories.name)
            .associate { row ->
                (row.getOrNull(AssetCategories.name) ?: "Unknown Category") to row[Assets.id.count()]
            }
    }

    suspend fun getUnassignedCount(): Long = dbQuery {
        Assets.selectAll()
            .where { Assets.assignedTo.isNull() }
            .count()
    }

    suspend fun getTotalValue(): BigDecimal = dbQuery {
        Assets.selectAll()
            .where { Assets.currentValue.isNotNull() }
            .sumOf { it[Assets.currentValue] ?: BigDecimal.ZERO }
    }

    private fun mapRowToDto(row: ResultRow): AssetDto {
        return AssetDto(
            id = row[Assets.id],
            categoryId = row[Assets.categoryId],
            vendorId = row[Assets.vendorId],
            locationId = row[Assets.locationId],
            name = row[Assets.name],
            model = row[Assets.model],
            serialNumber = row[Assets.serialNumber],
            barcode = row[Assets.barcode],
            purchaseDate = row[Assets.purchaseDate]?.toString(),
            purchasePrice = row[Assets.purchasePrice]?.toString(),
            currentValue = row[Assets.currentValue]?.toString(),
            depreciationRate = row[Assets.depreciationRate]?.toString(),
            warrantyExpiry = row[Assets.warrantyExpiry]?.toString(),
            assignedTo = row[Assets.assignedTo],
            status = row[Assets.status],
            location = row[Assets.location],
            notes = row[Assets.notes],
            createdAt = row[Assets.createdAt].toString(),
            updatedAt = row[Assets.updatedAt].toString()
        )
    }

    private fun mapRowToSummaryDto(row: ResultRow): AssetSummaryDto {
        return AssetSummaryDto(
            id = row[Assets.id],
            name = row[Assets.name],
            model = row[Assets.model],
            serialNumber = row[Assets.serialNumber],
            status = row[Assets.status],
            categoryId = row[Assets.categoryId],
            vendorId = row[Assets.vendorId],
            assignedTo = row[Assets.assignedTo],
            purchasePrice = row[Assets.purchasePrice]?.toString(),
            currentValue = row[Assets.currentValue]?.toString(),
            hasWarranty = row[Assets.warrantyExpiry] != null
        )
    }

    private fun mapRowToDetailDto(row: ResultRow): AssetDetailDto {
        return AssetDetailDto(
            id = row[Assets.id],
            categoryId = row[Assets.categoryId],
            categoryName = row.getOrNull(AssetCategories.name),
            vendorId = row[Assets.vendorId],
            vendorName = row.getOrNull(Vendors.name),
            locationId = row[Assets.locationId],
            locationName = row.getOrNull(Locations.name),
            name = row[Assets.name],
            model = row[Assets.model],
            serialNumber = row[Assets.serialNumber],
            barcode = row[Assets.barcode],
            purchaseDate = row[Assets.purchaseDate]?.toString(),
            purchasePrice = row[Assets.purchasePrice]?.toString(),
            currentValue = row[Assets.currentValue]?.toString(),
            depreciationRate = row[Assets.depreciationRate]?.toString(),
            warrantyExpiry = row[Assets.warrantyExpiry]?.toString(),
            assignedTo = row[Assets.assignedTo],
            assignedToName = row.getOrNull(Users.firstName),
            status = row[Assets.status],
            location = row[Assets.location],
            notes = row[Assets.notes],
            createdAt = row[Assets.createdAt].toString(),
            updatedAt = row[Assets.updatedAt].toString()
        )
    }
}