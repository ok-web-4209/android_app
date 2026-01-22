package com.example.golfapp.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "players",
)
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(
    tableName = "seasons",
)
data class SeasonEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
)

@Entity(
    tableName = "locations",
)
data class LocationEntity(
    @PrimaryKey val id: String,
    val name: String,
)

@Entity(
    tableName = "courses",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["location_id"])],
)
data class CourseEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "location_id") val locationId: String,
    val name: String,
    @ColumnInfo(name = "hole_count") val holeCount: Int,
)

@Entity(
    tableName = "season_locations",
    primaryKeys = ["season_id", "location_id"],
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["season_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["season_id"]),
        Index(value = ["location_id"]),
    ],
)
data class SeasonLocationEntity(
    @ColumnInfo(name = "season_id") val seasonId: String,
    @ColumnInfo(name = "location_id") val locationId: String,
)

@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["season_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["location_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["course_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["season_id"]),
        Index(value = ["location_id"]),
        Index(value = ["course_id"]),
    ],
)
data class GameEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "season_id") val seasonId: String,
    @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "course_id") val courseId: String,
    @ColumnInfo(name = "started_at") val startedAt: LocalDateTime,
    @ColumnInfo(name = "completed_at") val completedAt: LocalDateTime?,
    @ColumnInfo(name = "starting_hole") val startingHole: Int,
)

@Entity(
    tableName = "game_players",
    primaryKeys = ["game_id", "player_id"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["game_id"]),
        Index(value = ["player_id"]),
    ],
)
data class GamePlayerEntity(
    @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "player_id") val playerId: String,
)

@Entity(
    tableName = "hole_results",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["game_id"]),
    ],
)
data class HoleResultEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "game_id") val gameId: String,
    @ColumnInfo(name = "hole_number") val holeNumber: Int,
    @ColumnInfo(name = "created_at") val createdAt: LocalDateTime,
)

enum class HoleOutcomeType {
    WIN,
    LOSS,
}

@Entity(
    tableName = "hole_result_players",
    primaryKeys = ["hole_result_id", "player_id"],
    foreignKeys = [
        ForeignKey(
            entity = HoleResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["hole_result_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["hole_result_id"]),
        Index(value = ["player_id"]),
    ],
)
data class HoleResultPlayerEntity(
    @ColumnInfo(name = "hole_result_id") val holeResultId: String,
    @ColumnInfo(name = "player_id") val playerId: String,
    @ColumnInfo(name = "outcome_type") val outcomeType: HoleOutcomeType?,
    @ColumnInfo(name = "hole_in_one") val holeInOne: Boolean,
)
