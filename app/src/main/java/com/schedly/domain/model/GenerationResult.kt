package com.schedly.domain.model

/**
 * Output DTO from the generation engine.
 * Contains all valid schedules generated and diagnostic info if zero results.
 */
data class GenerationResult(
    val schedules: List<Schedule>,
    val hasPartialSchedules: Boolean = false,
    val diagnostic: DiagnosticInfo? = null
) {
    companion object {
        fun success(schedules: List<Schedule>): GenerationResult {
            return GenerationResult(
                schedules = schedules,
                hasPartialSchedules = schedules.any { it.isPartial },
                diagnostic = null
            )
        }
        
        fun zeroResults(diagnostic: DiagnosticInfo): GenerationResult {
            return GenerationResult(
                schedules = emptyList(),
                hasPartialSchedules = false,
                diagnostic = diagnostic
            )
        }
    }
}
