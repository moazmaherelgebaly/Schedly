package com.schedly.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.db.entity.CourseEntity
import com.schedly.data.db.entity.SessionEntity
import com.schedly.domain.error.NotFoundError
import com.schedly.domain.error.RoomConflictException
import com.schedly.domain.error.ValidationError
import com.schedly.domain.model.DayOfWeek
import com.schedly.domain.model.PriorityLevel
import com.schedly.domain.model.Session
import com.schedly.domain.model.SessionType
import com.schedly.domain.repository.ICourseRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.UUID

/**
 * Integration tests for CourseRepositoryImpl.
 * Tests all repository operations with real Room database.
 */
class CourseRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var courseDao: CourseDao
    private lateinit var sessionDao: SessionDao
    private lateinit var repository: ICourseRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        courseDao = database.courseDao()
        sessionDao = database.sessionDao()
        repository = CourseRepositoryImpl(courseDao, sessionDao, database)
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createTestCourse(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Course"
    ) = com.schedly.domain.model.Course(
        id = id,
        name = name,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )

    private fun createTestSession(
        id: UUID = UUID.randomUUID(),
        courseId: UUID,
        type: SessionType = SessionType.LECTURE,
        group: String = "G1",
        day: DayOfWeek = DayOfWeek.SATURDAY,
        period: Int = 1,
        room: String = "A-101",
        priority: PriorityLevel = PriorityLevel.HIGH
    ) = Session(
        id = id,
        courseId = courseId,
        type = type,
        group = group,
        instructor = "Dr. Test",
        day = day,
        period = period,
        room = room,
        priority = priority
    )

    @Test
    fun `getAllCourses returns empty list when no courses exist`() = runTest {
        val courses = repository.getAllCourses()

        assertEquals(0, courses.size)
    }

    @Test
    fun `insertCourse with valid data succeeds`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId, "Data Structures")
        val sessions = listOf(
            createTestSession(courseId = courseId, type = SessionType.LECTURE),
            createTestSession(courseId = courseId, type = SessionType.SECTION, group = "G1", period = 2)
        )

        val result = repository.insertCourse(course, sessions)

        assertTrue(result.isSuccess)
        assertEquals(courseId, result.getOrNull())
    }

    @Test
    fun `insertCourse fails with empty sessions list`() = runTest {
        val course = createTestCourse()

        val result = repository.insertCourse(course, emptyList())

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationError.MissingSessions)
    }

    @Test
    fun `insertCourse fails with only lectures and no sections`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId, type = SessionType.LECTURE),
            createTestSession(courseId = courseId, type = SessionType.LECTURE, group = "G2")
        )

        val result = repository.insertCourse(course, sessions)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationError.MissingSessions)
    }

    @Test
    fun `insertCourse fails with only sections and no lectures`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId, type = SessionType.SECTION),
            createTestSession(courseId = courseId, type = SessionType.SECTION, group = "G2")
        )

        val result = repository.insertCourse(course, sessions)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ValidationError.MissingSessions)
    }

    @Test
    fun `insertCourse fails with invalid period`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId, period = 0), // Invalid period
            createTestSession(courseId = courseId, type = SessionType.SECTION, period = 2)
        )

        val result = repository.insertCourse(course, sessions)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is ValidationError.InvalidPeriod)
        assertTrue(exception?.message?.contains("period", ignoreCase = true) == true)
    }

    @Test
    fun `insertCourse fails with room conflict`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId, day = DayOfWeek.SATURDAY, period = 1, room = "A-101"),
            createTestSession(
                courseId = courseId,
                type = SessionType.SECTION,
                day = DayOfWeek.SATURDAY,
                period = 1,
                room = "A-101" // Same room, day, period
            )
        )

        val result = repository.insertCourse(course, sessions)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RoomConflictException)
    }

    @Test
    fun `getCourseById returns course when exists`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId, "Algorithms")
        val sessions = listOf(
            createTestSession(courseId = courseId),
            createTestSession(courseId = courseId, type = SessionType.SECTION, period = 2)
        )
        repository.insertCourse(course, sessions)

        val retrieved = repository.getCourseById(courseId)

        assertNotNull(retrieved)
        assertEquals("Algorithms", retrieved?.name)
        assertEquals(courseId, retrieved?.id)
    }

    @Test
    fun `getCourseById returns null when course does not exist`() = runTest {
        val nonExistentId = UUID.randomUUID()

        val result = repository.getCourseById(nonExistentId)

        assertNull(result)
    }

    @Test
    fun `getAllCourses returns all courses`() = runTest {
        val course1Id = UUID.randomUUID()
        val course2Id = UUID.randomUUID()

        repository.insertCourse(
            createTestCourse(course1Id, "Course 1"),
            listOf(
                createTestSession(courseId = course1Id),
                createTestSession(courseId = course1Id, type = SessionType.SECTION, period = 2)
            )
        )
        repository.insertCourse(
            createTestCourse(course2Id, "Course 2"),
            listOf(
                createTestSession(courseId = course2Id),
                createTestSession(courseId = course2Id, type = SessionType.SECTION, period = 2)
            )
        )

        val courses = repository.getAllCourses()

        assertEquals(2, courses.size)
    }

    @Test
    fun `updateCourse succeeds with valid data`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId, "Original Name")
        val initialSessions = listOf(
            createTestSession(courseId = courseId),
            createTestSession(courseId = courseId, type = SessionType.SECTION, period = 2)
        )
        repository.insertCourse(course, initialSessions)

        val updatedCourse = course.copy(name = "Updated Name", updatedAt = Instant.now())
        val updatedSessions = listOf(
            createTestSession(courseId = courseId, period = 3), // Changed period
            createTestSession(courseId = courseId, type = SessionType.SECTION, period = 4)
        )

        val result = repository.updateCourse(updatedCourse, updatedSessions)

        assertTrue(result.isSuccess)
        val retrieved = repository.getCourseById(courseId)
        assertEquals("Updated Name", retrieved?.name)
    }

    @Test
    fun `updateCourse fails when course does not exist`() = runTest {
        val nonExistentId = UUID.randomUUID()
        val course = createTestCourse(nonExistentId)
        val sessions = listOf(
            createTestSession(courseId = nonExistentId),
            createTestSession(courseId = nonExistentId, type = SessionType.SECTION)
        )

        val result = repository.updateCourse(course, sessions)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotFoundError)
    }

    @Test
    fun `updateCourse fails with room conflict`() = runTest {
        val courseId1 = UUID.randomUUID()
        val courseId2 = UUID.randomUUID()

        // Insert first course
        repository.insertCourse(
            createTestCourse(courseId1, "Course 1"),
            listOf(
                createTestSession(courseId = courseId1, day = DayOfWeek.SATURDAY, period = 1, room = "A-101"),
                createTestSession(courseId = courseId1, type = SessionType.SECTION, period = 2)
            )
        )

        // Insert second course
        repository.insertCourse(
            createTestCourse(courseId2, "Course 2"),
            listOf(
                createTestSession(courseId = courseId2, day = DayOfWeek.SUNDAY, period = 1, room = "A-102"),
                createTestSession(courseId = courseId2, type = SessionType.SECTION, period = 2)
            )
        )

        // Try to update course 2 with a session that conflicts with course 1
        val updatedSessions = listOf(
            createTestSession(courseId = courseId2, day = DayOfWeek.SATURDAY, period = 1, room = "A-101"),
            createTestSession(courseId = courseId2, type = SessionType.SECTION, period = 2)
        )

        val result = repository.updateCourse(createTestCourse(courseId2), updatedSessions)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RoomConflictException)
    }

    @Test
    fun `deleteCourse removes course and all sessions`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId),
            createTestSession(courseId = courseId, type = SessionType.SECTION, period = 2)
        )
        repository.insertCourse(course, sessions)

        val result = repository.deleteCourse(courseId)

        assertTrue(result.isSuccess)
        assertNull(repository.getCourseById(courseId))
        assertEquals(0, repository.getSessionsForCourse(courseId).size)
    }

    @Test
    fun `deleteCourse fails when course does not exist`() = runTest {
        val nonExistentId = UUID.randomUUID()

        val result = repository.deleteCourse(nonExistentId)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NotFoundError)
    }

    @Test
    fun `getSessionsForCourse returns all sessions for that course`() = runTest {
        val courseId = UUID.randomUUID()
        val course = createTestCourse(courseId)
        val sessions = listOf(
            createTestSession(courseId = courseId, group = "G1", period = 1),
            createTestSession(courseId = courseId, group = "G2", period = 2),
            createTestSession(courseId = courseId, type = SessionType.SECTION, group = "G1", period = 3),
            createTestSession(courseId = courseId, type = SessionType.SECTION, group = "G2", period = 4)
        )
        repository.insertCourse(course, sessions)

        val retrievedSessions = repository.getSessionsForCourse(courseId)

        assertEquals(4, retrievedSessions.size)
    }

    @Test
    fun `getSessionsForCourse returns empty list when course has no sessions`() = runTest {
        // Course without inserting any sessions

        val sessions = repository.getSessionsForCourse(UUID.randomUUID())

        assertEquals(0, sessions.size)
    }
}
