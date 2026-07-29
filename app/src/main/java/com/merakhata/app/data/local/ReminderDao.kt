package com.merakhata.app.data.local

import androidx.room.*
import com.merakhata.app.data.model.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY reminderDateTime ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 ORDER BY reminderDateTime ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 ORDER BY reminderDateTime ASC")
    suspend fun getActiveRemindersList(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE customerId = :customerId ORDER BY reminderDateTime ASC")
    fun getRemindersForCustomer(customerId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
