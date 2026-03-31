package com.schedly.domain.model

import java.time.Instant
import java.util.UUID

data class Course(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val createdAt: Instant = Instant.ofEpochMilli(System.currentTimeMillis()),
    val updatedAt: Instant = Instant.ofEpochMilli(System.currentTimeMillis())
) {
    fun isValid(): Boolean = name.isNotBlank() && name.length <= 100
}
