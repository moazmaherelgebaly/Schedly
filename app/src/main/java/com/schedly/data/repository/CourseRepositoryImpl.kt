package com.schedly.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.db.entity.CourseEntity
import com.schedly.data.db.entity.SessionEntity
import com.schedly.domain.error.NotFoundError
import com.schedly.domain.error.RoomConflictException
import com.schedly.domain.error.ValidationError
import com.schedly.domain.model.Course
import com.schedly.domain.model.Session
import com.schedly.domain.model.SessionType
import com.schedly.domain.repository.ICourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class CourseRepositoryImpl(
    private val courseDao: CourseDao,
    private val sessionDao: SessionDao,
    private val database: AppDatabase
) : ICourseRepository {

    override suspend fun getAllCourses(): List<Course> = withContext(Dispatchers.IO) {
        courseDao.getAll().map { it.toDomain() }
    }

    override suspend fun getCourseById(id: UUID): Course? = withContext(Dispatchers.IO) {
        courseDao.getById(id.toString())?.toDomain()
    }

    override suspend fun insertCourse(course: Course, sessions: List<Session>): Result<UUID> = withContext(Dispatchers.IO) {
        try {
            if (!course.isValid()) {
                return@withContext Result.failure(ValidationError.InvalidCourseName("Course name must be non-empty and max 100 characters"))
            }

            if (sessions.isEmpty()) {
                return@withContext Result.failure(ValidationError.MissingSessions("Course must have at least one session"))
            }

            val hasLecture = sessions.any { it.type == SessionType.LECTURE }
            val hasSection = sessions.any { it.type == SessionType.SECTION }
            if (!hasLecture || !hasSection) {
                return@withContext Result.failure(ValidationError.MissingSessions("Course must have at least one lecture and one section"))
            }

            sessions.forEach { session ->
                if (!session.isValid()) {
                    return@withContext Result.failure(ValidationError.InvalidPeriod(session.period, "Invalid session data"))
                }
            }

            database.withTransaction {
                try {
                    val courseEntity = CourseEntity.fromDomain(course)
                    val rowId = courseDao.insert(courseEntity)
                    if (rowId == -1L) {
                        throw ValidationError.InvalidCourseName("Failed to insert course")
                    }

                    sessions.forEach { session ->
                        val sessionEntity = SessionEntity.fromDomain(session.copy(courseId = course.id))
                        val sessionRowId = sessionDao.insert(sessionEntity)
                        if (sessionRowId == -1L) {
                            throw RoomConflictException(session.day, session.period, session.room)
                        }
                    }
                } catch (e: SQLiteConstraintException) {
                    throw RoomConflictException(null, null, null, "Room conflict detected during insert")
                }
            }

            Result.success(course.id)
        } catch (e: RoomConflictException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCourse(course: Course, sessions: List<Session>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            courseDao.getById(course.id.toString())
                ?: return@withContext Result.failure(NotFoundError("Course", course.id))

            if (!course.isValid()) {
                return@withContext Result.failure(ValidationError.InvalidCourseName("Course name must be non-empty and max 100 characters"))
            }

            if (sessions.isEmpty()) {
                return@withContext Result.failure(ValidationError.MissingSessions("Course must have at least one session"))
            }

            val hasLecture = sessions.any { it.type == SessionType.LECTURE }
            val hasSection = sessions.any { it.type == SessionType.SECTION }
            if (!hasLecture || !hasSection) {
                return@withContext Result.failure(ValidationError.MissingSessions("Course must have at least one lecture and one section"))
            }

            sessions.forEach { session ->
                if (!session.isValid()) {
                    return@withContext Result.failure(ValidationError.InvalidPeriod(session.period, "Invalid session data"))
                }
            }

            database.withTransaction {
                try {
                    val courseEntity = CourseEntity.fromDomain(course)
                    courseDao.update(courseEntity)

                    sessionDao.deleteSessionsForCourse(course.id.toString())

                    sessions.forEach { session ->
                        val sessionEntity = SessionEntity.fromDomain(session.copy(courseId = course.id))
                        val sessionRowId = sessionDao.insert(sessionEntity)
                        if (sessionRowId == -1L) {
                            throw RoomConflictException(session.day, session.period, session.room)
                        }
                    }
                } catch (e: SQLiteConstraintException) {
                    throw RoomConflictException(null, null, null, "Room conflict detected during update")
                }
            }

            Result.success(Unit)
        } catch (e: RoomConflictException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCourse(id: UUID): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val course = courseDao.getById(id.toString())
                ?: return@withContext Result.failure(NotFoundError("Course", id))

            courseDao.delete(course)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessionsForCourse(courseId: UUID): List<Session> = withContext(Dispatchers.IO) {
        sessionDao.getSessionsForCourse(courseId.toString()).map { it.toDomain() }
    }
}
