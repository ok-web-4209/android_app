package com.example.golfapp.data.local

import com.example.golfapp.data.Course
import com.example.golfapp.data.CourseLocation
import com.example.golfapp.data.Game
import com.example.golfapp.data.GolfRepository
import com.example.golfapp.data.HoleResult
import com.example.golfapp.data.Player
import com.example.golfapp.data.PlayerScorecard
import com.example.golfapp.data.Season
import com.example.golfapp.data.SeasonStanding
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class LocalGolfRepository(
    private val database: GolfDatabase,
) : GolfRepository {
    private val playerDao = database.playerDao()
    private val seasonDao = database.seasonDao()
    private val locationDao = database.locationDao()
    private val courseDao = database.courseDao()
    private val seasonLocationDao = database.seasonLocationDao()
    private val gameDao = database.gameDao()
    private val gamePlayerDao = database.gamePlayerDao()
    private val holeResultDao = database.holeResultDao()
    private val holeResultPlayerDao = database.holeResultPlayerDao()

    override suspend fun addPlayer(name: String): Player {
        val player = Player(name = name.trim())
        playerDao.insert(player.toEntity())
        return player
    }

    override suspend fun removePlayer(playerId: String) {
        playerDao.delete(playerId)
    }

    override suspend fun listPlayers(): List<Player> =
        playerDao.listPlayers().map { it.toModel() }

    override suspend fun createSeason(name: String): Season {
        val playerCount = playerDao.countPlayers()
        require(playerCount > 0) { "Create at least one player before starting a season." }
        val season = Season(name = name.trim())
        seasonDao.insert(season.toEntity())
        return season
    }

    override suspend fun listSeasons(): List<Season> =
        seasonDao.listSeasons().map { it.toModel() }

    override suspend fun addLocation(seasonId: String, location: CourseLocation) {
        locationDao.insert(location.toEntity())
        courseDao.insertAll(location.courses.map { it.toEntity(location.id) })
        seasonLocationDao.insert(SeasonLocationEntity(seasonId = seasonId, locationId = location.id))
    }

    override suspend fun listLocations(seasonId: String): List<CourseLocation> {
        val locations = seasonLocationDao.listLocationsForSeason(seasonId)
        return locations.map { location ->
            val courses = courseDao.listByLocation(location.id).map { it.toModel() }
            CourseLocation(
                id = location.id,
                name = location.name,
                courses = courses,
            )
        }
    }

    override suspend fun startGame(
        seasonId: String,
        locationId: String,
        courseId: String,
        startingHole: Int,
        playerIds: List<String>,
    ): Game {
        val game = Game(
            seasonId = seasonId,
            locationId = locationId,
            courseId = courseId,
            startingHole = startingHole,
            startedAt = LocalDateTime.now(),
        )
        gameDao.insert(game.toEntity())
        gamePlayerDao.insertAll(playerIds.distinct().map { playerId ->
            GamePlayerEntity(gameId = game.id, playerId = playerId)
        })
        return game
    }

    override suspend fun recordHoleResult(gameId: String, result: HoleResult): Game {
        val game = gameDao.getById(gameId) ?: error("Game not found")
        val holeResultId = UUID.randomUUID().toString()
        holeResultDao.insert(
            HoleResultEntity(
                id = holeResultId,
                gameId = gameId,
                holeNumber = result.holeNumber,
                createdAt = LocalDateTime.now(),
            ),
        )

        val outcomeByPlayer = mutableMapOf<String, HoleOutcomeType>()
        result.winners.forEach { outcomeByPlayer[it] = HoleOutcomeType.WIN }
        result.losers.forEach { playerId ->
            if (playerId !in outcomeByPlayer) {
                outcomeByPlayer[playerId] = HoleOutcomeType.LOSS
            }
        }

        val holeInOnePlayers = result.holeInOnePlayers.toSet()
        val combinedPlayers = (outcomeByPlayer.keys + holeInOnePlayers).toSet()
        val holeResultPlayers = combinedPlayers.map { playerId ->
            HoleResultPlayerEntity(
                holeResultId = holeResultId,
                playerId = playerId,
                outcomeType = outcomeByPlayer[playerId],
                holeInOne = playerId in holeInOnePlayers,
            )
        }
        holeResultPlayerDao.insertAll(holeResultPlayers)

        val results = buildHoleResults(gameId)
        return game.toModel(results)
    }

    override suspend fun finishGame(gameId: String): Game {
        val game = gameDao.getById(gameId) ?: error("Game not found")
        val updated = game.copy(completedAt = LocalDateTime.now())
        gameDao.update(updated)
        val results = buildHoleResults(gameId)
        return updated.toModel(results)
    }

    override suspend fun playerScorecards(playerId: String): List<PlayerScorecard> {
        return gameDao.listByPlayer(playerId).mapNotNull { game ->
            val courseName = courseDao.getById(game.courseId)?.name ?: return@mapNotNull null
            PlayerScorecard(
                playerId = playerId,
                courseName = courseName,
                score = scoreForPlayer(game.id, playerId),
                datePlayed = game.startedAt.toLocalDate(),
                holeInOneCount = holeInOnesForPlayer(game.id, playerId),
            )
        }
    }

    override suspend fun seasonStandings(seasonId: String): List<SeasonStanding> {
        val players = gamePlayerDao.listPlayerIdsForSeason(seasonId)
        val games = gameDao.listBySeason(seasonId)
        return players.map { playerId ->
            val score = games.sumOf { scoreForPlayer(it.id, playerId) }
            val holeInOnes = games.sumOf { holeInOnesForPlayer(it.id, playerId) }
            SeasonStanding(playerId = playerId, score = score, holeInOneCount = holeInOnes)
        }.sortedByDescending { it.score }
    }

    override suspend fun courseRankings(courseId: String): List<PlayerScorecard> {
        val courseName = courseDao.getById(courseId)?.name ?: "Unknown Course"
        return gameDao.listByCourse(courseId).flatMap { game ->
            gamePlayerDao.listPlayerIds(game.id).map { playerId ->
                PlayerScorecard(
                    playerId = playerId,
                    courseName = courseName,
                    score = scoreForPlayer(game.id, playerId),
                    datePlayed = game.startedAt.toLocalDate(),
                    holeInOneCount = holeInOnesForPlayer(game.id, playerId),
                )
            }
        }.sortedByDescending { it.score }
    }

    override suspend fun exportSeasonStatsCsv(seasonId: String, date: LocalDate): String {
        val standings = seasonStandings(seasonId)
        val playersById = playerDao.listPlayers().associateBy { it.id }
        val header = "Season,Player,Score,HoleInOnes,ExportedOn"
        val rows = standings.joinToString("\n") { standing ->
            val playerName = playersById[standing.playerId]?.name ?: "Unknown"
            listOf(seasonId, playerName, standing.score, standing.holeInOneCount, date).joinToString(",")
        }
        return listOf(header, rows).filter { it.isNotBlank() }.joinToString("\n")
    }

    private suspend fun buildHoleResults(gameId: String): List<HoleResult> {
        val holeResults = holeResultDao.listByGame(gameId)
        val holeResultPlayers = holeResultPlayerDao.listByGame(gameId)
        val groupedPlayers = holeResultPlayers.groupBy { it.holeResultId }
        return holeResults.map { holeResult ->
            val players = groupedPlayers[holeResult.id].orEmpty()
            HoleResult(
                holeNumber = holeResult.holeNumber,
                winners = players.filter { it.outcomeType == HoleOutcomeType.WIN }.map { it.playerId },
                losers = players.filter { it.outcomeType == HoleOutcomeType.LOSS }.map { it.playerId },
                holeInOnePlayers = players.filter { it.holeInOne }.map { it.playerId },
            )
        }
    }

    private suspend fun scoreForPlayer(gameId: String, playerId: String): Int {
        val results = holeResultPlayerDao.listByGameAndPlayer(gameId, playerId)
        val wins = results.count { it.outcomeType == HoleOutcomeType.WIN }
        val losses = results.count { it.outcomeType == HoleOutcomeType.LOSS }
        return wins - losses
    }

    private suspend fun holeInOnesForPlayer(gameId: String, playerId: String): Int {
        return holeResultPlayerDao.listByGameAndPlayer(gameId, playerId).count { it.holeInOne }
    }
}

private fun Player.toEntity(): PlayerEntity = PlayerEntity(id = id, name = name)

private fun PlayerEntity.toModel(): Player = Player(id = id, name = name)

private fun Season.toEntity(): SeasonEntity =
    SeasonEntity(id = id, name = name, createdAt = createdAt)

private fun SeasonEntity.toModel(): Season =
    Season(id = id, name = name, createdAt = createdAt)

private fun CourseLocation.toEntity(): LocationEntity = LocationEntity(id = id, name = name)

private fun Course.toEntity(locationId: String): CourseEntity =
    CourseEntity(id = id, locationId = locationId, name = name, holeCount = holeCount)

private fun CourseEntity.toModel(): Course =
    Course(id = id, name = name, holeCount = holeCount)

private fun Game.toEntity(): GameEntity =
    GameEntity(
        id = id,
        seasonId = seasonId,
        locationId = locationId,
        courseId = courseId,
        startedAt = startedAt,
        completedAt = completedAt,
        startingHole = startingHole,
    )

private fun GameEntity.toModel(holeResults: List<HoleResult>): Game =
    Game(
        id = id,
        seasonId = seasonId,
        locationId = locationId,
        courseId = courseId,
        startedAt = startedAt,
        completedAt = completedAt,
        startingHole = startingHole,
        holeResults = holeResults,
    )
