package com.example.businessos.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.sync.SyncUiState
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.StockItemThumbnail
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockAuditScreen(
    items: List<ItemEntity>,
    businessName: String?,
    syncState: SyncUiState,
    onSwitchWorkspace: () -> Unit,
    onNavigateBack: () -> Unit,
    onConfirmAudit: suspend (List<Pair<ItemEntity, Int>>) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Map of itemId to physical count entered
    val countsMap = remember(items) {
        mutableStateMapOf<String, String>().apply {
            items.forEach { item ->
                put(item.id, item.stockQty.toString())
            }
        }
    }

    var isSubmitting by remember { mutableStateOf(false) }
    var auditCompleted by remember { mutableStateOf(false) }

    // Compute stats
    val variances = items.map { item ->
        val physical = countsMap[item.id]?.toIntOrNull() ?: item.stockQty
        val diff = physical - item.stockQty
        Triple(item, physical, diff)
    }

    val totalItems = items.size
    val shortageCount = variances.count { it.third < 0 }
    val surplusCount = variances.count { it.third > 0 }
    val balancedCount = variances.count { it.third == 0 }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(DarkSurface)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    BusinessOsTopBar(
                        title = "Ukaguzi wa Akiba (Stock Take)",
                        businessName = businessName,
                        role = null,
                        syncState = syncState,
                        onSwitchWorkspace = onSwitchWorkspace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (shortageCount > 0) StatusRed.copy(alpha = 0.5f) else AmberAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "MATOKEO YA UKAGUZI WA AKIBA (STOCK AUDIT SUMMARY)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Bidhaa Zote", fontSize = 11.sp, color = TextSecondary)
                            Text("$totalItems", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column {
                            Text("Zilizolingana", fontSize = 11.sp, color = TextSecondary)
                            Text("$balancedCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                        }
                        Column {
                            Text("Pungufu (Short)", fontSize = 11.sp, color = TextSecondary)
                            Text("$shortageCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (shortageCount > 0) StatusRed else TextPrimary)
                        }
                        Column {
                            Text("Ziada (Surplus)", fontSize = 11.sp, color = TextSecondary)
                            Text("$surplusCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (surplusCount > 0) StatusAmber else TextPrimary)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HESABU HALISI DUKANI (ENTER PHYSICAL COUNT)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                if (variances.any { it.third != 0 }) {
                    Surface(
                        color = StatusAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${shortageCount + surplusCount} Zina Tofauti",
                            color = StatusAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val currentInput = countsMap[item.id] ?: item.stockQty.toString()
                    val physicalInt = currentInput.toIntOrNull() ?: item.stockQty
                    val variance = physicalInt - item.stockQty

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            when {
                                variance < 0 -> StatusRed.copy(alpha = 0.6f)
                                variance > 0 -> StatusAmber.copy(alpha = 0.6f)
                                else -> CardBorder
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StockItemThumbnail(
                                itemId = item.id,
                                itemName = item.name,
                                size = 46.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Mfumo: ${item.stockQty}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = when {
                                            variance < 0 -> "Tofauti: $variance (Pungufu)"
                                            variance > 0 -> "Tofauti: +$variance (Ziada)"
                                            else -> "✓ Sawa"
                                        },
                                        color = when {
                                            variance < 0 -> StatusRed
                                            variance > 0 -> StatusAmber
                                            else -> StatusGreen
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Input for physical count
                            OutlinedTextField(
                                value = currentInput,
                                onValueChange = { countsMap[item.id] = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(75.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberAccent,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Action Area
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val dateStr = sdf.format(Date())
                            val message = buildString {
                                appendLine("📋 *RIPOTI YA UKAGUZI WA AKIBA (STOCK TAKE)*")
                                appendLine("🏢 *Biashara:* ${businessName ?: "My Business"}")
                                appendLine("📅 *Tarehe:* $dateStr")
                                appendLine("─────────────────")
                                appendLine("📦 *Jumla ya Bidhaa:* $totalItems")
                                appendLine("✅ *Zilizolingana:* $balancedCount")
                                appendLine("⚠️ *Zenye Pungufu:* $shortageCount")
                                appendLine("➕ *Zenye Ziada:* $surplusCount")
                                appendLine("─────────────────")
                                val varied = variances.filter { it.third != 0 }
                                if (varied.isNotEmpty()) {
                                    appendLine("*MICHANGANUO YA TOFAUTI:*")
                                    varied.forEach { (item, physical, diff) ->
                                        val mark = if (diff < 0) "⚠️ Pungufu: $diff" else "➕ Ziada: +$diff"
                                        appendLine("• ${item.name}: Mfumo=${item.stockQty}, Hesabu=$physical ($mark)")
                                    }
                                } else {
                                    appendLine("✅ *Hakuna tofauti yoyote. Akiba iko sawa 100%!*")
                                }
                                appendLine("─────────────────")
                                appendLine("_Imetolewa na BusinessOS_")
                            }
                            WhatsAppHelper.shareGeneralText(context, message)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = StatusGreen)
                        Spacer(Modifier.width(6.dp))
                        Text("Tuma Ripoti", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSubmitting = true
                                val updates = items.map { item ->
                                    val physical = countsMap[item.id]?.toIntOrNull() ?: item.stockQty
                                    Pair(item, physical)
                                }
                                onConfirmAudit(updates)
                                isSubmitting = false
                                auditCompleted = true
                                snackbarHostState.showSnackbar("Ukaguzi wa akiba umethibitishwa kikamilifu!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent),
                        enabled = !isSubmitting,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = TextOnAccent, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Thibitisha Ukaguzi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
