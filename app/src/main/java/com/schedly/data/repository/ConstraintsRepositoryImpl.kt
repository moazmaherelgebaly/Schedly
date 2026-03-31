package com.schedly.data.repository

import com.schedly.data.db.dao.ConstraintsDao
import com.schedly.data.db.entity.ConstraintsEntity
import com.schedly.domain.error.ValidationResult
import com.schedly.domain.model.Constraints
import com.schedly.domain.repository.IConstraintsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConstraintsRepositoryImpl(
    private val constraintsDao: ConstraintsDao
) : IConstraintsRepository {

    override suspend fun getConstraints(): Constraints? = withContext(Dispatchers.IO) {
        constraintsDao.getConstraints()?.toDomain()
    }

    override suspend fun saveConstraints(constraints: Constraints): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val validationErrors = constraints.validate()
            if (validationErrors.isNotEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid constraints: ${validationErrors.joinToString("; ")}")
                )
            }

            val entity = ConstraintsEntity.fromDomain(constraints)
            constraintsDao.saveConstraints(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateConstraints(constraints: Constraints): ValidationResult = withContext(Dispatchers.IO) {
        val errors = constraints.validate()
        if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }
}
