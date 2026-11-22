package com.example.businessos.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.ui.components.BackupRestoreHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupValidationTest {

    private val ctx: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun realDatabaseFilePassesTheCheck() {
        val db = Room.databaseBuilder(ctx, AppDatabase::class.java, "validate_ok.db").allowMainThreadQueries().build()
        db.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        val file = ctx.getDatabasePath("validate_ok.db")
        assertNull(BackupRestoreHelper.validate(file))
        db.close()
    }

    @Test
    fun garbageFileIsRejected() {
        val file = File(ctx.cacheDir, "garbage.db").apply { writeText("this is not a database at all") }
        assertNotNull(BackupRestoreHelper.validate(file))
    }

    @Test
    fun emptyFileIsRejected() {
        val file = File(ctx.cacheDir, "empty.db").apply { writeBytes(ByteArray(0)) }
        assertNotNull(BackupRestoreHelper.validate(file))
    }

    @Test
    fun otherSqliteDatabaseIsRejected() {
        val path = File(ctx.cacheDir, "other.db")
        val other = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(path, null)
        other.execSQL("CREATE TABLE notes(id INTEGER)")
        other.version = 1
        other.close()
        assertEquals("This is not a BusinessOS backup", BackupRestoreHelper.validate(path))
    }
}
