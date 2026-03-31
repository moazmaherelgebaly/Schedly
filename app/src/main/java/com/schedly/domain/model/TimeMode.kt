package com.schedly.domain.model

sealed class TimeMode {
    object Normal : TimeMode()
    object Ramadan : TimeMode()
}
