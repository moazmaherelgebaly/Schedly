package com.schedly.data.db

import androidx.room.TypeConverter
import com.schedly.domain.model.DayOfWeek
import com.schedly.domain.model.PriorityLevel
import com.schedly.domain.model.SessionType

class Converters {

    @TypeConverter
    fun fromSessionType(value: SessionType): String = value.name

    @TypeConverter
    fun toSessionType(value: String): SessionType = SessionType.valueOf(value)

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek): String = value.name

    @TypeConverter
    fun toDayOfWeek(value: String): DayOfWeek = DayOfWeek.valueOf(value)

    @TypeConverter
    fun fromPriorityLevel(value: PriorityLevel): String = value.name

    @TypeConverter
    fun toPriorityLevel(value: String): PriorityLevel = PriorityLevel.valueOf(value)
}
