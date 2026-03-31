package com.schedly.data.db

import androidx.room.TypeConverter
import com.schedly.domain.model.Constraints
import com.schedly.domain.model.DayOfWeek
import com.schedly.domain.model.DayPeriod
import com.schedly.domain.model.GroupPref
import com.schedly.domain.model.InstructorPref
import com.schedly.domain.model.SessionRef
import com.schedly.domain.model.SessionType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ConstraintsConverter {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private fun safeValueOfDayOfWeek(name: String): DayOfWeek? {
        return DayOfWeek.values().find { it.name == name }
    }

    private fun safeValueOfSessionType(name: String): SessionType? {
        return SessionType.values().find { it.name == name }
    }

    private fun safeUUIDFromString(uuidString: String): java.util.UUID? {
        return try {
            java.util.UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    @TypeConverter
    fun fromConstraints(constraints: Constraints): String {
        return json.encodeToString(ConstraintsSerializer.SerializableConstraints(
            weekLoadMin = constraints.weekLoadMin,
            weekLoadMax = constraints.weekLoadMax,
            dayLoadMin = constraints.dayLoadMin,
            dayLoadMax = constraints.dayLoadMax,
            excludedDayPeriods = constraints.excludedDayPeriods.map {
                ConstraintsSerializer.SerializableDayPeriod(it.day.name, it.period)
            },
            excludedSessions = constraints.excludedSessions.map {
                ConstraintsSerializer.SerializableSessionRef(it.courseId.toString(), it.type.name, it.group)
            },
            allowGaps = constraints.allowGaps,
            matchGroups = constraints.matchGroups,
            preferredInstructors = constraints.preferredInstructors.map {
                ConstraintsSerializer.SerializableInstructorPref(it.courseId.toString(), it.type.name, it.instructor)
            },
            preferredGroups = constraints.preferredGroups.map {
                ConstraintsSerializer.SerializableGroupPref(it.courseId.toString(), it.type.name, it.group)
            }
        ))
    }

    @TypeConverter
    fun toConstraints(jsonString: String): Constraints {
        val serializable = json.decodeFromString<ConstraintsSerializer.SerializableConstraints>(jsonString)
        return Constraints(
            weekLoadMin = serializable.weekLoadMin,
            weekLoadMax = serializable.weekLoadMax,
            dayLoadMin = serializable.dayLoadMin,
            dayLoadMax = serializable.dayLoadMax,
            excludedDayPeriods = serializable.excludedDayPeriods.mapNotNull {
                safeValueOfDayOfWeek(it.day)?.let { day -> DayPeriod(day, it.period) }
            },
            excludedSessions = serializable.excludedSessions.mapNotNull {
                val courseId = safeUUIDFromString(it.courseId) ?: return@mapNotNull null
                val type = safeValueOfSessionType(it.type) ?: return@mapNotNull null
                SessionRef(courseId, type, it.group)
            },
            allowGaps = serializable.allowGaps,
            matchGroups = serializable.matchGroups,
            preferredInstructors = serializable.preferredInstructors.mapNotNull {
                val courseId = safeUUIDFromString(it.courseId) ?: return@mapNotNull null
                val type = safeValueOfSessionType(it.type) ?: return@mapNotNull null
                InstructorPref(courseId, type, it.instructor)
            },
            preferredGroups = serializable.preferredGroups.mapNotNull {
                val courseId = safeUUIDFromString(it.courseId) ?: return@mapNotNull null
                val type = safeValueOfSessionType(it.type) ?: return@mapNotNull null
                GroupPref(courseId, type, it.group)
            }
        )
    }

    @TypeConverter
    fun fromDayPeriod(dayPeriod: DayPeriod): String {
        return json.encodeToString(ConstraintsSerializer.SerializableDayPeriod(dayPeriod.day.name, dayPeriod.period))
    }

    @TypeConverter
    fun toDayPeriod(jsonString: String): DayPeriod {
        val serializable = json.decodeFromString<ConstraintsSerializer.SerializableDayPeriod>(jsonString)
        val day = safeValueOfDayOfWeek(serializable.day)
            ?: throw IllegalArgumentException("Invalid day: ${serializable.day}. Expected one of: ${DayOfWeek.values().joinToString { it.name }}")
        return DayPeriod(day, serializable.period)
    }
}

private object ConstraintsSerializer {
    @kotlinx.serialization.Serializable
    data class SerializableConstraints(
        val weekLoadMin: Int?,
        val weekLoadMax: Int?,
        val dayLoadMin: Int?,
        val dayLoadMax: Int?,
        val excludedDayPeriods: List<SerializableDayPeriod>,
        val excludedSessions: List<SerializableSessionRef>,
        val allowGaps: Boolean,
        val matchGroups: Boolean,
        val preferredInstructors: List<SerializableInstructorPref>,
        val preferredGroups: List<SerializableGroupPref>
    )

    @kotlinx.serialization.Serializable
    data class SerializableDayPeriod(val day: String, val period: Int)

    @kotlinx.serialization.Serializable
    data class SerializableSessionRef(val courseId: String, val type: String, val group: String)

    @kotlinx.serialization.Serializable
    data class SerializableInstructorPref(val courseId: String, val type: String, val instructor: String)

    @kotlinx.serialization.Serializable
    data class SerializableGroupPref(val courseId: String, val type: String, val group: String)
}
