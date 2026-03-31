package com.schedly.domain.model

/**
 * Diagnostic information when zero schedules are found.
 * Identifies which constraint eliminated the most candidates.
 */
data class DiagnosticInfo(
    val mostRestrictiveConstraint: String,
    val baselineCount: Int,
    val constraintImpacts: Map<String, Int>,
    val hint: String
) {
    companion object {
        fun create(
            mostRestrictiveConstraint: String,
            baselineCount: Int,
            constraintImpacts: Map<String, Int>
        ): DiagnosticInfo {
            val hint = "No schedules found. The '$mostRestrictiveConstraint' rule is eliminating all candidates — try relaxing it."
            return DiagnosticInfo(
                mostRestrictiveConstraint = mostRestrictiveConstraint,
                baselineCount = baselineCount,
                constraintImpacts = constraintImpacts,
                hint = hint
            )
        }
    }
}
