package com.merakhata.app.domain.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed class AuthResult {
    data class Success(val idToken: String, val userId: String, val email: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

object FirebaseAuthService {

    // Firebase Web API Key for Real-Time Online Authentication
    private const val FIREBASE_API_KEY = "AIzaSyD-MeraKhataRealtimeAuthKey2026"
    private const val SIGN_IN_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
    private const val SIGN_UP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:signUp?key="

    /**
     * Performs REAL-TIME Online Firebase Authentication (Sign In).
     */
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
                put("returnSecureToken", true)
            }

            val responseJson = executePost(SIGN_IN_URL + FIREBASE_API_KEY, payload.toString())
            if (responseJson.has("idToken")) {
                val idToken = responseJson.getString("idToken")
                val localId = responseJson.getString("localId")
                val responseEmail = responseJson.optString("email", email)
                AuthResult.Success(idToken = idToken, userId = localId, email = responseEmail)
            } else if (responseJson.has("error")) {
                val errorObj = responseJson.getJSONObject("error")
                val rawMsg = errorObj.optString("message", "Authentication Failed")
                val readableMsg = parseFirebaseErrorMessage(rawMsg)
                AuthResult.Error(readableMsg)
            } else {
                AuthResult.Error("Unknown response from Auth server")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error("Unable to connect to authentication server. Please try again.")
        }
    }

    /**
     * Performs REAL-TIME Online Firebase Account Registration (Sign Up).
     */
    suspend fun signUp(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("email", email.trim())
                put("password", password.trim())
                put("returnSecureToken", true)
            }

            val responseJson = executePost(SIGN_UP_URL + FIREBASE_API_KEY, payload.toString())
            if (responseJson.has("idToken")) {
                val idToken = responseJson.getString("idToken")
                val localId = responseJson.getString("localId")
                val responseEmail = responseJson.optString("email", email)
                AuthResult.Success(idToken = idToken, userId = localId, email = responseEmail)
            } else if (responseJson.has("error")) {
                val errorObj = responseJson.getJSONObject("error")
                val rawMsg = errorObj.optString("message", "Registration Failed")
                val readableMsg = parseFirebaseErrorMessage(rawMsg)
                AuthResult.Error(readableMsg)
            } else {
                AuthResult.Error("Unknown response from Auth server")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            AuthResult.Error("Unable to connect to authentication server. Please try again.")
        }
    }

    private fun executePost(urlString: String, jsonInput: String): JSONObject {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.connectTimeout = 8000
        conn.readTimeout = 8000

        OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
            os.write(jsonInput)
            os.flush()
        }

        val statusCode = conn.responseCode
        val inputStream = if (statusCode in 200..299) conn.inputStream else conn.errorStream
        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        val responseStr = reader.use { it.readText() }
        conn.disconnect()

        return JSONObject(responseStr)
    }

    private fun parseFirebaseErrorMessage(rawMsg: String): String {
        return when {
            rawMsg.contains("INVALID_PASSWORD") -> "Incorrect Password! Please check your password and try again."
            rawMsg.contains("EMAIL_NOT_FOUND") -> "No account found with this email address. Please register first."
            rawMsg.contains("EMAIL_EXISTS") -> "An account with this email address already exists. Please log in."
            rawMsg.contains("WEAK_PASSWORD") -> "Password is too weak. Password should be at least 6 characters."
            rawMsg.contains("INVALID_EMAIL") -> "Invalid email address format."
            rawMsg.contains("USER_DISABLED") -> "This user account has been disabled."
            rawMsg.contains("TOO_MANY_ATTEMPTS_TRY_LATER") -> "Too many failed login attempts. Please try again later."
            else -> rawMsg.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}
