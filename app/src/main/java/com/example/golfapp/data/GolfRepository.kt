package com.example.golfapp.data

import java.time.LocalDate

interface GolfRepository {
    fun addPlayer(name: String): Player
    fun removePlayer(playerId: String)
    fun listPlayers(): List<Player>

    fun createSeason(name: String): Season
    fun listSeasons(): List<Season>

    fun addLocation(seasonId: String, location: CourseLocation)
    fun listLocations(seasonId: String): List<CourseLocation>

    fun startGame(
        seasonId: String,
        locationId: String,
        courseId: String,
        startingHole: Int,
        playerIds: List<String>,
    ): Game

    fun recordHoleResult(gameId: String, result: HoleResult): Game
    fun finishGame(gameId: String): Game

    fun playerScorecards(playerId: String): List<PlayerScorecard>
    fun seasonStandings(seasonId: String): List<SeasonStanding>
    fun courseRankings(courseId: String): List<PlayerScorecard>

    fun exportSeasonStatsCsv(seasonId: String, date: LocalDate = LocalDate.now()): String
}
