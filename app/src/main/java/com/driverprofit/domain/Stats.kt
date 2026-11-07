package com.driverprofit.domain

import com.driverprofit.data.DayEntry

data class Stats(
    val gross: Double,
    val expenses: Double,
    val distance: Double,
    val hours: Double,
) {
    val net: Double get() = gross - expenses
    val grossPerDistance: Double get() = if (distance > 0) gross / distance else 0.0
    val grossPerHour: Double get() = if (hours > 0) gross / hours else 0.0
}

fun List<DayEntry>.toStats(): Stats = Stats(
    gross = sumOf { it.gross },
    expenses = sumOf { it.expenses },
    distance = sumOf { it.distance },
    hours = sumOf { it.hours },
)
