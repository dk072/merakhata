package com.merakhata.app.domain.backup

import com.merakhata.app.data.local.AppDatabase
import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.ReminderEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupValidationSummary(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val backupDateStr: String = "",
    val customerCount: Int = 0,
    val transactionCount: Int = 0,
    val reminderCount: Int = 0,
    val rawJson: String = ""
)

object BackupManager {

    fun createBackupJson(
        customers: List<CustomerEntity>,
        transactions: List<TransactionEntity>,
        reminders: List<ReminderEntity>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("backupTimestamp", System.currentTimeMillis())

        val custArray = JSONArray()
        for (c in customers) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone ?: "")
            obj.put("address", c.address ?: "")
            obj.put("notes", c.notes ?: "")
            obj.put("photoUri", c.photoUri ?: "")
            obj.put("createdAt", c.createdAt)
            obj.put("updatedAt", c.updatedAt)
            custArray.put(obj)
        }
        root.put("customers", custArray)

        val txArray = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("customerId", t.customerId)
            obj.put("amountMinor", t.amountMinor)
            obj.put("type", t.type.name)
            obj.put("description", t.description ?: "")
            obj.put("attachmentUri", t.attachmentUri ?: "")
            obj.put("transactionDate", t.transactionDate)
            obj.put("createdAt", t.createdAt)
            obj.put("updatedAt", t.updatedAt)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val remArray = JSONArray()
        for (r in reminders) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("customerId", r.customerId)
            obj.put("amountMinor", r.amountMinor)
            obj.put("reminderDateTime", r.reminderDateTime)
            obj.put("notes", r.notes ?: "")
            obj.put("enabled", r.enabled)
            obj.put("createdAt", r.createdAt)
            remArray.put(obj)
        }
        root.put("reminders", remArray)

        return root.toString(2)
    }

    fun validateBackupJson(jsonString: String): BackupValidationSummary {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("version") || !root.has("customers") || !root.has("transactions")) {
                return BackupValidationSummary(isValid = false, errorMessage = "Invalid backup file structure.")
            }

            val timestamp = root.optLong("backupTimestamp", System.currentTimeMillis())
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(timestamp))

            val custArray = root.getJSONArray("customers")
            val txArray = root.getJSONArray("transactions")
            val remArray = root.optJSONArray("reminders")

            BackupValidationSummary(
                isValid = true,
                backupDateStr = dateStr,
                customerCount = custArray.length(),
                transactionCount = txArray.length(),
                reminderCount = remArray?.length() ?: 0,
                rawJson = jsonString
            )
        } catch (e: Exception) {
            BackupValidationSummary(isValid = false, errorMessage = "Corrupted file: ${e.message}")
        }
    }

    suspend fun restoreDatabaseFromJson(database: AppDatabase, jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val custArray = root.getJSONArray("customers")
            val txArray = root.getJSONArray("transactions")
            val remArray = root.optJSONArray("reminders")

            val customers = mutableListOf<CustomerEntity>()
            for (i in 0 until custArray.length()) {
                val obj = custArray.getJSONObject(i)
                customers.add(
                    CustomerEntity(
                        id = obj.getLong("id"),
                        name = obj.getString("name"),
                        phone = obj.optString("phone").takeIf { it.isNotBlank() },
                        address = obj.optString("address").takeIf { it.isNotBlank() },
                        notes = obj.optString("notes").takeIf { it.isNotBlank() },
                        photoUri = obj.optString("photoUri").takeIf { it.isNotBlank() },
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val transactions = mutableListOf<TransactionEntity>()
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                transactions.add(
                    TransactionEntity(
                        id = obj.getLong("id"),
                        customerId = obj.getLong("customerId"),
                        amountMinor = obj.getLong("amountMinor"),
                        type = TransactionType.valueOf(obj.getString("type")),
                        description = obj.optString("description").takeIf { it.isNotBlank() },
                        attachmentUri = obj.optString("attachmentUri").takeIf { it.isNotBlank() },
                        transactionDate = obj.optLong("transactionDate", System.currentTimeMillis()),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }

            val reminders = mutableListOf<ReminderEntity>()
            if (remArray != null) {
                for (i in 0 until remArray.length()) {
                    val obj = remArray.getJSONObject(i)
                    reminders.add(
                        ReminderEntity(
                            id = obj.getLong("id"),
                            customerId = obj.getLong("customerId"),
                            amountMinor = obj.getLong("amountMinor"),
                            reminderDateTime = obj.getLong("reminderDateTime"),
                            notes = obj.optString("notes").takeIf { it.isNotBlank() },
                            enabled = obj.optBoolean("enabled", true),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Restore inside DB transaction
            database.runInTransaction {
                kotlinx.coroutines.runBlocking {
                    database.transactionDao().deleteAllTransactions()
                    database.reminderDao().deleteAllReminders()
                    database.customerDao().deleteAllCustomers()

                    for (c in customers) {
                        database.customerDao().insertCustomer(c)
                    }
                    for (t in transactions) {
                        database.transactionDao().insertTransaction(t)
                    }
                    for (r in reminders) {
                        database.reminderDao().insertReminder(r)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
