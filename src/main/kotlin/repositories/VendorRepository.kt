package com.techbros.repositories

import com.techbros.database.tables.Vendors
import com.techbros.models.dto.CreateVendorRequest
import com.techbros.models.dto.UpdateVendorRequest
import com.techbros.models.dto.VendorDto
import com.techbros.models.dto.VendorSummaryDto
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class VendorRepository {

    suspend fun create(request: CreateVendorRequest): Int = dbQuery {
        Vendors.insert {
            it[name] = request.name
            it[contactEmail] = request.contactEmail
            it[contactPhone] = request.contactPhone
            it[address] = request.address
        }[Vendors.id]
    }

    suspend fun findById(id: Int): VendorDto? = dbQuery {
        Vendors.selectAll()
            .where { Vendors.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findAllSummary(): List<VendorSummaryDto> = dbQuery {
        Vendors.selectAll()
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToSummaryDto(it) }
    }

    suspend fun update(id: Int, request: UpdateVendorRequest): Boolean = dbQuery {
        Vendors.update({ Vendors.id eq id }) {
            it[name] = request.name
            it[contactEmail] = request.contactEmail
            it[contactPhone] = request.contactPhone
            it[address] = request.address
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Vendors.deleteWhere { Vendors.id eq id } > 0
    }

    suspend fun searchByName(searchTerm: String): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.name.lowerCase() like "%${searchTerm.lowercase()}%" }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByEmail(email: String): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactEmail.lowerCase() eq email.lowercase() }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findByPhone(phone: String): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactPhone eq phone }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findVendorsWithEmail(): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactEmail.isNotNull() }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun findVendorsWithPhone(): List<VendorDto> = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactPhone.isNotNull() }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun searchByContact(searchTerm: String): List<VendorDto> = dbQuery {
        val term = "%${searchTerm.lowercase()}%"
        Vendors.selectAll()
            .where {
                (Vendors.name.lowerCase() like term) or
                        (Vendors.contactEmail.lowerCase() like term) or
                        (Vendors.contactPhone like term)
            }
            .orderBy(Vendors.name to SortOrder.ASC)
            .map { mapRowToDto(it) }
    }

    suspend fun exists(id: Int): Boolean = dbQuery {
        Vendors.selectAll()
            .where { Vendors.id eq id }
            .count() > 0
    }

    suspend fun getCount(): Long = dbQuery {
        Vendors.selectAll().count()
    }

    suspend fun getCountWithEmail(): Long = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactEmail.isNotNull() }
            .count()
    }

    suspend fun getCountWithPhone(): Long = dbQuery {
        Vendors.selectAll()
            .where { Vendors.contactPhone.isNotNull() }
            .count()
    }

    private fun mapRowToDto(row: ResultRow): VendorDto {
        return VendorDto(
            id = row[Vendors.id],
            name = row[Vendors.name],
            contactEmail = row[Vendors.contactEmail],
            contactPhone = row[Vendors.contactPhone],
            address = row[Vendors.address]
        )
    }

    private fun mapRowToSummaryDto(row: ResultRow): VendorSummaryDto {
        return VendorSummaryDto(
            id = row[Vendors.id],
            name = row[Vendors.name],
            contactEmail = row[Vendors.contactEmail],
            hasPhone = row[Vendors.contactPhone] != null,
            hasAddress = row[Vendors.address] != null
        )
    }
}