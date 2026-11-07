package com.driverprofit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "day_entries")
data class DayEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val platform: String,
    val gross: Double,
    val distance: Double,
    val hours: Double,
    val fuelExpense: Double,
    val otherExpense: Double,
) {
    val expenses: Double get() = fuelExpense + otherExpense
    val net: Double get() = gross - expenses
}
