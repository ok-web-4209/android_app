package com.example.golfapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        PlayerEntity::class,
        SeasonEntity::class,
        LocationEntity::class,
        CourseEntity::class,
        SeasonLocationEntity::class,
        GameEntity::class,
        GamePlayerEntity::class,
        HoleResultEntity::class,
        HoleResultPlayerEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GolfDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun seasonDao(): SeasonDao
    abstract fun locationDao(): LocationDao
    abstract fun courseDao(): CourseDao
    abstract fun seasonLocationDao(): SeasonLocationDao
    abstract fun gameDao(): GameDao
    abstract fun gamePlayerDao(): GamePlayerDao
    abstract fun holeResultDao(): HoleResultDao
    abstract fun holeResultPlayerDao(): HoleResultPlayerDao
}
