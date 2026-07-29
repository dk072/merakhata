package com.merakhata.app.domain.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class CloudAuthResult {
    data class Success(val userId: String, val email: String, val token: String, val ownerName: String?, val businessName: String?) : CloudAuthResult()
    data class Error(val message: String) : CloudAuthResult()
}

object CloudAuthService {

    // Public Active Cloud Tunnel Server Endpoint
    private const val CLOUD_TUNNEL_URL = "https://fancy-worms-relate.loca.lt"

    /**
     * Performs STRICT REAL-TIME Online Authentication (Sign In).
     * Returns error if user does not exist or if password is incorrect.
     */
    suspend fun signIn(email: String, password: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email.trim())
            put("password", password.trim())
        }

        val responseJson = executePost("$CLOUD_TUNNEL_URL/api/auth/login", payload.toString())
        if (responseJson == null) {
            return@withContext CloudAuthResult.Error("Unable to connect to Cloud Auth Server. Please check internet connection.")
        }

        if (responseJson.optBoolean("success", false)) {
            CloudAuthResult.Success(
                userId = responseJson.getString("userId"),
                email = responseJson.getString("email"),
                token = responseJson.optString("token", ""),
                ownerName = if (responseJson.has("ownerName") && !responseJson.isNull("ownerName")) responseJson.getString("ownerName") else null,
                businessName = if (responseJson.has("businessName") && !responseJson.isNull("businessName")) responseJson.getString("businessName") else null
            )
        } else {
            val msg = responseJson.optString("message", "Incorrect password or account not found!")
            CloudAuthResult.Error(msg)
        }
    }

    /**
     * Performs STRICT REAL-TIME Online Registration (Sign Up).
     * Returns error if user account already exists.
     */
    suspend fun signUp(email: String, password: String, ownerName: String, businessName: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("email", email.trim())
            put("password", password.trim())
            put("ownerName", ownerName.trim())
            put("businessName", businessName.trim())
        }

        val responseJson = executePost("$CLOUD_TUNNEL_URL/api/auth/register", payload.toString())
        if (responseJson == null) {
            return@withContext CloudAuthResult.Error("Unable to connect to Cloud Registration Server. Please check internet connection.")
        }

        if (responseJson.optBoolean("success", false)) {
            CloudAuthResult.Success(
                userId = responseJson.getString("userId"),
                email = responseJson.getString("email"),
                token = responseJson.optString("token", ""),
                ownerName = if (responseJson.has("ownerName") && !responseJson.isNull("ownerName")) responseJson.getString("ownerName") else ownerName,
                businessName = if (responseJson.has("businessName") && !responseJson.isNull("businessName")) responseJson.getString("businessName") else businessName
            )
        } else {
            val msg = responseJson.optString("message", "Registration Failed! Account may already exist.")
            CloudAuthResult.Error(msg)
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
            conn.connectTimeout = 6000
            conn.readTimeout = 6000

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
