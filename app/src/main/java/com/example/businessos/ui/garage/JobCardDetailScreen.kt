package com.example.businessos.ui.garage

import com.example.businessos.ui.util.AppLanguageManager
import com.example.businessos.ui.components.FullScreenForm
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PartItem
import com.example.businessos.data.model.Role
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.threed.Vehicle3DInspector
import com.example.businessos.util.FeatureFlag
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.ui.theme.StatusAmber
import com.example.businessos.ui.theme.StatusGreen
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TealAccent
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun JobCardDetailScreen(
    jobCardId: String,
    viewModel: GarageViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val allJobs by viewModel.jobCards.collectAsState()
    val jobCard = allJobs.find { it.id == jobCardId }
    val partsFlow = remember(jobCardId) { viewModel.partsFor(jobCardId) }
    val historyFlow = remember(jobCardId) { viewModel.historyFor(jobCardId) }
    val parts by partsFlow.collectAsState(initial = emptyList())
    val stageHistory by historyFlow.collectAsState(initial = emptyList())
    val userRole = session?.activeRole ?: Role.CASHIER

    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddPartDialog by remember { mutableStateOf(false) }
    var showReopenDialog by remember { mutableStateOf(false) }
    var presetPartName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage, successMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (jobCard == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(AppLanguageManager.tr("Order not found", "Oda haipatikani"), color = TextSecondary)
                TextButton(onClick = onNavigateBack) { Text(AppLanguageManager.tr("Go back", "Rudi"), color = AmberAccent) }
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Bar
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppLanguageManager.tr("Back", "Rudi"), tint = TextPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = jobCard.vehiclePlate,
                            style = MaterialTheme.typography.titleLarge,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = AppLanguageManager.tr("Customer: ${jobCard.customerName}", "Mteja: ${jobCard.customerName}"),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    JobStagePill(stage = jobCard.stage)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Optional 3D Vehicle Inspector (off by default)
                if (FeatureFlag.ENABLE_3D_INSPECTOR) {
                    item {
                        Vehicle3DInspector(
                            onZoneSelected = { zone ->
                                presetPartName = zone.title
                                showAddPartDialog = true
                            }
                        )
                    }
                }

                // Description Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = AppLanguageManager.tr("Details", "Maelezo"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = jobCard.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = AppLanguageManager.tr("Phone: ${jobCard.customerPhone}", "Simu: ${jobCard.customerPhone}"),
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // WhatsApp Approval Section
                if (jobCard.stage == JobStage.AWAITING_APPROVAL) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF133426)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppLanguageManager.tr("Ask for approval on WhatsApp", "Omba idhini kwa WhatsApp"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = AppLanguageManager.tr("Send the full estimate of KSh ${jobCard.estimateTotal.toInt()} to ${jobCard.customerName} on WhatsApp.", "Tuma makadirio kamili ya KSh ${jobCard.estimateTotal.toInt()} kwa ${jobCard.customerName} kwa WhatsApp."),
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        WhatsAppHelper.openWhatsAppEstimateApproval(
                                            context = context,
                                            jobCard = jobCard,
                                            businessName = session?.activeBusinessName,
                                            parts = parts
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = TextOnAccent),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppLanguageManager.tr("Send estimate on WhatsApp", "Tuma makadirio kwa WhatsApp"), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Parts & Estimate Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppLanguageManager.tr("Parts and labour estimate", "Makadirio ya vipuri na ufundi"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                                TextButton(onClick = {
                                    presetPartName = ""
                                    showAddPartDialog = true
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppLanguageManager.tr("Add part", "Ongeza kipuri"), color = AmberAccent, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            parts.forEach { part ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${part.name}",
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = part.unitPrice.formatCompact(),
                                        color = AmberAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    if (userRole != Role.CASHIER) {
                                        IconButton(
                                            onClick = { viewModel.removePartFromJob(jobCard.id, parts, part.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = AppLanguageManager.tr("Delete", "Futa"), tint = StatusRed, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }

                            if (parts.isEmpty()) {
                                Text(
                                    text = AppLanguageManager.tr("Nothing added yet.", "Hakuna kilichoongezwa bado."),
                                    color = TextMuted,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(AppLanguageManager.tr("Total estimate:", "Jumla ya makadirio:"), color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "KSh ${jobCard.estimateTotal.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Stage History Timeline
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = AppLanguageManager.tr("History", "Historia"),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            stageHistory.forEach { transition ->
                                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(transition.timestamp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(AmberAccent)
                                            .padding(top = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = transition.stage.title,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = dateStr, fontSize = 12.sp, color = TextMuted)
                                        }
                                        if (!transition.note.isNullOrEmpty()) {
                                            Text(text = transition.note, fontSize = 14.sp, color = TextSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Stage Transition Bar
            Surface(
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (userRole != Role.CASHIER && jobCard.stage != JobStage.INTAKE) {
                        OutlinedButton(
                            onClick = { showReopenDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppLanguageManager.tr("Reopen a stage", "Fungua hatua tena"), color = StatusRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    val nextStage = jobCard.stage.next()
                    if (nextStage != null) {
                        Button(
                            onClick = { viewModel.advanceStage(jobCard.id) },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                        ) {
                            Text(AppLanguageManager.tr("Move to ${nextStage.title}", "Hamisha hadi ${nextStage.title}"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Surface(
                            color = StatusGreen.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text(AppLanguageManager.tr("Fully invoiced", "Imeshatolewa ankara"), color = StatusGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showAddPartDialog) {
            AddPartDialog(
                initialName = presetPartName,
                onDismiss = { showAddPartDialog = false },
                onAdd = { name, cost ->
                    viewModel.addPartToJob(jobCard.id, parts, name, Money.fromShillingsDouble(cost))
                    showAddPartDialog = false
                }
            )
        }

        if (showReopenDialog) {
            ReopenStageDialog(
                currentStage = jobCard.stage,
                onDismiss = { showReopenDialog = false },
                onConfirm = { targetStage, reason ->
                    viewModel.reopenStage(jobCard.id, targetStage, reason)
                    showReopenDialog = false
                }
            )
        }
    }
}

@Composable
fun AddPartDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onAdd: (name: String, cost: Double) -> Unit
) {
    var partName by remember { mutableStateOf(initialName) }
    var partCost by remember { mutableStateOf("") }

    val repairPresets = remember {
        listOf(
            // Bodywork & Paint
            "Bumper Dent & Welding" to 3500.0,
            "Panel Beating Fender/Door" to 4500.0,
            "Full Bumper Respray (Rangi)" to 4500.0,
            "Scratch Buffing & Polish" to 2500.0,
            // Interior & Upholstery
            "Torn Leather Seat Stitching" to 3000.0,
            "Full Leather Reupholstery" to 18000.0,
            "Roof Lining Repair (Dari)" to 4500.0,
            // Tyres & Wheels
            "Tyre Burst / Puncture Repair" to 800.0,
            "Rim Bend Straightening" to 2500.0,
            "Wheel Alignment & Balancing" to 2000.0,
            // Engine & Service
            "Radiator Leak & Cooling Flush" to 3500.0,
            "Full Engine Oil & Filter Service" to 6500.0,
            // Brakes & Electrical
            "Front/Rear Brake Pads" to 3500.0,
            "Brake Disc Skimming" to 2400.0,
            "Alternator / Starter Rewind" to 4500.0
        )
    }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Column {
                Text(AppLanguageManager.tr("Add part or labour", "Ongeza kipuri au ufundi"), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(AppLanguageManager.tr("Body, paint, leather seats, tyres and engine work", "Ufundi wa bodi, rangi, viti vya ngozi, matairi na injini"), color = TextMuted, fontSize = 14.sp)
            }
        },
        text = {
            Column {
                Text(
                    text = AppLanguageManager.tr("Quick picks (tap to fill in):", "Chagua haraka (gusa kujaza):"),
                    fontSize = 12.sp,
                    color = AmberAccent,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Chips for fast 1-tap repair selection
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    items(repairPresets) { (presetTitle, presetPrice) ->
                        Surface(
                            color = DarkSurfaceElevated,
                            border = BorderStroke(1.dp, if (partName == presetTitle) AmberAccent else DarkBorder),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                partName = presetTitle
                                partCost = presetPrice.toInt().toString()
                            }
                        ) {
                            Text(
                                text = "$presetTitle (KSh ${presetPrice.toInt()})",
                                fontSize = 12.sp,
                                color = if (partName == presetTitle) AmberAccent else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = partName,
                    onValueChange = { partName = it },
                    label = { Text(AppLanguageManager.tr("Item or part", "Bidhaa au kipuri")) },
                    placeholder = { Text(AppLanguageManager.tr("e.g. Pair of front shock absorbers", "mf. Jozi ya shock absorber za mbele")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = partCost,
                    onValueChange = { partCost = it },
                    label = { Text(AppLanguageManager.tr("Cost (KSh)", "Gharama (KSh)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = partCost.toDoubleOrNull() ?: 0.0
                    if (partName.isNotBlank()) {
                        onAdd(partName, cost)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Add to estimate", "Ongeza kwenye makadirio"), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi"), color = TextSecondary) }
        }
    )
}

@Composable
fun ReopenStageDialog(
    currentStage: JobStage,
    onDismiss: () -> Unit,
    onConfirm: (JobStage, String) -> Unit
) {
    val eligibleStages = JobStage.entries.filter { it.order < currentStage.order }
    var selectedTarget by remember { mutableStateOf(eligibleStages.lastOrNull() ?: JobStage.INTAKE) }
    var reason by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(AppLanguageManager.tr("Reopen a stage", "Fungua hatua tena"), color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = AppLanguageManager.tr("Going back a stage needs a reason, and it is recorded in the activity log.", "Kurudi nyuma hatua kunahitaji sababu, na inarekodiwa kwenye kumbukumbu."),
                    color = StatusRed,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(AppLanguageManager.tr("Go back to:", "Rudi hadi:"), color = TextSecondary, fontSize = 14.sp)
                eligibleStages.forEach { stage ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedTarget == stage,
                            onClick = { selectedTarget = stage },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stage.title, color = TextPrimary, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text(AppLanguageManager.tr("Reason", "Sababu")) },
                    placeholder = { Text(AppLanguageManager.tr("e.g. Customer asked for another check", "mf. Mteja aliomba ukaguzi mwingine")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) {
                        onConfirm(selectedTarget, reason)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Confirm", "Thibitisha"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi"), color = TextSecondary) }
        }
    )
}
