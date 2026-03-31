package com.schedly.domain.error

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult()

    fun isValid(): Boolean = this is Valid
    fun getErrorList(): List<String> = when (this) {
        is Valid -> emptyList()
        is Invalid -> errors
    }

    companion object {
        fun fromErrors(errors: List<String>): ValidationResult =
            if (errors.isEmpty()) Valid else Invalid(errors)
    }
}
