package com.example.businessos.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object BackupRestoreHelper {

    private const val DB_NAME = "business_os_v4.db"

    /**
     * Creates a standalone copy of the local Room database and opens the system Share/Drive intent
     * so the owner can save it to Google Drive, WhatsApp, or File Manager with 1 tap.
     */
    fun exportDatabaseBackup(context: Context) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            if (!dbFile.exists()) {
                Toast.makeText(context, "Hati ya daftari bado haijatengenezwa", Toast.LENGTH_SHORT).show()
                return
            }

            val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
            val dateStr = sdf.format(Date())
            val backupFileName = "BusinessOS_Backup_${dateStr}.db"

            val backupDir = File(context.cacheDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()
            val backupFile = File(backupDir, backupFileName)

            // Copy db file
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Share via FileProvider or system send intent
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = try {
                FileProvider.getUriForFile(context, authority, backupFile)
            } catch (e: Exception) {
                Uri.fromFile(backupFile)
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BusinessOS Backup - $dateStr")
                putExtra(Intent.EXTRA_TEXT, "Hifadhi ya Daftari la BusinessOS iliyochukuliwa tarehe $dateStr. Unaweza kuihifadhi kwenye Google Drive yako.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(shareIntent, "Hifadhi Nakala (Save to Drive / WhatsApp / Files)")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)

        } catch (e: Exception) {
            Toast.makeText(context, "Hitilafu wakati wa kutoa nakala: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Restores database from a selected URI
     */
    fun restoreDatabaseFromUri(context: Context, sourceUri: Uri): Boolean {
        return try {
            val targetDb = context.getDatabasePath(DB_NAME)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetDb).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
