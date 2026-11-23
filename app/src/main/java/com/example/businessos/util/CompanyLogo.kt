package com.example.businessos.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.mutableIntStateOf
import com.example.businessos.ui.theme.AccentTheme
import java.io.File

/** What kind of business this company is. Used only to suggest categories and units. */
enum class BusinessKind { GENERAL, GARAGE, STALL }

/** A company logo kept as a small picture file on the phone. */
object CompanyLogo {
    /** Changes whenever a logo is saved or removed, so screens showing it redraw. */
    val version = mutableIntStateOf(0)

    private fun file(context: Context, businessId: String) =
        File(File(context.filesDir, "logos").apply { mkdirs() }, "$businessId.png")

    fun load(context: Context, businessId: String): Bitmap? {
        val f = file(context, businessId)
        return if (f.exists()) BitmapFactory.decodeFile(f.path) else null
    }

    /** Reads the picture, shrinks it so it stays small, and saves it. Returns false if it could not be read. */
    fun save(context: Context, businessId: String, source: Uri): Boolean {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / sample > 1024 || bounds.outHeight / sample > 1024) sample *= 2
            val bmp = context.contentResolver.openInputStream(source)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
            } ?: return false
            val scale = 512f / maxOf(bmp.width, bmp.height)
            val out = if (scale < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true) else bmp
            file(context, businessId).outputStream().use { out.compress(Bitmap.CompressFormat.PNG, 100, it) }
            version.intValue++
            true
        } catch (e: Exception) {
            false
        }
    }

    fun remove(context: Context, businessId: String) {
        file(context, businessId).delete()
        version.intValue++
    }

    /** The accent colour closest to the logo's main colour, ignoring white, black and grey. */
    fun suggestAccent(bitmap: Bitmap): AccentTheme {
        val small = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        var r = 0L; var g = 0L; var b = 0L; var n = 0L
        for (x in 0 until small.width) for (y in 0 until small.height) {
            val p = small.getPixel(x, y)
            if (android.graphics.Color.alpha(p) < 200) continue
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(p, hsv)
            if (hsv[1] < 0.25f || hsv[2] < 0.2f || hsv[2] > 0.95f) continue
            r += android.graphics.Color.red(p); g += android.graphics.Color.green(p); b += android.graphics.Color.blue(p); n++
        }
        if (n == 0L) return AccentTheme.SLATE
        val ar = r / n; val ag = g / n; val ab = b / n
        return AccentTheme.values().minByOrNull { t ->
            val c = t.main.value.toLong()
            val tr = ((t.main.red * 255).toInt() - ar); val tg = ((t.main.green * 255).toInt() - ag); val tb = ((t.main.blue * 255).toInt() - ab)
            tr * tr + tg * tg + tb * tb + (c * 0)
        } ?: AccentTheme.AMBER
    }
}
