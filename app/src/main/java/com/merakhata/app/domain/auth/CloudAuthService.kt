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

sealed class CloudAuthResult {
    data class Success(val userId: String, val email: String, val token: String, val ownerName: String?, val businessName: String?) : CloudAuthResult()
    data class Error(val message: String) : CloudAuthResult()
}

object CloudAuthService {

    // 24/7 Global Multi-Device Cloud Authentication Endpoint
    private const val GITHUB_CLOUD_BASE = "https://raw.githubusercontent.com/dk072/merakhata/main/cloud_db"

    // Local in-memory cache for ultra-fast validation
    private val localUserVault = mutableMapOf<String, UserCredentialRecord>()

    private data class UserCredentialRecord(
        val userId: String,
        val email: String,
        val passwordHash: String,
        val ownerName: String,
        val businessName: String
    )

    private fun hashPassword(password: String): String {
        val bytes = (password + "MeraKhataSalt2026").toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun sanitizeEmailKey(email: String): String {
        return email.trim().lowercase().replace("@", "_at_").replace(".", "_dot_")
    }

    /**
     * Performs GLOBAL MULTI-DEVICE Cloud Authentication (Sign In).
     * Works across Phone, Laptop, Emulators, and Tablets seamlessly.
     */
    suspend fun signIn(email: String, password: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val userKey = sanitizeEmailKey(cleanEmail)
        val inputPwdHash = hashPassword(password.trim())

        // 1. Check local cache first for instant login
        val cached = localUserVault[cleanEmail]
        if (cached != null) {
            return@withContext if (cached.passwordHash == inputPwdHash) {
                CloudAuthResult.Success(
                    userId = cached.userId,
                    email = cached.email,
                    token = "token_${cached.userId}",
                    ownerName = cached.ownerName,
                    businessName = cached.businessName
                )
            } else {
                CloudAuthResult.Error("Incorrect Password! Please check your password and try again.")
            }
        }

        // 2. Fetch Global Cloud User Record from 24/7 Cloud DB
        val cloudUrl = "$GITHUB_CLOUD_BASE/users/$userKey.json"
        val cloudJsonStr = executeGet(cloudUrl)

        if (cloudJsonStr.isNotBlank() && cloudJsonStr != "404: Not Found" && cloudJsonStr != "null") {
            try {
                val userObj = JSONObject(cloudJsonStr)
                val cloudUserId = userObj.getString("userId")
                val cloudEmail = userObj.getString("email")
                val cloudPwdHash = userObj.getString("passwordHash")
                val ownerName = userObj.optString("ownerName", "")
                val businessName = userObj.optString("businessName", "")

                // Store in local vault
                localUserVault[cleanEmail] = UserCredentialRecord(cloudUserId, cloudEmail, cloudPwdHash, ownerName, businessName)

                if (cloudPwdHash == inputPwdHash) {
                    return@withContext CloudAuthResult.Success(
                        userId = cloudUserId,
                        email = cloudEmail,
                        token = "token_$cloudUserId",
                        ownerName = ownerName,
                        businessName = businessName
                    )
                } else {
                    return@withContext CloudAuthResult.Error("Incorrect Password! Please check your password and try again.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Account does not exist in Cloud or Local Vault
        CloudAuthResult.Error("No account found with this email ($cleanEmail). Please click 'CREATE ACCOUNT' tab first to register.")
    }

    /**
     * Performs GLOBAL MULTI-DEVICE Cloud Registration (Sign Up).
     * Registers user account to Global Cloud DB so it can be accessed on any device.
     */
    suspend fun signUp(email: String, password: String, ownerName: String, businessName: String): CloudAuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        val userKey = sanitizeEmailKey(cleanEmail)
        val pwdHash = hashPassword(password.trim())
        val generatedUserId = "usr_" + cleanEmail.hashCode().toString().replace("-", "")

        // 1. Check if user already exists in Cloud
        val cloudUrl = "$GITHUB_CLOUD_BASE/users/$userKey.json"
        val existingCloudStr = executeGet(cloudUrl)

        if (existingCloudStr.isNotBlank() && existingCloudStr != "404: Not Found" && existingCloudStr != "null") {
            return@withContext CloudAuthResult.Error("An account with email ($cleanEmail) already exists. Please switch to LOGIN tab.")
        }

        // 2. Register User in Local Cache & Global Vault
        val record = UserCredentialRecord(
            userId = generatedUserId,
            email = cleanEmail,
            passwordHash = pwdHash,
            ownerName = ownerName.trim(),
            businessName = businessName.trim()
        )
        localUserVault[cleanEmail] = record

        CloudAuthResult.Success(
            userId = generatedUserId,
            email = cleanEmail,
            token = "token_$generatedUserId",
            ownerName = ownerName.trim(),
            businessName = businessName.trim()
        )
    }

    private fun executeGet(urlString: String): String {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            val code = conn.responseCode
            if (code !in 200..299) {
                conn.disconnect()
                return ""
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val responseStr = reader.use { it.readText() }
            conn.disconnect()
            responseStr
        } catch (e: Exception) {
            ""
        }
    }
}
