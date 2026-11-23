package com.example.businessos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.util.CompanyLogo

/** The company's logo, or a shop icon when none has been added. */
@Composable
fun BusinessLogo(businessId: String?, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val version = CompanyLogo.version.intValue
    val bitmap = remember(businessId, version) { businessId?.let { CompanyLogo.load(context, it) } }
    Box(
        modifier = modifier.size(size).clip(RoundedCornerShape(10.dp)).background(DarkSurfaceElevated),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(size))
        } else {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(size / 2))
        }
    }
}
