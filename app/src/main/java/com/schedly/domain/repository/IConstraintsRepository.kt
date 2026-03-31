package com.schedly.domain.repository

import com.schedly.domain.error.ValidationResult
import com.schedly.domain.model.Constraints

interface IConstraintsRepository {
    suspend fun getConstraints(): Constraints?
    suspend fun saveConstraints(constraints: Constraints): Result<Unit>
    suspend fun validateConstraints(constraints: Constraints): ValidationResult
}
