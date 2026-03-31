package com.schedly.data.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.CourseDao
import com.schedly.data.db.entity.CourseEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.Instant

/**
 * Unit tests for CourseDao.
 * Tests all CRUD operations for courses.
 */
class CourseDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var courseDao: CourseDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        courseDao = database.courseDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertCourse adds course to database`() = runTest {
        val course = CourseEntity(
            id = "test-course-id-1",
            name = "Data Structures",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )

        val rowId = courseDao.insert(course)

        assert(rowId > 0) { "Insert should return positive row ID" }
    }

    @Test
    fun `getAll returns all courses ordered by name`() = runTest {
        val course1 = CourseEntity(
            id = "course-z",
            name = "Zebra Course",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )
        val course2 = CourseEntity(
            id = "course-a",
            name = "Alpha Course",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )

        courseDao.insert(course1)
        courseDao.insert(course2)

        val courses = courseDao.getAll()

        assertEquals(2, courses.size)
        assertEquals("Alpha Course", courses[0].name)
        assertEquals("Zebra Course", courses[1].name)
    }

    @Test
    fun `getById returns course when exists`() = runTest {
        val course = CourseEntity(
            id = "specific-course-id",
            name = "Test Course",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )
        courseDao.insert(course)

        val retrieved = courseDao.getById("specific-course-id")

        assertNotNull(retrieved)
        assertEquals("Test Course", retrieved?.name)
        assertEquals("specific-course-id", retrieved?.id)
    }

    @Test
    fun `getById returns null when course does not exist`() = runTest {
        val result = courseDao.getById("non-existent-id")

        assertNull(result)
    }

    @Test
    fun `updateCourse modifies existing course`() = runTest {
        val course = CourseEntity(
            id = "update-test-id",
            name = "Original Name",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )
        courseDao.insert(course)

        val updatedCourse = course.copy(
            name = "Updated Name",
            updatedAt = Instant.now().toEpochMilli()
        )
        courseDao.update(updatedCourse)

        val retrieved = courseDao.getById("update-test-id")

        assertEquals("Updated Name", retrieved?.name)
    }

    @Test
    fun `deleteCourse removes course from database`() = runTest {
        val course = CourseEntity(
            id = "delete-test-id",
            name = "To Be Deleted",
            createdAt = Instant.now().toEpochMilli(),
            updatedAt = Instant.now().toEpochMilli()
        )
        courseDao.insert(course)

        courseDao.delete(course)

        val retrieved = courseDao.getById("delete-test-id")
        assertNull(retrieved)
    }

    @Test
    fun `getAll returns empty list when no courses exist`() = runTest {
        val courses = courseDao.getAll()

        assertEquals(0, courses.size)
    }

    @Test
    fun `insertCourse with REPLACE overwrites existing course`() = runTest {
        val course1 = CourseEntity(
            id = "replace-test-id",
            name = "Original Name",
            createdAt = 1000L,
            updatedAt = 1000L
        )
        courseDao.insert(course1)

        val course2 = CourseEntity(
            id = "replace-test-id",
            name = "New Name",
            createdAt = 2000L,
            updatedAt = 2000L
        )
        courseDao.insert(course2)

        val courses = courseDao.getAll()
        assertEquals(1, courses.size)
        assertEquals("New Name", courses[0].name)
    }
}
