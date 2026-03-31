package com.schedly.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.schedly.domain.model.DayOfWeek
import com.schedly.domain.model.PriorityLevel
import com.schedly.domain.model.Session
import com.schedly.domain.model.SessionType
import java.util.UUID

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["id"],
        childColumns = ["courseId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("courseId"),
        Index("day", "period"),
        Index("day", "period", "room", unique = true)
    ]
)
data class SessionEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val type: String,
    val group: String,
    val instructor: String,
    val day: String,
    val period: Int,
    val room: String,
    val priority: String
) {
    fun toDomain(): Session {
        return Session(
            id = UUID.fromString(id),
            courseId = UUID.fromString(courseId),
            type = SessionType.valueOf(type),
            group = group,
            instructor = instructor,
            day = DayOfWeek.valueOf(day),
            period = period,
            room = room,
            priority = PriorityLevel.valueOf(priority)
        )
    }

    companion object {
        fun fromDomain(session: Session): SessionEntity {
            return SessionEntity(
                id = session.id.toString(),
                courseId = session.courseId.toString(),
                type = session.type.name,
                group = session.group,
                instructor = session.instructor,
                day = session.day.name,
                period = session.period,
                room = session.room,
                priority = session.priority.name
            )
        }
    }
}
