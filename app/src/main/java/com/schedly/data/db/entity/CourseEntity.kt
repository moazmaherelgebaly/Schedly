package com.schedly.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.schedly.domain.model.Course
import java.time.Instant
import java.util.UUID

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): Course {
        return Course(
            id = UUID.fromString(id),
            name = name,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }

    companion object {
        fun fromDomain(course: Course): CourseEntity {
            return CourseEntity(
                id = course.id.toString(),
                name = course.name,
                createdAt = course.createdAt.toEpochMilli(),
                updatedAt = course.updatedAt.toEpochMilli()
            )
        }
    }
}
