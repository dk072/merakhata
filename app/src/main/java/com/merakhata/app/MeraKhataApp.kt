package com.merakhata.app

import android.app.Application
import com.merakhata.app.data.local.AppDatabase
import com.merakhata.app.data.preferences.UserPreferencesRepository
import com.merakhata.app.data.repository.KhataRepository

class MeraKhataApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: KhataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        val preferences = UserPreferencesRepository(this)
        repository = KhataRepository(
            customerDao = database.customerDao(),
            transactionDao = database.transactionDao(),
            reminderDao = database.reminderDao(),
            preferences = preferences
        )
    }
}
