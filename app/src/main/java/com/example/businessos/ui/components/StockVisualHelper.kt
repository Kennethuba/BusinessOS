package com.example.businessos.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object StockImageStorage {

    private fun getImageFile(context: Context, itemId: String): File {
        val dir = File(context.filesDir, "stock_images")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "item_${itemId}.jpg")
    }

    fun hasCustomImage(context: Context, itemId: String): Boolean {
        return getImageFile(context, itemId).exists()
    }

    fun loadCustomImage(context: Context, itemId: String): Bitmap? {
        val file = getImageFile(context, itemId)
        return if (file.exists()) {
            try {
                BitmapFactory.decodeFile(file.absolutePath)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    fun saveCustomImage(context: Context, itemId: String, bitmap: Bitmap) {
        val file = getImageFile(context, itemId)
        try {
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class StockCategoryPreset(
    val category: String,
    val icon: ImageVector,
    val accentColor: Color,
    val keywords: List<String>
)

object StockCategoryRegistry {
    val presets = listOf(
        StockCategoryPreset(
            category = "Safari Boots & Shoes",
            icon = Icons.Filled.Hiking,
            accentColor = Color(0xFFD97706),
            keywords = listOf("boot", "kiatu", "shoe", "safari", "sneaker", "sandal", "leather boot")
        ),
        StockCategoryPreset(
            category = "Leather Jackets & Bags",
            icon = Icons.Filled.ShoppingBag,
            accentColor = Color(0xFF8B5CF6),
            keywords = listOf("leather", "jacket", "ngozi", "bag", "handbag", "purse", "wallet")
        ),
        StockCategoryPreset(
            category = "Leather Belts",
            icon = Icons.Filled.HorizontalRule,
            accentColor = Color(0xFF10B981),
            keywords = listOf("belt", "mshipi", "strap")
        ),
        StockCategoryPreset(
            category = "Engine Oil & Lubricants",
            icon = Icons.Filled.LocalGasStation,
            accentColor = Color(0xFFF59E0B),
            keywords = listOf("oil", "mafuta", "lubricant", "20w50", "10w40", "coolant", "grease")
        ),
        StockCategoryPreset(
            category = "Tyres & Rims",
            icon = Icons.Filled.Album,
            accentColor = Color(0xFF06B6D4),
            keywords = listOf("tyre", "tairi", "rim", "wheel", "tube", "puncture")
        ),
        StockCategoryPreset(
            category = "Brake Pads & Discs",
            icon = Icons.Filled.Adjust,
            accentColor = Color(0xFFEF4444),
            keywords = listOf("brake", "pad", "disc", "caliper", "breki", "lining")
        ),
        StockCategoryPreset(
            category = "Filters & Spares",
            icon = Icons.Filled.Build,
            accentColor = Color(0xFF3B82F6),
            keywords = listOf("filter", "plug", "belt", "shock", "spring", "suspension", "spare")
        ),
        StockCategoryPreset(
            category = "Paint & Body Supplies",
            icon = Icons.Filled.FormatPaint,
            accentColor = Color(0xFFA855F7),
            keywords = listOf("paint", "rangi", "spray", "thinner", "filler", "putty", "sandpaper")
        )
    )

    fun matchPreset(itemName: String): StockCategoryPreset {
        val lower = itemName.lowercase(Locale.getDefault())
        return presets.firstOrNull { preset ->
            preset.keywords.any { keyword -> lower.contains(keyword) }
        } ?: StockCategoryPreset(
            category = "General Stock",
            icon = Icons.Filled.Inventory2,
            accentColor = Color(0xFF34D399),
            keywords = emptyList()
        )
    }
}

/**
 * Clean Stock Thumbnail Component:
 * Displays custom privately saved stock photo if present,
 * or beautiful category visual preset with matching color badge.
 * Zero loose storage on phone gallery!
 */
@Composable
fun StockItemThumbnail(
    itemId: String,
    itemName: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    var customBitmap by remember(itemId) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(itemId) {
        customBitmap = StockImageStorage.loadCustomImage(context, itemId)
    }

    val preset = remember(itemName) { StockCategoryRegistry.matchPreset(itemName) }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(preset.accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        val bmp = customBitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = itemName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            Icon(
                imageVector = preset.icon,
                contentDescription = preset.category,
                tint = preset.accentColor,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
