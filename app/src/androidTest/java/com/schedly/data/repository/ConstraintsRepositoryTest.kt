package com.schedly.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.schedly.data.db.AppDatabase
import com.schedly.data.db.dao.ConstraintsDao
import com.schedly.domain.error.ValidationResult
import com.schedly.domain.model.Constraints
import com.schedly.domain.model.DayOfWeek
import com.schedly.domain.model.DayPeriod
import com.schedly.domain.model.GroupPref
import com.schedly.domain.model.InstructorPref
import com.schedly.domain.model.SessionRef
import com.schedly.domain.model.SessionType
import com.schedly.domain.repository.IConstraintsRepository
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Integration tests for ConstraintsRepositoryImpl.
 * Tests save, retrieve, and validate operations.
 */
class ConstraintsRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var constraintsDao: ConstraintsDao
    private lateinit var repository: IConstraintsRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        constraintsDao = database.constraintsDao()
        repository = ConstraintsRepositoryImpl(constraintsDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `getConstraints returns null when no constraints saved`() = runTest {
        val constraints = repository.getConstraints()

        assertNull(constraints)
    }

    @Test
    fun `saveConstraints with valid data succeeds`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 1,
            dayLoadMax = 3,
            allowGaps = true,
            matchGroups = false
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `saveConstraints persists and retrieves correctly`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 2,
            weekLoadMax = 4,
            dayLoadMin = 1,
            dayLoadMax = 2,
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.SUNDAY, 2)
            ),
            allowGaps = false,
            matchGroups = true
        )

        repository.saveConstraints(constraints)
        val retrieved = repository.getConstraints()

        assertNotNull(retrieved)
        assertEquals(2, retrieved?.weekLoadMin)
        assertEquals(4, retrieved?.weekLoadMax)
        assertEquals(1, retrieved?.dayLoadMin)
        assertEquals(2, retrieved?.dayLoadMax)
        assertEquals(2, retrieved?.excludedDayPeriods?.size)
        assertEquals(false, retrieved?.allowGaps)
        assertEquals(true, retrieved?.matchGroups)
    }

    @Test
    fun `saveConstraints with all constraint types persists correctly`() = runTest {
        val courseId = UUID.randomUUID()
        val sessionId = UUID.randomUUID()

        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 1,
            dayLoadMax = 3,
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 1),
                DayPeriod(DayOfWeek.THURSDAY, 6)
            ),
            excludedSessions = listOf(
                SessionRef(courseId, sessionId)
            ),
            allowGaps = true,
            matchGroups = false,
            preferredInstructors = listOf(
                InstructorPref(courseId, SessionType.LECTURE, "Dr. Smith")
            ),
            preferredGroups = listOf(
                GroupPref(courseId, SessionType.SECTION, "G1")
            )
        )

        repository.saveConstraints(constraints)
        val retrieved = repository.getConstraints()

        assertNotNull(retrieved)
        assertEquals(2, retrieved?.excludedDayPeriods?.size)
        assertEquals(1, retrieved?.excludedSessions?.size)
        assertEquals(1, retrieved?.preferredInstructors?.size)
        assertEquals(1, retrieved?.preferredGroups?.size)
        assertEquals("Dr. Smith", retrieved?.preferredInstructors?.first()?.instructor)
        assertEquals("G1", retrieved?.preferredGroups?.first()?.group)
    }

    @Test
    fun `saveConstraints rejects weekLoad min greater than max`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 5,
            weekLoadMax = 3, // Invalid: min > max
            dayLoadMin = 1,
            dayLoadMax = 2
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isFailure)
    }

    @Test
    fun `saveConstraints rejects dayLoad min greater than max`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 4,
            dayLoadMax = 2 // Invalid: min > max
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isFailure)
    }

    @Test
    fun `saveConstraints rejects invalid weekLoad range`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 0, // Invalid: must be 1-6
            weekLoadMax = 7, // Invalid: must be 1-6
            dayLoadMin = 1,
            dayLoadMax = 2
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isFailure)
    }

    @Test
    fun `saveConstraints rejects invalid dayLoad range`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = -1, // Invalid: must be 0-6
            dayLoadMax = 7   // Invalid: must be 0-6
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isFailure)
    }

    @Test
    fun `saveConstraints rejects excludedDayPeriods with invalid period`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 0), // Invalid: must be 1-6
                DayPeriod(DayOfWeek.SUNDAY, 7)    // Invalid: must be 1-6
            )
        )

        val result = repository.saveConstraints(constraints)

        assertTrue(result.isFailure)
    }

    @Test
    fun `saveConstraints replaces existing constraints`() = runTest {
        val constraints1 = Constraints(
            weekLoadMin = 2,
            weekLoadMax = 4,
            allowGaps = true
        )
        repository.saveConstraints(constraints1)

        val constraints2 = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            allowGaps = false
        )
        repository.saveConstraints(constraints2)

        val retrieved = repository.getConstraints()

        assertNotNull(retrieved)
        assertEquals(3, retrieved?.weekLoadMin)
        assertEquals(5, retrieved?.weekLoadMax)
        assertEquals(false, retrieved?.allowGaps)
    }

    @Test
    fun `validateConstraints returns Valid for valid constraints`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 3,
            weekLoadMax = 5,
            dayLoadMin = 1,
            dayLoadMax = 3,
            allowGaps = true
        )

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateConstraints returns Invalid for weekLoad violation`() = runTest {
        val constraints = Constraints(
            weekLoadMin = 5,
            weekLoadMax = 3 // Invalid
        )

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.errors.any { it.contains("week load", ignoreCase = true) })
    }

    @Test
    fun `validateConstraints returns Invalid for dayLoad violation`() = runTest {
        val constraints = Constraints(
            dayLoadMin = 4,
            dayLoadMax = 2 // Invalid
        )

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.errors.any { it.contains("day load", ignoreCase = true) })
    }

    @Test
    fun `validateConstraints returns Invalid for excludedDayPeriods violation`() = runTest {
        val constraints = Constraints(
            excludedDayPeriods = listOf(
                DayPeriod(DayOfWeek.SATURDAY, 7) // Invalid period
            )
        )

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Invalid)
        val invalid = result as ValidationResult.Invalid
        assertTrue(invalid.errors.any { it.contains("period", ignoreCase = true) })
    }

    @Test
    fun `validateConstraints returns Valid for empty constraints`() = runTest {
        val constraints = Constraints()

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Valid)
    }

    @Test
    fun `validateConstraints returns Valid when only some fields are set`() = runTest {
        val constraints = Constraints(
            weekLoadMax = 5,
            allowGaps = false,
            matchGroups = true
        )

        val result = repository.validateConstraints(constraints)

        assertTrue(result is ValidationResult.Valid)
    }
}
