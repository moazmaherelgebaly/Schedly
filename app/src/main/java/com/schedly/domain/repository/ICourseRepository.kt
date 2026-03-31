package com.schedly.domain.repository

import com.schedly.domain.model.Course
import com.schedly.domain.model.Session
import java.util.UUID

/**
 * Repository interface for Course operations.
 * 
 * This interface defines the contract for course data operations, providing
 * an abstraction layer between the domain and data layers. This enables
 * clean architecture and facilitates KMP migration by keeping domain logic
 * independent of Android-specific implementations.
 * 
 * @see Course
 * @see Session
 */
interface ICourseRepository {
    /**
     * Retrieve all courses from the database.
     *
     * @return List of all courses with their associated sessions
     */
    suspend fun getAllCourses(): List<Course>

    /**
     * Retrieve a specific course by its ID.
     *
     * @param id The unique identifier of the course
     * @return The course if found, null otherwise
     */
    suspend fun getCourseById(id: UUID): Course?

    /**
     * Insert a new course with its associated sessions.
     *
     * Validates that:
     * - Course name is non-empty and ≤100 characters
     * - At least one lecture and one section exist
     * - No room conflicts (same day, period, room)
     *
     * @param course The course to insert
     * @param sessions List of sessions (lectures and sections) for this course
     * @return Result containing the course ID on success, or an error
     */
    suspend fun insertCourse(course: Course, sessions: List<Session>): Result<UUID>

    /**
     * Update an existing course and its sessions.
     *
     * Performs the same validations as [insertCourse].
     * Existing sessions are deleted and replaced with the new list.
     *
     * @param course The updated course
     * @param sessions The updated list of sessions
     * @return Result indicating success or failure
     */
    suspend fun updateCourse(course: Course, sessions: List<Session>): Result<Unit>

    /**
     * Delete a course and all its associated sessions.
     *
     * Sessions are automatically deleted via CASCADE foreign key constraint.
     *
     * @param id The unique identifier of the course to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteCourse(id: UUID): Result<Unit>

    /**
     * Retrieve all sessions for a specific course.
     *
     * @param courseId The unique identifier of the course
     * @return List of sessions (lectures and sections) for the course
     */
    suspend fun getSessionsForCourse(courseId: UUID): List<Session>
}
