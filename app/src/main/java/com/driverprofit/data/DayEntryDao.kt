package com.driverprofit.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface DayEntryDao {
    @Query("SELECT * FROM day_entries ORDER BY epochDay DESC, id DESC")
    fun observeAll(): Flow<List<DayEntry>>

    @Query("SELECT * FROM day_entries WHERE id = :id")
    suspend fun get(id: Long): DayEntry?

    @Upsert
    suspend fun upsert(entry: DayEntry)

    @Query("DELETE FROM day_entries WHERE id = :id")
    suspend fun delete(id: Long)
}
