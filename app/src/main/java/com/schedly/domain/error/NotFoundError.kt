package com.schedly.domain.error

import java.util.UUID

data class NotFoundError(
    val entityType: String,
    val id: UUID
) : Exception("$entityType with ID $id not found")
