package com.example.businessos.update

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
    val latestVersionCode: Int,
    val versionName: String,
    val releaseNotes: String,
    val apkDownloadUrl: String,
    val isMandatory: Boolean
)

object UpdateManager {

    /**
     * Checks a remote endpoint (e.g. GitHub Releases JSON or your web server) for updates.
     */
    suspend fun checkForUpdates(currentVersionCode: Int, checkUrl: String): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(checkUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val obj = JSONObject(jsonStr)
                val latestCode = obj.optInt("latestVersionCode", currentVersionCode)
                if (latestCode > currentVersionCode) {
                    return@withContext AppUpdateInfo(
                        latestVersionCode = latestCode,
                        versionName = obj.optString("versionName", "New Version"),
                        releaseNotes = obj.optString("releaseNotes", "Performance improvements & fixes"),
                        apkDownloadUrl = obj.optString("apkDownloadUrl", ""),
                        isMandatory = obj.optBoolean("isMandatory", false)
                    )
                }
            }
        } catch (_: Exception) {}
        null
    }

    /**
     * Triggers the Android system DownloadManager to fetch the new APK.
     */
    fun startApkDownload(context: Context, downloadUrl: String, versionName: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                setTitle("Business OS Update v$versionName")
                setDescription("Downloading the latest release of Business OS…")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "business_os_v$versionName.apk")
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
            dm?.enqueue(request)
        } catch (_: Exception) {
            // Fallback: Open download URL directly in browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }
}

@Composable
fun UpdatePromptDialog(
    updateInfo: AppUpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory) onDismiss()
        },
        title = {
            Text("Update Available (v${updateInfo.versionName})")
        },
        text = {
            Text(updateInfo.releaseNotes)
        },
        confirmButton = {
            Button(onClick = onDownload) {
                Text("Install Update")
            }
        },
        dismissButton = if (!updateInfo.isMandatory) {
            {
                TextButton(onClick = onDismiss) {
                    Text("Later")
                }
            }
        } else null
    )
}
