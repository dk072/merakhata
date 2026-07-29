package com.merakhata.app.domain.sync

import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.backup.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FirebaseRealtimeSyncEngine {

    private const val FIREBASE_DATABASE_URL = "https://merakhata-cloud-default-rtdb.firebaseio.com/users/"

    /**
     * Pushes local database state (customers, transactions, reminders) to Firebase Realtime Database in REAL-TIME.
     */
    suspend fun pushLocalDataToCloud(repository: KhataRepository, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val customers = repository.getAllCustomersList()
            val transactions = repository.getAllTransactionsList()
            val reminders = repository.getActiveRemindersList()

            val jsonPayload = BackupManager.createBackupJson(customers, transactions, reminders)
            val cloudUrl = "$FIREBASE_DATABASE_URL$userId.json"

            executePut(cloudUrl, jsonPayload)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Fetches cloud database state from Firebase for userId and restores it to local Room DB.
     */
    suspend fun fetchCloudDataAndRestore(repository: KhataRepository, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cloudUrl = "$FIREBASE_DATABASE_URL$userId.json"
            val cloudJsonStr = executeGet(cloudUrl)

            if (cloudJsonStr.isBlank() || cloudJsonStr == "null") {
                return@withContext false
            }

            val validation = BackupManager.validateBackupJson(cloudJsonStr)
            if (validation.isValid) {
                val backupObj = JSONObject(cloudJsonStr)
                
                if (backupObj.has("customers")) {
                    val custArray = backupObj.getJSONArray("customers")
                    for (i in 0 until custArray.length()) {
                        val cObj = custArray.getJSONObject(i)
                        val name = cObj.getString("name")
                        val phone = cObj.optString("phone", null)
                        val address = cObj.optString("address", null)
                        val notes = cObj.optString("notes", null)
                        
                        repository.insertCustomer(
                            CustomerEntity(
                                id = cObj.optLong("id", 0L),
                                name = name,
                                phone = phone,
                                address = address,
                                notes = notes,
                                createdAt = cObj.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                }

                if (backupObj.has("transactions")) {
                    val txArray = backupObj.getJSONArray("transactions")
                    for (i in 0 until txArray.length()) {
                        val tObj = txArray.getJSONObject(i)
                        val customerId = tObj.getLong("customerId")
                        val typeStr = tObj.getString("type")
                        val amountMinor = tObj.getLong("amountMinor")
                        val description = tObj.optString("description", null)
                        val date = tObj.getLong("transactionDate")

                        repository.insertTransaction(
                            TransactionEntity(
                                id = tObj.optLong("id", 0L),
                                customerId = customerId,
                                type = TransactionType.valueOf(typeStr),
                                amountMinor = amountMinor,
                                description = description,
                                transactionDate = date,
                                createdAt = date
                            )
                        )
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun executePut(urlString: String, jsonInput: String) {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.doOutput = true
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
            os.write(jsonInput)
            os.flush()
        }

        val code = conn.responseCode
        conn.disconnect()
    }

    private fun executeGet(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        val code = conn.responseCode
        if (code !in 200..299) {
            conn.disconnect()
            return ""
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val content = reader.use { it.readText() }
        conn.disconnect()
        return content
    }
}
