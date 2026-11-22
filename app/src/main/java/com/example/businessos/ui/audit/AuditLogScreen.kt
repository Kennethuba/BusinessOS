package com.example.businessos.ui.audit

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.AuditLogEntity
import com.example.businessos.data.model.ActionType
import com.example.businessos.data.model.Role
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.RoleBadge
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─── Tamper-Proof Security Header ────────────────────────────────────────────

@Composable
private fun AuditShieldBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13221C)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "DAFTARI LA USALAMA (AUDIT TRAIL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Rekodi Zote za Wafanyakazi Zinalindwa Kisheria",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Mabadiliko ya bei, kufuta mauzo au kurekebisha hisa hayawezi kubadilishwa bila idhini ya mwenye biashara",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )
            }
        }
    }
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

@Composable
fun AuditLogScreen(
    viewModel: AuditLogViewModel,
    businessName: String?,
    userRole: Role?,
    onSwitchWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (userRole != Role.OWNER) {
        AccessDeniedView(onBack)
        return
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF131824)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack, contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                BusinessOsTopBar(
                    title = "Audit Log",
                    businessName = businessName,
                    role = userRole,
                    onSwitchWorkspace = onSwitchWorkspace,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Professional Security & Audit Banner
            AuditShieldBanner()

            AuditSummaryStrip(logs = uiState.logs)
            ActionTypeFilterRow(selected = uiState.selectedFilter, onSelect = viewModel::setFilter)
            Spacer(Modifier.height(4.dp))

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                uiState.logs.isEmpty() -> EmptyAuditState()
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(uiState.logs) { _, log ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(log.id) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 })
                            ) {
                                AuditLogCard(log = log)
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Sub-components ──────────────────────────────────────────────────────────

@Composable
private fun AccessDeniedView(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.Lock, contentDescription = null,
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Owners Only", style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Audit logs are strictly restricted to business owners.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            OutlinedButton(onClick = onBack) { Text("Go Back") }
        }
    }
}

@Composable
private fun AuditSummaryStrip(logs: List<AuditLogEntity>) {
    val total = logs.size
    val today = logs.count {
        val now = Calendar.getInstance()
        val logCal = Calendar.getInstance().apply { timeInMillis = it.createdAt }
        now.get(Calendar.DAY_OF_YEAR) == logCal.get(Calendar.DAY_OF_YEAR)
    }
    val priceOverrides = logs.count { it.actionType == ActionType.PRICE_OVERRIDE }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryChip("Total Logs", "$total", MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
        SummaryChip("Today", "$today", MaterialTheme.colorScheme.secondaryContainer, Modifier.weight(1f))
        SummaryChip("Price Overrides", "$priceOverrides", MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
    }
}

@Composable
private fun SummaryChip(label: String, value: String, bg: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ActionTypeFilterRow(selected: ActionType?, onSelect: (ActionType?) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("All") },
                leadingIcon = if (selected == null) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(14.dp)) }
                } else null
            )
        }
        items(ActionType.entries) { type ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(if (selected == type) null else type) },
                label = { Text(type.name.replace("_", " ")) },
                leadingIcon = if (selected == type) {
                    { Icon(Icons.Filled.Check, null, Modifier.size(14.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = actionTypeColor(type).copy(alpha = 0.22f),
                    selectedLabelColor = actionTypeColor(type)
                )
            )
        }
    }
}

@Composable
fun AuditLogCard(log: AuditLogEntity) {
    val color = actionTypeColor(log.actionType)
    val sdf = remember { SimpleDateFormat("d MMM yy · HH:mm:ss", Locale.getDefault()) }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.Top) {
            // Colored icon badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.14f))
                        .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(actionTypeIcon(log.actionType), null, tint = color, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(4.dp))
                Box(
                    Modifier.width(2.dp).height(28.dp).background(
                        Brush.verticalGradient(listOf(color.copy(alpha = 0.4f), Color.Transparent))
                    )
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        log.actionType.name.replace("_", " "),
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        sdf.format(Date(log.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(6.dp))

                // Before vs After values
                if (!log.beforeValue.isNullOrBlank() || !log.afterValue.isNullOrBlank()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        if (!log.beforeValue.isNullOrBlank()) {
                            Text(
                                "Before: ${log.beforeValue}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!log.afterValue.isNullOrBlank()) {
                            Text(
                                "After:  ${log.afterValue}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Person, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (log.performedByName.isNotBlank()) log.performedByName else log.performedByUserId.take(12),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAuditState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.History, null, Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No audit entries yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Critical actions like price overrides, cancellations, and role changes will appear here.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Composable
fun actionTypeColor(type: ActionType): Color = when (type) {
    ActionType.PRICE_OVERRIDE -> Color(0xFFF59E0B) // Amber
    ActionType.SALE_VOIDED    -> Color(0xFFEF4444) // Red
    ActionType.EXPENSE_VOIDED -> Color(0xFFEF4444) // Red
    ActionType.JOB_CANCELLED  -> Color(0xFFDC2626) // Dark Red
    ActionType.STOCK_EDITED   -> Color(0xFF3B82F6) // Blue
    ActionType.STOCK_ADJUSTED -> Color(0xFF3B82F6) // Blue
    ActionType.WAGE_PAID      -> Color(0xFF10B981) // Green
    ActionType.ROLE_CHANGED   -> Color(0xFF8B5CF6) // Purple
}

fun actionTypeIcon(type: ActionType) = when (type) {
    ActionType.PRICE_OVERRIDE -> Icons.Filled.PriceChange
    ActionType.SALE_VOIDED    -> Icons.Filled.Delete
    ActionType.EXPENSE_VOIDED -> Icons.Filled.Delete
    ActionType.JOB_CANCELLED  -> Icons.Filled.Cancel
    ActionType.STOCK_EDITED   -> Icons.Filled.Inventory
    ActionType.STOCK_ADJUSTED -> Icons.Filled.Inventory
    ActionType.WAGE_PAID      -> Icons.Filled.Payments
    ActionType.ROLE_CHANGED   -> Icons.Filled.AdminPanelSettings
}
