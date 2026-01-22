package com.example.golfapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: PlayerEntity)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun delete(playerId: String)

    @Query("SELECT * FROM players ORDER BY name")
    suspend fun listPlayers(): List<PlayerEntity>

    @Query("SELECT COUNT(*) FROM players")
    suspend fun countPlayers(): Int
}

@Dao
interface SeasonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: SeasonEntity)

    @Query("SELECT * FROM seasons ORDER BY created_at DESC")
    suspend fun listSeasons(): List<SeasonEntity>
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getById(locationId: String): LocationEntity?

    @Query("SELECT * FROM locations ORDER BY name")
    suspend fun listLocations(): List<LocationEntity>
}

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    @Query("SELECT * FROM courses WHERE location_id = :locationId ORDER BY name")
    suspend fun listByLocation(locationId: String): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    suspend fun getById(courseId: String): CourseEntity?
}

@Dao
interface SeasonLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: SeasonLocationEntity)

    @Query(
        """
        SELECT locations.* FROM locations
        INNER JOIN season_locations ON season_locations.location_id = locations.id
        WHERE season_locations.season_id = :seasonId
        ORDER BY locations.name
        """,
    )
    suspend fun listLocationsForSeason(seasonId: String): List<LocationEntity>
}

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: GameEntity)

    @Update
    suspend fun update(game: GameEntity)

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getById(gameId: String): GameEntity?

    @Query("SELECT * FROM games WHERE season_id = :seasonId ORDER BY started_at DESC")
    suspend fun listBySeason(seasonId: String): List<GameEntity>

    @Query("SELECT * FROM games WHERE course_id = :courseId ORDER BY started_at DESC")
    suspend fun listByCourse(courseId: String): List<GameEntity>

    @Query(
        """
        SELECT games.* FROM games
        INNER JOIN game_players ON game_players.game_id = games.id
        WHERE game_players.player_id = :playerId
        ORDER BY games.started_at DESC
        """,
    )
    suspend fun listByPlayer(playerId: String): List<GameEntity>
}

@Dao
interface GamePlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<GamePlayerEntity>)

    @Query("SELECT player_id FROM game_players WHERE game_id = :gameId")
    suspend fun listPlayerIds(gameId: String): List<String>

    @Query(
        """
        SELECT DISTINCT player_id FROM game_players
        INNER JOIN games ON games.id = game_players.game_id
        WHERE games.season_id = :seasonId
        """,
    )
    suspend fun listPlayerIdsForSeason(seasonId: String): List<String>
}

@Dao
interface HoleResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: HoleResultEntity)

    @Query("SELECT * FROM hole_results WHERE game_id = :gameId ORDER BY hole_number")
    suspend fun listByGame(gameId: String): List<HoleResultEntity>
}

@Dao
interface HoleResultPlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<HoleResultPlayerEntity>)

    @Query(
        """
        SELECT hole_result_players.* FROM hole_result_players
        INNER JOIN hole_results ON hole_results.id = hole_result_players.hole_result_id
        WHERE hole_results.game_id = :gameId AND hole_result_players.player_id = :playerId
        """,
    )
    suspend fun listByGameAndPlayer(gameId: String, playerId: String): List<HoleResultPlayerEntity>

    @Query(
        """
        SELECT hole_result_players.* FROM hole_result_players
        INNER JOIN hole_results ON hole_results.id = hole_result_players.hole_result_id
        WHERE hole_results.game_id = :gameId
        """,
    )
    suspend fun listByGame(gameId: String): List<HoleResultPlayerEntity>
}
