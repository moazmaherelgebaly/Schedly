package com.schedly.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.dao.SessionDao
import com.schedly.data.db.entity.CourseEntity
import com.schedly.data.db.entity.SessionEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for SessionDao.
 * Tests all CRUD operations for sessions and room uniqueness constraint.
 */
class SessionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var sessionDao: SessionDao
    private lateinit var courseDao: CourseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDao = database.sessionDao()
        courseDao = database.courseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createTestCourse(id: String = "test-course"): CourseEntity {
        return CourseEntity(
            id = id,
            name = "Test Course",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )
    }

    private fun createTestSession(
        id: String = "session-1",
        courseId: String = "test-course",
        day: String = "SATURDAY",
        period: Int = 1,
        room: String = "A-101"
    ): SessionEntity {
        return SessionEntity(
            id = id,
            courseId = courseId,
            type = "LECTURE",
            group = "G1",
            instructor = "Dr. Test",
            day = day,
            period = period,
            room = room,
            priority = "HIGH"
        )
    }

    @Test
    fun `insertSession adds session to database`() = runTest {
        courseDao.insert(createTestCourse())
        val session = createTestSession()

        val rowId = sessionDao.insert(session)

        assert(rowId > 0) { "Insert should return positive row ID" }
    }

    @Test
    fun `getAll returns all sessions`() = runTest {
        courseDao.insert(createTestCourse())
        val session1 = createTestSession("session-1")
        val session2 = createTestSession("session-2", period = 2)

        sessionDao.insert(session1)
        sessionDao.insert(session2)

        val sessions = sessionDao.getAll()

        assertEquals(2, sessions.size)
    }

    @Test
    fun `getSessionsForCourse returns only sessions for that course`() = runTest {
        courseDao.insert(createTestCourse("course-1"))
        courseDao.insert(createTestCourse("course-2"))

        val session1 = createTestSession("s1", "course-1")
        val session2 = createTestSession("s2", "course-1")
        val session3 = createTestSession("s3", "course-2")

        sessionDao.insert(session1)
        sessionDao.insert(session2)
        sessionDao.insert(session3)

        val course1Sessions = sessionDao.getSessionsForCourse("course-1")

        assertEquals(2, course1Sessions.size)
        assertTrue(course1Sessions.all { it.courseId == "course-1" })
    }

    @Test
    fun `getSessionsForCourse returns empty list when no sessions exist`() = runTest {
        val sessions = sessionDao.getSessionsForCourse("non-existent-course")

        assertEquals(0, sessions.size)
    }

    @Test
    fun `updateSession modifies existing session`() = runTest {
        courseDao.insert(createTestCourse())
        val session = createTestSession()
        sessionDao.insert(session)

        val updatedSession = session.copy(instructor = "Updated Instructor")
        sessionDao.update(updatedSession)

        val retrieved = sessionDao.getAll().first { it.id == "session-1" }

        assertEquals("Updated Instructor", retrieved.instructor)
    }

    @Test
    fun `deleteSession removes session from database`() = runTest {
        courseDao.insert(createTestCourse())
        val session = createTestSession()
        sessionDao.insert(session)

        sessionDao.delete(session)

        val sessions = sessionDao.getAll()
        assertEquals(0, sessions.size)
    }

    @Test
    fun `deleteSessionsForCourse removes all sessions for that course`() = runTest {
        courseDao.insert(createTestCourse("course-1"))
        courseDao.insert(createTestCourse("course-2"))

        sessionDao.insert(createTestSession("s1", "course-1"))
        sessionDao.insert(createTestSession("s2", "course-1"))
        sessionDao.insert(createTestSession("s3", "course-2"))

        sessionDao.deleteSessionsForCourse("course-1")

        val course1Sessions = sessionDao.getSessionsForCourse("course-1")
        val course2Sessions = sessionDao.getSessionsForCourse("course-2")

        assertEquals(0, course1Sessions.size)
        assertEquals(1, course2Sessions.size)
    }

    @Test
    fun `insertSession fails when room is occupied at same day and period`() = runTest {
        courseDao.insert(createTestCourse())
        val session1 = createTestSession("session-1", "test-course", "SATURDAY", 1, "A-101")
        sessionDao.insert(session1)

        val session2 = createTestSession("session-2", "test-course", "SATURDAY", 1, "A-101")
        try {
            sessionDao.insert(session2)
            throw AssertionError("Insert should throw SQLiteConstraintException for room conflict")
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Expected
        }
    }

    @Test
    fun `insertSession succeeds when room is occupied at different period`() = runTest {
        courseDao.insert(createTestCourse())
        val session1 = createTestSession("session-1", "test-course", "SATURDAY", 1, "A-101")
        sessionDao.insert(session1)

        val session2 = createTestSession("session-2", "test-course", "SATURDAY", 2, "A-101")
        val rowId = sessionDao.insert(session2)

        assert(rowId > 0) { "Insert should succeed for different period" }
    }

    @Test
    fun `insertSession succeeds when room is occupied on different day`() = runTest {
        courseDao.insert(createTestCourse())
        val session1 = createTestSession("session-1", "test-course", "SATURDAY", 1, "A-101")
        sessionDao.insert(session1)

        val session2 = createTestSession("session-2", "test-course", "SUNDAY", 1, "A-101")
        val rowId = sessionDao.insert(session2)

        assert(rowId > 0) { "Insert should succeed for different day" }
    }

    @Test
    fun `insertSession succeeds when different room at same day and period`() = runTest {
        courseDao.insert(createTestCourse())
        val session1 = createTestSession("session-1", "test-course", "SATURDAY", 1, "A-101")
        sessionDao.insert(session1)

        val session2 = createTestSession("session-2", "test-course", "SATURDAY", 1, "A-102")
        val rowId = sessionDao.insert(session2)

        assert(rowId > 0) { "Insert should succeed for different room" }
    }

    @Test
    fun `isRoomOccupied returns true when room is occupied`() = runTest {
        courseDao.insert(createTestCourse())
        val session = createTestSession()
        sessionDao.insert(session)

        val isOccupied = sessionDao.isRoomOccupied("SATURDAY", 1, "A-101", null)

        assertTrue(isOccupied)
    }

    @Test
    fun `isRoomOccupied returns false when room is not occupied`() = runTest {
        val isOccupied = sessionDao.isRoomOccupied("SATURDAY", 1, "A-101", null)

        assertFalse(isOccupied)
    }

    @Test
    fun `isRoomOccupied returns false when checking same session being updated`() = runTest {
        courseDao.insert(createTestCourse())
        val session = createTestSession("session-1")
        sessionDao.insert(session)

        // When updating the same session, excludeId should prevent self-conflict
        val isOccupied = sessionDao.isRoomOccupied("SATURDAY", 1, "A-101", "session-1")

        assertFalse(isOccupied)
    }

    @Test
    fun `isRoomOccupied returns true when different session occupies the room`() = runTest {
        courseDao.insert(createTestCourse())
        sessionDao.insert(createTestSession("session-1"))
        sessionDao.insert(createTestSession("session-2", "test-course", "SATURDAY", 2, "A-101"))

        // Check if session-2's room is occupied (should be true due to session-2 itself)
        val isOccupied = sessionDao.isRoomOccupied("SATURDAY", 2, "A-101", "session-1")

        assertTrue(isOccupied)
    }
}
