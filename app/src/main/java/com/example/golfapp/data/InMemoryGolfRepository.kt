package com.example.golfapp.data

import java.time.LocalDate
import java.time.LocalDateTime

class InMemoryGolfRepository : GolfRepository {
    private val players = mutableListOf<Player>()
    private val seasons = mutableListOf<Season>()
    private val locationsBySeason = mutableMapOf<String, MutableList<CourseLocation>>()
    private val games = mutableListOf<Game>()
    private val gamePlayers = mutableMapOf<String, List<String>>()

    override suspend fun addPlayer(name: String): Player {
        val player = Player(name = name.trim())
        players.add(player)
        return player
    }

    override suspend fun removePlayer(playerId: String) {
        players.removeAll { it.id == playerId }
    }

    override suspend fun listPlayers(): List<Player> = players.toList()

    override suspend fun createSeason(name: String): Season {
        val season = Season(name = name.trim())
        seasons.add(season)
        return season
    }

    override suspend fun listSeasons(): List<Season> = seasons.toList()

    override suspend fun addLocation(seasonId: String, location: CourseLocation) {
        val locations = locationsBySeason.getOrPut(seasonId) { mutableListOf() }
        locations.add(location)
    }

    override suspend fun listLocations(seasonId: String): List<CourseLocation> =
        locationsBySeason[seasonId]?.toList().orEmpty()

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
        games.add(game)
        gamePlayers[game.id] = playerIds
        return game
    }

    override suspend fun recordHoleResult(gameId: String, result: HoleResult): Game {
        val gameIndex = games.indexOfFirst { it.id == gameId }
        check(gameIndex >= 0) { "Game not found" }
        val game = games[gameIndex]
        val updated = game.copy(holeResults = game.holeResults + result)
        games[gameIndex] = updated
        return updated
    }

    override suspend fun finishGame(gameId: String): Game {
        val gameIndex = games.indexOfFirst { it.id == gameId }
        check(gameIndex >= 0) { "Game not found" }
        val game = games[gameIndex]
        val updated = game.copy(completedAt = LocalDateTime.now())
        games[gameIndex] = updated
        return updated
    }

    override suspend fun playerScorecards(playerId: String): List<PlayerScorecard> {
        return games.mapNotNull { game ->
            val playersInGame = gamePlayers[game.id].orEmpty()
            if (playerId !in playersInGame) {
                return@mapNotNull null
            }
            val courseName = findCourseName(game.courseId)
            val score = scoreForPlayer(game, playerId)
            val holeInOnes = holeInOnesForPlayer(game, playerId)
            PlayerScorecard(
                playerId = playerId,
                courseName = courseName,
                score = score,
                datePlayed = game.startedAt.toLocalDate(),
                holeInOneCount = holeInOnes,
            )
        }
    }

    override suspend fun seasonStandings(seasonId: String): List<SeasonStanding> {
        val seasonGames = games.filter { it.seasonId == seasonId }
        val playersInSeason = seasonGames.flatMap { gamePlayers[it.id].orEmpty() }.distinct()
        return playersInSeason.map { playerId ->
            val scores = seasonGames.sumOf { scoreForPlayer(it, playerId) }
            val holeInOnes = seasonGames.sumOf { holeInOnesForPlayer(it, playerId) }
            SeasonStanding(playerId = playerId, score = scores, holeInOneCount = holeInOnes)
        }.sortedByDescending { it.score }
    }

    override suspend fun courseRankings(courseId: String): List<PlayerScorecard> {
        return games.filter { it.courseId == courseId }.flatMap { game ->
            val courseName = findCourseName(courseId)
            gamePlayers[game.id].orEmpty().map { playerId ->
                PlayerScorecard(
                    playerId = playerId,
                    courseName = courseName,
                    score = scoreForPlayer(game, playerId),
                    datePlayed = game.startedAt.toLocalDate(),
                    holeInOneCount = holeInOnesForPlayer(game, playerId),
                )
            }
        }.sortedByDescending { it.score }
    }

    override suspend fun exportSeasonStatsCsv(seasonId: String, date: LocalDate): String {
        val standings = seasonStandings(seasonId)
        val header = "Season,Player,Score,HoleInOnes,ExportedOn"
        val rows = standings.joinToString("\n") { standing ->
            val playerName = players.firstOrNull { it.id == standing.playerId }?.name ?: "Unknown"
            listOf(seasonId, playerName, standing.score, standing.holeInOneCount, date).joinToString(",")
        }
        return listOf(header, rows).filter { it.isNotBlank() }.joinToString("\n")
    }

    private fun scoreForPlayer(game: Game, playerId: String): Int {
        val wins = game.holeResults.count { playerId in it.winners }
        val losses = game.holeResults.count { playerId in it.losers }
        return wins - losses
    }

    private fun holeInOnesForPlayer(game: Game, playerId: String): Int {
        return game.holeResults.sumOf { result -> if (playerId in result.holeInOnePlayers) 1 else 0 }
    }

    private fun findCourseName(courseId: String): String {
        val location = locationsBySeason.values.flatten().firstOrNull { location ->
            location.courses.any { it.id == courseId }
        }
        return location?.courses?.firstOrNull { it.id == courseId }?.name ?: "Unknown Course"
    }
}
