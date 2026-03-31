package com.schedly.domain.error

import com.schedly.domain.model.DayOfWeek

class RoomConflictException(
    val day: DayOfWeek?,
    val period: Int?,
    val room: String?,
    message: String
) : Exception(message) {
    constructor(day: DayOfWeek, period: Int, room: String) : this(
        day = day,
        period = period,
        room = room,
        message = "Room '$room' already occupied on ${day.name} period $period"
    )
}
