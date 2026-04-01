package com.schedly.domain.repository

import com.schedly.domain.error.ValidationResult
import com.schedly.domain.model.Constraints

/**
 * Repository interface for Constraints operations.
 * 
 * This interface defines the contract for storing and retrieving user-defined
 * scheduling constraints. Constraints are persisted as a single-row JSON blob
 * in the database, representing all 8 constraint types:
 * 
 * - Week Load (min/max distinct days)
 * - Day Load (min/max sessions per day)
 * - Excluded Day-Periods
 * - Excluded Sessions
 * - Allow Gaps
 * - Match Groups
 * - Preferred Instructors
 * - Preferred Groups
 * 
 * @see Constraints
 */
interface IConstraintsRepository {
    /**
     * Retrieve the stored constraints.
     *
     * @return The constraints if saved, null if none exist
     */
    suspend fun getConstraints(): Constraints?

    /**
     * Save constraints to the database.
     *
     * Constraints are serialized as JSON and stored in a single-row table.
     * Validation is performed before persistence.
     *
     * @param constraints The constraints to save
     * @return Result indicating success or failure
     */
    suspend fun saveConstraints(constraints: Constraints): Result<Unit>

    /**
     * Validate constraints without persisting them.
     *
     * Checks:
     * - Min/max ranges are valid (min ≤ max)
     * - Week load values are between 1-6
     * - Day load values are between 0-6
     * - Period values are between 1-6
     *
     * @param constraints The constraints to validate
     * @return ValidationResult indicating valid or list of errors
     */
    suspend fun validateConstraints(constraints: Constraints): ValidationResult
}
