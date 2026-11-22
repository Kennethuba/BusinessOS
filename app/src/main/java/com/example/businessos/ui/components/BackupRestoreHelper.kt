package com.example.businessos.ui.components

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Process
import androidx.core.content.FileProvider
import com.example.businessos.data.local.AppDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup and restore of the whole database file.
 *
 * Backup first flushes the write-ahead log into the main file, so the copy is complete.
 * Restore never trusts the chosen file: it is checked first, the current data is kept as
 * a rollback copy, and if anything fails the current data is put back.
 */
object BackupRestoreHelper {

    sealed class RestoreResult {
        object Success : RestoreResult()
        data class Failure(val reason: String) : RestoreResult()
    }

    private val REQUIRED_TABLES = listOf("businesses", "users", "accounts", "journal_entries", "journal_lines")

    /** Writes a complete copy of the database and opens the share sheet. Returns an error message or null. */
    fun exportDatabaseBackup(context: Context, database: AppDatabase): String? {
        return try {
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
            val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
            if (!dbFile.exists()) return "There is no data to back up yet"

            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
            val dir = File(context.cacheDir, "backups").apply { mkdirs() }
            dir.listFiles()?.forEach { it.delete() }
            val out = File(dir, "BusinessOS_Backup_$stamp.db")
            dbFile.copyTo(out, overwrite = true)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", out)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "BusinessOS backup $stamp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            null
        } catch (e: Exception) {
            e.localizedMessage ?: "Backup failed"
        }
    }

    /** Checks that [file] is a healthy BusinessOS database that this app version can open. */
    fun validate(file: File): String? {
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
            val integrity = db.rawQuery("PRAGMA integrity_check", null).use { if (it.moveToFirst()) it.getString(0) else "" }
            if (integrity != "ok") return "The file is damaged"
            if (db.version > AppDatabase.SCHEMA_VERSION) return "This backup is from a newer version of the app"
            if (db.version < 1) return "This is not a BusinessOS backup"
            val tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { c ->
                buildSet { while (c.moveToNext()) add(c.getString(0)) }
            }
            val missing = REQUIRED_TABLES.filter { it !in tables }
            if (missing.isNotEmpty()) "This is not a BusinessOS backup" else null
        } catch (e: Exception) {
            "This is not a BusinessOS backup"
        } finally {
            db?.close()
        }
    }

    /** Replaces the current data with the backup at [source]. The caller must then restart the app. */
    fun restoreDatabaseFromUri(context: Context, source: Uri): RestoreResult {
        val live = context.getDatabasePath(AppDatabase.DB_NAME)
        val dir = live.parentFile ?: return RestoreResult.Failure("Cannot find the data folder")
        val incoming = File(dir, "incoming_restore.db")
        val rollbackDir = File(context.cacheDir, "rollback").apply { deleteRecursively(); mkdirs() }
        val parts = listOf("", "-wal", "-shm")
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                incoming.outputStream().use { input.copyTo(it) }
            } ?: return RestoreResult.Failure("Cannot read the chosen file")

            validate(incoming)?.let { incoming.delete(); return RestoreResult.Failure(it) }

            AppDatabase.closeInstance()
            // Keep what we have now, so a failure puts everything back.
            parts.forEach { suffix ->
                val f = File(dir, AppDatabase.DB_NAME + suffix)
                if (f.exists()) f.copyTo(File(rollbackDir, f.name), overwrite = true)
            }
            try {
                parts.drop(1).forEach { File(dir, AppDatabase.DB_NAME + it).delete() }
                incoming.copyTo(live, overwrite = true)
                if (validate(live) != null) throw IllegalStateException("Restored file failed the check")
            } catch (e: Exception) {
                parts.forEach { suffix ->
                    val saved = File(rollbackDir, AppDatabase.DB_NAME + suffix)
                    val target = File(dir, AppDatabase.DB_NAME + suffix)
                    if (saved.exists()) saved.copyTo(target, overwrite = true) else target.delete()
                }
                return RestoreResult.Failure("Restore failed. Your current data was kept.")
            } finally {
                incoming.delete()
            }
            return RestoreResult.Success
        } catch (e: Exception) {
            incoming.delete()
            return RestoreResult.Failure(e.localizedMessage ?: "Restore failed")
        }
    }

    /** Starts the app fresh and ends this process, so nothing keeps using the old database. */
    fun restartApp(context: Context) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        if (intent != null) context.startActivity(intent)
        Process.killProcess(Process.myPid())
    }
}
