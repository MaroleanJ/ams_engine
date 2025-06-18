package com.techbros.repositories

import com.techbros.database.tables.Users
import com.techbros.models.dto.CreateUserRequest
import com.techbros.models.dto.UpdateUserRequest
import com.techbros.models.dto.UserDto
import com.techbros.models.dto.UserSafeDto
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt

class UserRepository {

    suspend fun create(request: CreateUserRequest): Int = dbQuery {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())

        Users.insert {
            it[email] = request.email
            it[phone] = request.phone
            it[passwordHash] = hashedPassword
            it[firstName] = request.firstName
            it[lastName] = request.lastName
            it[profilePicture] = request.profilePicture
            it[role] = request.role
        }[Users.id]
    }

    suspend fun findById(id: Int): UserDto? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findByIdSafe(id: Int): UserSafeDto? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { mapRowToSafeDto(it) }
            .singleOrNull()
    }

    suspend fun findByEmail(email: String): UserDto? = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .map { mapRowToDto(it) }
            .singleOrNull()
    }

    suspend fun findAll(): List<UserSafeDto> = dbQuery {
        Users.selectAll()
            .orderBy(Users.createdAt to SortOrder.DESC)
            .map { mapRowToSafeDto(it) }
    }

    suspend fun update(id: Int, request: UpdateUserRequest): Boolean = dbQuery {
        Users.update({ Users.id eq id }) {
            it[email] = request.email
            it[phone] = request.phone
            it[firstName] = request.firstName
            it[lastName] = request.lastName
            it[profilePicture] = request.profilePicture
            it[role] = request.role
        } > 0
    }

    suspend fun updatePassword(id: Int, newPassword: String): Boolean = dbQuery {
        val hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        Users.update({ Users.id eq id }) {
            it[passwordHash] = hashedPassword
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        Users.deleteWhere { Users.id eq id } > 0
    }

    suspend fun findByRole(role: String): List<UserSafeDto> = dbQuery {
        Users.selectAll()
            .where { Users.role eq role }
            .orderBy(Users.createdAt to SortOrder.DESC)
            .map { mapRowToSafeDto(it) }
    }

    suspend fun emailExists(email: String): Boolean = dbQuery {
        Users.selectAll()
            .where { Users.email eq email }
            .count() > 0
    }

    suspend fun verifyPassword(email: String, password: String): UserDto? = dbQuery {
        val user = Users.selectAll()
            .where { Users.email eq email }
            .map { mapRowToDto(it) }
            .singleOrNull()

        if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }

    private fun mapRowToDto(row: ResultRow): UserDto {
        return UserDto(
            id = row[Users.id],
            email = row[Users.email],
            phone = row[Users.phone],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            profilePicture = row[Users.profilePicture],
            role = row[Users.role],
            passwordHash = row[Users.passwordHash],
            createdAt = row[Users.createdAt].toString()
        )
    }

    private fun mapRowToSafeDto(row: ResultRow): UserSafeDto {
        return UserSafeDto(
            id = row[Users.id],
            email = row[Users.email],
            phone = row[Users.phone],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            profilePicture = row[Users.profilePicture],
            role = row[Users.role],
            createdAt = row[Users.createdAt].toString()
        )
    }
}