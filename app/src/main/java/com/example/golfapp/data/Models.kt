package com.example.golfapp.data

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Player(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
)

data class Season(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class CourseLocation(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val courses: List<Course>,
)

data class Course(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val holeCount: Int,
)

data class Game(
    val id: String = UUID.randomUUID().toString(),
    val seasonId: String,
    val locationId: String,
    val courseId: String,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val startingHole: Int,
    val holeResults: List<HoleResult> = emptyList(),
)

data class HoleResult(
    val holeNumber: Int,
    val winners: List<String>,
    val losers: List<String>,
    val holeInOnePlayers: List<String>,
)

data class PlayerScorecard(
    val playerId: String,
    val courseName: String,
    val score: Int,
    val datePlayed: LocalDate,
    val holeInOneCount: Int,
)

data class SeasonStanding(
    val playerId: String,
    val score: Int,
    val holeInOneCount: Int,
)
