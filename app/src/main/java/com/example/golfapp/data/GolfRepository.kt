package com.example.golfapp.data

import java.time.LocalDate

interface GolfRepository {
    suspend fun addPlayer(name: String): Player
    suspend fun removePlayer(playerId: String)
    suspend fun listPlayers(): List<Player>

    suspend fun createSeason(name: String): Season
    suspend fun listSeasons(): List<Season>

    suspend fun addLocation(seasonId: String, location: CourseLocation)
    suspend fun listLocations(seasonId: String): List<CourseLocation>

    suspend fun startGame(
        seasonId: String,
        locationId: String,
        courseId: String,
        startingHole: Int,
        playerIds: List<String>,
    ): Game

    suspend fun recordHoleResult(gameId: String, result: HoleResult): Game
    suspend fun finishGame(gameId: String): Game

    suspend fun playerScorecards(playerId: String): List<PlayerScorecard>
    suspend fun seasonStandings(seasonId: String): List<SeasonStanding>
    suspend fun courseRankings(courseId: String): List<PlayerScorecard>

    suspend fun exportSeasonStatsCsv(seasonId: String, date: LocalDate = LocalDate.now()): String
}
