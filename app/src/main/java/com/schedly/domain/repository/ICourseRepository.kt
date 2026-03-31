package com.schedly.domain.repository

import com.schedly.domain.model.Course
import com.schedly.domain.model.Session
import java.util.UUID

interface ICourseRepository {
    suspend fun getAllCourses(): List<Course>
    suspend fun getCourseById(id: UUID): Course?
    suspend fun insertCourse(course: Course, sessions: List<Session>): Result<UUID>
    suspend fun updateCourse(course: Course, sessions: List<Session>): Result<Unit>
    suspend fun deleteCourse(id: UUID): Result<Unit>
    suspend fun getSessionsForCourse(courseId: UUID): List<Session>
}
