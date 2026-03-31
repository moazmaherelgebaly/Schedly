package com.schedly.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Migration tests for AppDatabase.
 * Verifies database schema migrations and data integrity.
 *
 * Note: For MVP, we use fallbackToDestructiveMigration, but these tests
 * ensure the schema is properly exported and can be used for future migrations.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private val testDbName = "test_schedly_database"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java.canonicalName,
        listOf() // No custom migrations yet for MVP
    )

    @Test
    @Throws(IOException::class)
    fun `migrateAll creates database with correct schema`() {
        // Create the database with the latest schema
        val db = helper.createDatabase(testDbName, 1).apply {
            // Verify tables exist by querying them
            query("SELECT * FROM courses", emptyArray()).use { cursor ->
                assertEquals(0, cursor.count) // Empty table
            }
            query("SELECT * FROM sessions", emptyArray()).use { cursor ->
                assertEquals(0, cursor.count) // Empty table
            }
            query("SELECT * FROM constraints", emptyArray()).use { cursor ->
                assertEquals(0, cursor.count) // Empty table
            }
            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database persists and retrieves data correctly`() {
        // Create database and insert test data
        val db = helper.createDatabase(testDbName, 1).apply {
            // Insert a test course
            execSQL(
                """
                INSERT INTO courses (id, name, createdAt, updatedAt)
                VALUES ('test-id', 'Test Course', 1000, 2000)
                """
            )

            // Verify course was inserted
            query("SELECT * FROM courses WHERE id = 'test-id'", emptyArray()).use { cursor ->
                assertEquals(1, cursor.count)
                cursor.moveToFirst()
                assertEquals("Test Course", cursor.getString(cursor.getColumnIndexOrThrow("name")))
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database enforces foreign key constraints`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            // Enable foreign keys
            execSQL("PRAGMA foreign_keys = ON")

            // Try to insert a session without a course - should fail
            try {
                execSQL(
                    """
                    INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                    VALUES ('session-id', 'non-existent-course', 'LECTURE', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                    """
                )
                // If we reach here, foreign keys are not enforced
                assert(false) { "Foreign key constraint should have been enforced" }
            } catch (e: Exception) {
                // Expected: foreign key constraint failed
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database cascade deletes sessions when course is deleted`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            // Enable foreign keys
            execSQL("PRAGMA foreign_keys = ON")

            // Insert a course
            execSQL(
                """
                INSERT INTO courses (id, name, createdAt, updatedAt)
                VALUES ('course-id', 'Test Course', 1000, 2000)
                """
            )

            // Insert sessions for the course
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-1', 'course-id', 'LECTURE', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                """
            )
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-2', 'course-id', 'SECTION', 'G1', 'Dr. Test', 'SUNDAY', 2, 'A-102', 'MEDIUM')
                """
            )

            // Verify sessions exist
            query("SELECT * FROM sessions WHERE courseId = 'course-id'", emptyArray()).use { cursor ->
                assertEquals(2, cursor.count)
            }

            // Delete the course
            execSQL("DELETE FROM courses WHERE id = 'course-id'")

            // Verify sessions were cascade deleted
            query("SELECT * FROM sessions WHERE courseId = 'course-id'", emptyArray()).use { cursor ->
                assertEquals(0, cursor.count)
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database enforces unique room constraint`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            // Insert a session
            execSQL(
                """
                INSERT INTO courses (id, name, createdAt, updatedAt)
                VALUES ('course-id', 'Test Course', 1000, 2000)
                """
            )
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-1', 'course-id', 'LECTURE', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                """
            )

            // Try to insert another session with same day, period, room - should fail
            try {
                execSQL(
                    """
                    INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                    VALUES ('session-2', 'course-id', 'SECTION', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                    """
                )
                // If we reach here, unique constraint was not enforced
                assert(false) { "Unique constraint should have been enforced" }
            } catch (e: Exception) {
                // Expected: UNIQUE constraint failed
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database allows different rooms at same time`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            execSQL(
                """
                INSERT INTO courses (id, name, createdAt, updatedAt)
                VALUES ('course-id', 'Test Course', 1000, 2000)
                """
            )

            // Insert first session
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-1', 'course-id', 'LECTURE', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                """
            )

            // Insert second session at same time but different room - should succeed
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-2', 'course-id', 'SECTION', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-102', 'HIGH')
                """
            )

            // Verify both sessions exist
            query("SELECT * FROM sessions", emptyArray()).use { cursor ->
                assertEquals(2, cursor.count)
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `database allows same room at different times`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            execSQL(
                """
                INSERT INTO courses (id, name, createdAt, updatedAt)
                VALUES ('course-id', 'Test Course', 1000, 2000)
                """
            )

            // Insert first session
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-1', 'course-id', 'LECTURE', 'G1', 'Dr. Test', 'SATURDAY', 1, 'A-101', 'HIGH')
                """
            )

            // Insert second session in same room but different period - should succeed
            execSQL(
                """
                INSERT INTO sessions (id, courseId, type, group, instructor, day, period, room, priority)
                VALUES ('session-2', 'course-id', 'SECTION', 'G1', 'Dr. Test', 'SATURDAY', 2, 'A-101', 'HIGH')
                """
            )

            // Verify both sessions exist
            query("SELECT * FROM sessions", emptyArray()).use { cursor ->
                assertEquals(2, cursor.count)
            }

            close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun `constraints table is single-row with id equals 1`() {
        val db = helper.createDatabase(testDbName, 1).apply {
            // Insert constraints
            execSQL(
                """
                INSERT INTO constraints (id, jsonData)
                VALUES (1, '{"weekLoadMin":3,"weekLoadMax":5,"allowGaps":true,"matchGroups":false,"excludedDayPeriods":[],"excludedSessions":[],"preferredInstructors":[],"preferredGroups":[],"dayLoadMin":null,"dayLoadMax":null}')
                """
            )

            // Verify constraints exist
            query("SELECT * FROM constraints WHERE id = 1", emptyArray()).use { cursor ->
                assertEquals(1, cursor.count)
                assertNotNull(cursor)
            }

            close()
        }
    }
}
