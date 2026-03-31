package com.schedly.domain.error

sealed class ValidationError(message: String) : Exception(message) {
    data class InvalidCourseName(override val message: String) : ValidationError(message)
    data class MissingSessions(override val message: String) : ValidationError(message)
    data class InvalidPeriod(val period: Int, override val message: String) : ValidationError(message)
    data class InvalidDay(override val message: String) : ValidationError(message)
    data class InvalidConstraints(val errors: List<String>) : ValidationError(errors.joinToString("; "))
    data class RoomConflict(override val message: String) : ValidationError(message)
}
