package com.schedly.domain.error

import com.schedly.domain.model.DayOfWeek

data class RoomConflictException(
    val day: DayOfWeek,
    val period: Int,
    val room: String
) : Exception("Room '$room' already occupied on ${day.name} period $period")
