package com.merakhata.app.domain.sync

import com.merakhata.app.data.model.CustomerEntity
import com.merakhata.app.data.model.TransactionEntity
import com.merakhata.app.data.model.TransactionType
import com.merakhata.app.data.repository.KhataRepository
import com.merakhata.app.domain.backup.BackupManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object CloudSyncEngine {

    private const val CLOUD_TUNNEL_URL = "https://fancy-worms-relate.loca.lt"

    /**
     * Pushes local database state (customers, transactions, reminders) to Cloud Database.
     */
    suspend fun pushLocalDataToCloud(repository: KhataRepository, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val customers = repository.getAllCustomersList()
            val transactions = repository.getAllTransactionsList()
            val reminders = repository.getActiveRemindersList()

            val jsonPayload = BackupManager.createBackupJson(customers, transactions, reminders)
            val bodyObj = JSONObject().apply {
                put("userId", userId)
                put("backupJson", jsonPayload)
            }

            executePost("$CLOUD_TUNNEL_URL/api/sync/push", bodyObj.toString())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Fetches cloud database state from Cloud for userId and restores it to local Room DB.
     */
    suspend fun fetchCloudDataAndRestore(repository: KhataRepository, userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bodyObj = JSONObject().apply {
                put("userId", userId)
            }
            val responseJson = executePost("$CLOUD_TUNNEL_URL/api/sync/pull", bodyObj.toString()) ?: return@withContext false

            if (!responseJson.optBoolean("success", false) || !responseJson.has("backupJson")) {
                return@withContext false
            }

            val cloudJsonStr = responseJson.optString("backupJson", null) ?: return@withContext false
            val validation = BackupManager.validateBackupJson(cloudJsonStr)
            if (validation.isValid) {
                val backupObj = JSONObject(cloudJsonStr)
                
                if (backupObj.has("customers")) {
                    val custArray = backupObj.getJSONArray("customers")
                    for (i in 0 until custArray.length()) {
                        val cObj = custArray.getJSONObject(i)
                        val name = cObj.getString("name")
                        val phone = if (cObj.has("phone") && !cObj.isNull("phone")) cObj.getString("phone") else null
                        val address = if (cObj.has("address") && !cObj.isNull("address")) cObj.getString("address") else null
                        val notes = if (cObj.has("notes") && !cObj.isNull("notes")) cObj.getString("notes") else null
                        
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
                        val description = if (tObj.has("description") && !tObj.isNull("description")) tObj.getString("description") else null
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

    private fun executePost(urlString: String, jsonInput: String): JSONObject? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("Bypass-Tunnel-Reminder", "true")
            conn.setRequestProperty("User-Agent", "MeraKhataAndroid/1.0")
            conn.doOutput = true
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(jsonInput)
                os.flush()
            }

            val statusCode = conn.responseCode
            val inputStream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val responseStr = reader.use { it.readText() }
            conn.disconnect()

            JSONObject(responseStr)
        } catch (e: Exception) {
            null
        }
    }
}
