package com.example.golfapp.data.local

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.toString()

    @TypeConverter
    fun toHoleOutcome(value: String?): HoleOutcomeType? = value?.let(HoleOutcomeType::valueOf)

    @TypeConverter
    fun fromHoleOutcome(outcome: HoleOutcomeType?): String? = outcome?.name
}
