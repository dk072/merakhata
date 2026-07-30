package com.merakhata.app.domain.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

sealed class CloudAuthResult {
    data class Success(
        val userId: String,
        val email: String,
        val token: String,
        val ownerName: String?,
        val businessName: String?
    ) : CloudAuthResult()

    data class Error(val message: String) : CloudAuthResult()
}

object CloudAuthService {

    // Backend Endpoints (Render Production + Local/Tunnel Development)
    private const val RENDER_BASE_URL = "https://merakhata-backend-nsl2.onrender.com"
    private const val SERVER_BASE_URL = "http://10.0.2.2:8080"
    private const val TUNNEL_BASE_URL = "https://fancy-worms-relate.loca.lt"

    // Persistent/In-memory fallback user repository strictly using salted SHA-256 password hashes
    private val registeredUserVault = ConcurrentHashMap<String, UserCredentialRecord>()

    data class UserCredentialRecord(
        val userId: String,
        val email: String,
        val passwordHash: String,
        val ownerName: String,
        val businessName: String
    )

    fun hashPassword(password: String): String {
        val bytes = (password + "MeraKhataSalt2026").toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    /**
     * Performs Secure Server & Vault Authentication (Sign In).
     * Strictly verifies registered existence and password hashes.
     */
    suspend fun signIn(email: String, password: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()

        if (cleanEmail.isEmpty() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext CloudAuthResult.Error("Enter a valid email address.")
        }
        if (cleanPassword.isEmpty()) {
            return@withContext CloudAuthResult.Error("Enter your password.")
        }

        val inputPwdHash = hashPassword(cleanPassword)

        // 1. Attempt Authentication via Render & Server API Endpoints
        val endpoints = listOf(
            "$RENDER_BASE_URL/api/auth/login",
            "$SERVER_BASE_URL/api/auth/login",
            "$TUNNEL_BASE_URL/api/auth/login"
        )
        val payload = JSONObject().apply {
            put("email", cleanEmail)
            put("password", cleanPassword)
        }

        for (endpoint in endpoints) {
            val response = executePost(endpoint, payload.toString())
            if (response != null) {
                val success = response.optBoolean("success", false)
                val message = response.optString("message", "Authentication failed.")
                if (success) {
                    val userId = response.getString("userId")
                    val token = response.optString("token", "token_$userId")
                    val ownerName = response.optString("ownerName", "")
                    val businessName = response.optString("businessName", "")

                    // Save to vault for session consistency
                    registeredUserVault[cleanEmail] = UserCredentialRecord(userId, cleanEmail, inputPwdHash, ownerName, businessName)

                    return@withContext CloudAuthResult.Success(
                        userId = userId,
                        email = cleanEmail,
                        token = token,
                        ownerName = ownerName,
                        businessName = businessName
                    )
                } else if (response.has("message")) {
                    return@withContext CloudAuthResult.Error(message)
                }
            }
        }

        // 2. Local Vault Credential Verification (Offline/Fallback Mode)
        val record = registeredUserVault[cleanEmail]
        if (record == null) {
            return@withContext CloudAuthResult.Error("No account found with this email ($cleanEmail). Please register first.")
        }

        if (record.passwordHash != inputPwdHash) {
            return@withContext CloudAuthResult.Error("Incorrect Password! Please check your password and try again.")
        }

        return@withContext CloudAuthResult.Success(
            userId = record.userId,
            email = record.email,
            token = "token_${record.userId}",
            ownerName = record.ownerName,
            businessName = record.businessName
        )
    }

    /**
     * Performs Secure Account Registration (Sign Up).
     * Server-side hashing & creation. Prevents duplicate email registrations.
     */
    suspend fun signUp(email: String, password: String, ownerName: String, businessName: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanOwner = ownerName.trim()
        val cleanBusiness = businessName.trim()

        if (cleanEmail.isEmpty() || !cleanEmail.contains("@") || !cleanEmail.contains(".")) {
            return@withContext CloudAuthResult.Error("Enter a valid email address.")
        }
        if (cleanPassword.length < 4) {
            return@withContext CloudAuthResult.Error("Password must be at least 4 characters.")
        }

        val pwdHash = hashPassword(cleanPassword)

        // 1. Attempt Registration via Render & Server API Endpoints
        val endpoints = listOf(
            "$RENDER_BASE_URL/api/auth/register",
            "$SERVER_BASE_URL/api/auth/register",
            "$TUNNEL_BASE_URL/api/auth/register"
        )
        val payload = JSONObject().apply {
            put("email", cleanEmail)
            put("password", cleanPassword)
            put("ownerName", cleanOwner)
            put("businessName", cleanBusiness)
        }

        for (endpoint in endpoints) {
            val response = executePost(endpoint, payload.toString())
            if (response != null) {
                val success = response.optBoolean("success", false)
                val message = response.optString("message", "Registration failed.")
                if (success) {
                    val userId = response.getString("userId")
                    val token = response.optString("token", "token_$userId")

                    registeredUserVault[cleanEmail] = UserCredentialRecord(userId, cleanEmail, pwdHash, cleanOwner, cleanBusiness)

                    return@withContext CloudAuthResult.Success(
                        userId = userId,
                        email = cleanEmail,
                        token = token,
                        ownerName = cleanOwner,
                        businessName = cleanBusiness
                    )
                } else if (response.has("message")) {
                    return@withContext CloudAuthResult.Error(message)
                }
            }
        }

        // 2. Local Vault Registration (Offline/Fallback Mode)
        if (registeredUserVault.containsKey(cleanEmail)) {
            return@withContext CloudAuthResult.Error("An account with this email address already exists. Please log in.")
        }

        val generatedUserId = "usr_" + cleanEmail.hashCode().toString().replace("-", "")
        val newRecord = UserCredentialRecord(
            userId = generatedUserId,
            email = cleanEmail,
            passwordHash = pwdHash,
            ownerName = cleanOwner,
            businessName = cleanBusiness
        )
        registeredUserVault[cleanEmail] = newRecord

        return@withContext CloudAuthResult.Success(
            userId = generatedUserId,
            email = cleanEmail,
            token = "token_$generatedUserId",
            ownerName = cleanOwner,
            businessName = cleanBusiness
        )
    }

    private fun executePost(urlString: String, jsonInput: String): JSONObject? {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 3000
            conn.readTimeout = 3000

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(jsonInput)
                os.flush()
            }

            val statusCode = conn.responseCode
            val inputStream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
            if (inputStream == null) return null

            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val responseStr = reader.use { it.readText() }
            conn.disconnect()

            if (responseStr.isBlank()) null else JSONObject(responseStr)
        } catch (e: Exception) {
            null
        }
    }
}
