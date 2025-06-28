package com.techbros.repositories

import com.techbros.database.tables.Assets
import com.techbros.database.tables.AssetIssues
import com.techbros.database.tables.Users
import com.techbros.models.dto.*
import com.techbros.utils.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class AssetIssueRepository {

    suspend fun create(request: CreateAssetIssueRequest): Int = dbQuery {
        AssetIssues.insert {
            it[assetId] = request.assetId
            it[reportedBy] = request.reportedBy
            it[assignedTo] = request.assignedTo
            it[issueType] = request.issueType
            it[severity] = request.severity
            it[issueDescription] = request.issueDescription
            it[resolutionNotes] = request.resolutionNotes
            it[status] = request.status
        }[AssetIssues.id]
    }

    suspend fun bulkCreate(requests: List<CreateAssetIssueRequest>): List<Int> = dbQuery {
        requests.map { request ->
            AssetIssues.insert {
                it[assetId] = request.assetId
                it[reportedBy] = request.reportedBy
                it[assignedTo] = request.assignedTo
                it[issueType] = request.issueType
                it[severity] = request.severity
                it[issueDescription] = request.issueDescription
                it[resolutionNotes] = request.resolutionNotes
                it[status] = request.status
            }[AssetIssues.id]
        }
    }

    suspend fun findById(id: Int): AssetIssueDto? = dbQuery {

        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")

        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.id eq id }
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
            .singleOrNull()
    }

    suspend fun findAll(): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")

        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { row ->
                mapRowToDto(row, reportedByAlias, assignedToAlias)
            }
    }


    suspend fun update(id: Int, request: UpdateAssetIssueRequest): Boolean = dbQuery {
        val currentResolvedAt = AssetIssues
            .selectAll()
            .where { AssetIssues.id eq id }
            .singleOrNull()
            ?.get(AssetIssues.resolvedAt)

        AssetIssues.update({ AssetIssues.id eq id }) {
            it[assetId] = request.assetId
            it[reportedBy] = request.reportedBy
            it[assignedTo] = request.assignedTo
            it[issueType] = request.issueType
            it[severity] = request.severity
            it[issueDescription] = request.issueDescription
            it[resolutionNotes] = request.resolutionNotes
            it[status] = request.status

            when (request.status.uppercase()) {
                "RESOLVED" -> {
                    it[resolvedAt] = LocalDateTime.now()
                }
                "CLOSED" -> {
                    it[closedAt] = LocalDateTime.now()
                    if (currentResolvedAt == null) {
                        it[resolvedAt] = LocalDateTime.now()
                    }
                }
            }
        } > 0
    }


    suspend fun delete(id: Int): Boolean = dbQuery {
        AssetIssues.deleteWhere { AssetIssues.id eq id } > 0
    }

    suspend fun findByAssetId(assetId: Int): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.assetId eq assetId }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findByReportedBy(reportedBy: Int): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.reportedBy eq reportedBy }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findByAssignedTo(assignedTo: Int): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.assignedTo eq assignedTo }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findByIssueType(issueType: String): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.issueType eq issueType }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findBySeverity(severity: String): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.severity eq severity }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findByStatus(status: String): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where { AssetIssues.status eq status }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun findByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): List<AssetIssueDto> = dbQuery {
        val reportedByAlias = Users.alias("reported_by_user")
        val assignedToAlias = Users.alias("assigned_to_user")
        AssetIssues
            .join(Assets, JoinType.LEFT, AssetIssues.assetId, Assets.id)
            .join(reportedByAlias, JoinType.LEFT, AssetIssues.reportedBy, reportedByAlias[Users.id])
            .join(assignedToAlias, JoinType.LEFT, AssetIssues.assignedTo, assignedToAlias[Users.id])
            .selectAll()
            .where {
                (AssetIssues.reportedAt greaterEq startDate) and
                        (AssetIssues.reportedAt lessEq endDate)
            }
            .orderBy(AssetIssues.reportedAt to SortOrder.DESC)
            .map { mapRowToDto(it, reportedByAlias, assignedToAlias) }
    }

    suspend fun assignIssue(id: Int, assignedTo: Int): Boolean = dbQuery {
        val currentStatus = AssetIssues
            .selectAll()
            .where { AssetIssues.id eq id }
            .singleOrNull()
            ?.get(AssetIssues.status)

        AssetIssues.update({ AssetIssues.id eq id }) {
            it[AssetIssues.assignedTo] = assignedTo
            if (currentStatus == "OPEN") {
                it[AssetIssues.status] = "ASSIGNED"
            }
        } > 0
    }

    suspend fun resolveIssue(id: Int, resolutionNotes: String, status: String): Boolean = dbQuery {
        AssetIssues.update({ AssetIssues.id eq id }) {
            it[AssetIssues.resolutionNotes] = resolutionNotes
            it[AssetIssues.status] = status
            it[resolvedAt] = LocalDateTime.now()

            if (status.uppercase() == "CLOSED") {
                it[closedAt] = LocalDateTime.now()
            }
        } > 0
    }

    suspend fun deleteByAssetId(assetId: Int): Int = dbQuery {
        AssetIssues.deleteWhere { AssetIssues.assetId eq assetId }
    }

    suspend fun getAssetIssueHistory(assetId: Int): AssetIssueHistoryDto? = dbQuery {
        val issues = findByAssetId(assetId)
        if (issues.isEmpty()) return@dbQuery null

        val asset = Assets.selectAll()
            .where { Assets.id eq assetId }
            .singleOrNull()
            ?: return@dbQuery null

        val openIssues = issues.count { it.status == "OPEN" || it.status == "ASSIGNED" }
        val resolvedIssues = issues.count { it.status == "RESOLVED" }
        val closedIssues = issues.count { it.status == "CLOSED" }

        AssetIssueHistoryDto(
            assetId = assetId,
            assetName = asset[Assets.name],
            assetSerialNumber = asset[Assets.serialNumber],
            totalIssues = issues.size,
            openIssues = openIssues,
            resolvedIssues = resolvedIssues,
            closedIssues = closedIssues,
            lastIssueDate = issues.firstOrNull()?.reportedAt,
            issues = issues
        )
    }

    suspend fun getIssueStats(): IssueStatsDto = dbQuery {
        val allIssues = AssetIssues.selectAll().toList()

        val issuesByStatus = allIssues.groupBy { it[AssetIssues.status] }
            .mapValues { it.value.size }

        val issuesBySeverity = allIssues.groupBy { it[AssetIssues.severity] }
            .mapValues { it.value.size }

        val issuesByType = allIssues.groupBy { it[AssetIssues.issueType] }
            .mapValues { it.value.size }

        val issuesByMonth = allIssues.groupBy {
            val reportedAt = it[AssetIssues.reportedAt]
            "${reportedAt.year}-${reportedAt.monthValue.toString().padStart(2, '0')}"
        }.mapValues { it.value.size }

        // Calculate average resolution time
        val resolvedIssues = allIssues.filter {
            it[AssetIssues.resolvedAt] != null
        }

        val averageResolutionTimeHours = if (resolvedIssues.isNotEmpty()) {
            resolvedIssues.map { issue ->
                val reported = issue[AssetIssues.reportedAt]
                val resolved = issue[AssetIssues.resolvedAt]!!
                ChronoUnit.HOURS.between(reported, resolved).toDouble()
            }.average()
        } else null

        // Count open issues older than 30 days
        val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
        val openIssuesOlderThan30Days = allIssues.count { issue ->
            val status = issue[AssetIssues.status]
            val reportedAt = issue[AssetIssues.reportedAt]
            (status == "OPEN" || status == "ASSIGNED") && reportedAt.isBefore(thirtyDaysAgo)
        }

        IssueStatsDto(
            totalIssues = allIssues.size,
            issuesByStatus = issuesByStatus,
            issuesBySeverity = issuesBySeverity,
            issuesByType = issuesByType,
            issuesByMonth = issuesByMonth,
            averageResolutionTimeHours = averageResolutionTimeHours,
            openIssuesOlderThan30Days = openIssuesOlderThan30Days
        )
    }

    suspend fun getUserIssueStats(userId: Int): UserIssueStatsDto? = dbQuery {
        val user = Users.selectAll()
            .where { Users.id eq userId }
            .singleOrNull()
            ?: return@dbQuery null

        val reportedIssues = AssetIssues.selectAll()
            .where { AssetIssues.reportedBy eq userId }
            .count().toInt()

        val assignedIssues = AssetIssues.selectAll()
            .where { AssetIssues.assignedTo eq userId }
            .count().toInt()

        val resolvedIssues = AssetIssues.selectAll()
            .where {
                (AssetIssues.assignedTo eq userId) and
                        (AssetIssues.status eq "RESOLVED" or (AssetIssues.status eq "CLOSED"))
            }
            .count().toInt()

        val pendingIssues = AssetIssues.selectAll()
            .where {
                (AssetIssues.assignedTo eq userId) and
                        (AssetIssues.status eq "OPEN" or (AssetIssues.status eq "ASSIGNED"))
            }
            .count().toInt()

        UserIssueStatsDto(
            userId = userId,
            userName = "${user[Users.firstName] ?: ""} ${user[Users.lastName] ?: ""}".trim(),
            reportedIssues = reportedIssues,
            assignedIssues = assignedIssues,
            resolvedIssues = resolvedIssues,
            pendingIssues = pendingIssues
        )
    }

    private fun mapRowToDto(
        row: ResultRow,
        reportedByAlias: Alias<Users>,
        assignedToAlias: Alias<Users>
    ): AssetIssueDto {
        return AssetIssueDto(
            id = row[AssetIssues.id],
            assetId = row[AssetIssues.assetId],
            reportedBy = row[AssetIssues.reportedBy],
            assignedTo = row[AssetIssues.assignedTo],
            issueType = row[AssetIssues.issueType],
            severity = row[AssetIssues.severity],
            issueDescription = row[AssetIssues.issueDescription],
            resolutionNotes = row[AssetIssues.resolutionNotes],
            status = row[AssetIssues.status],
            reportedAt = row[AssetIssues.reportedAt].toString(),
            resolvedAt = row[AssetIssues.resolvedAt]?.toString(),
            closedAt = row[AssetIssues.closedAt]?.toString(),
            assetName = row.getOrNull(Assets.name),
            assetSerialNumber = row.getOrNull(Assets.serialNumber),
            reportedByName = "${row.getOrNull(reportedByAlias[Users.firstName]) ?: ""} ${row.getOrNull(reportedByAlias[Users.lastName]) ?: ""}".trim(),
            reportedByEmail = row.getOrNull(reportedByAlias[Users.email]),
            assignedToName = "${row.getOrNull(assignedToAlias[Users.firstName]) ?: ""} ${row.getOrNull(assignedToAlias[Users.lastName]) ?: ""}".trim(),
            assignedToEmail = row.getOrNull(assignedToAlias[Users.email])
        )
    }
}