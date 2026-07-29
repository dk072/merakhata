package com.merakhata.app.domain.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val isMandatory: Boolean = false
)

sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object Checking : UpdateStatus()
    data class Available(val updateInfo: UpdateInfo) : UpdateStatus()
    object NoUpdate : UpdateStatus()
    data class Downloading(val progressPercent: Int) : UpdateStatus()
    data class ReadyToInstall(val apkFile: File) : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

object UpdateManager {

    /**
     * Gets the current installed version code of the app.
     */
    fun getCurrentVersionCode(context: Context): Int {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                pInfo.versionCode
            }
        } catch (e: Exception) {
            1
        }
    }

    /**
     * Gets the current installed version name of the app.
     */
    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    /**
     * Checks for available updates by parsing update JSON from [updateUrl] or [jsonContent].
     */
    suspend fun checkForUpdates(
        context: Context,
        updateUrl: String,
        jsonContent: String? = null
    ): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val rawJson = jsonContent ?: fetchJsonFromUrl(updateUrl) ?: return@withContext null
            val root = JSONObject(rawJson)

            val remoteVersionCode = root.optInt("versionCode", 0)
            val remoteVersionName = root.optString("versionName", "1.0.0")
            val apkUrl = root.optString("apkUrl", "")
            val releaseNotes = root.optString("releaseNotes", "New performance enhancements and bug fixes.")
            val isMandatory = root.optBoolean("isMandatory", false)

            val currentCode = getCurrentVersionCode(context)

            if (remoteVersionCode > currentCode && apkUrl.isNotBlank()) {
                UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName,
                    apkUrl = apkUrl,
                    releaseNotes = releaseNotes,
                    isMandatory = isMandatory
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun fetchJsonFromUrl(urlString: String): String? {
        return try {
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Bypass-Tunnel-Reminder", "true")
            connection.setRequestProperty("User-Agent", "MeraKhataApp/1.0")
            connection.connect()

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Downloads update APK file to local cache with progress callback.
     */
    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Int) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val url = URL(apkUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.requestMethod = "GET"
            connection.setRequestProperty("Bypass-Tunnel-Reminder", "true")
            connection.setRequestProperty("User-Agent", "MeraKhataApp/1.0")
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                return@withContext null
            }

            val fileLength = connection.contentLength
            val outputFile = File(context.cacheDir, "MeraKhata_Update.apk")

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val data = ByteArray(4096)
                    var total: Long = 0
                    var count: Int

                    while (input.read(data).also { count = it } != -1) {
                        total += count.toLong()
                        if (fileLength > 0) {
                            val progress = ((total * 100) / fileLength).toInt()
                            withContext(Dispatchers.Main) {
                                onProgress(progress)
                            }
                        }
                        output.write(data, 0, count)
                    }
                    output.flush()
                }
            }

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Triggers the Android system Package Installer to install the updated APK.
     */
    fun installApk(context: Context, apkFile: File) {
        try {
            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "com.merakhata.app.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
