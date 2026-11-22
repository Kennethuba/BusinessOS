package com.example.businessos.ui.garage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.KeyboardType
import com.example.businessos.data.model.JobOrderKind
import com.example.businessos.ui.util.AppLanguageManager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.JobStage
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.ui.theme.StatusAmber
import com.example.businessos.ui.theme.StatusBlue
import com.example.businessos.ui.theme.StatusGreen
import com.example.businessos.ui.theme.StatusPurple
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TealAccent
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary

@Composable
fun JobCardListScreen(
    viewModel: GarageViewModel,
    onNavigateToJobDetail: (String) -> Unit,
    onNavigateToVehicleHistory: () -> Unit,
    onSwitchWorkspace: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val allJobs by viewModel.jobCards.collectAsState()
    val selectedStage by viewModel.selectedStageFilter.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
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

    val filteredJobs = remember(allJobs, selectedStage) {
        if (selectedStage == null) allJobs
        else allJobs.filter { it.stage == selectedStage }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AmberAccent,
                contentColor = TextOnAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Job Card")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BusinessOsTopBar(
                title = "Job Cards",
                businessName = session?.activeBusinessName,
                role = session?.activeRole,
                onSwitchWorkspace = onSwitchWorkspace
            )

            // Stage Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStage == null,
                    onClick = { viewModel.setStageFilter(null) },
                    label = { Text("All (${allJobs.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AmberAccent,
                        selectedLabelColor = TextOnAccent,
                        containerColor = DarkSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )

                JobStage.entries.forEach { stage ->
                    val count = allJobs.count { it.stage == stage }
                    FilterChip(
                        selected = selectedStage == stage,
                        onClick = { viewModel.setStageFilter(stage) },
                        label = { Text("${stage.title} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberAccent,
                            selectedLabelColor = TextOnAccent,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Quick History Search Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface)
                    .clickable { onNavigateToVehicleHistory() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = TealAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Vehicle Repair History",
                        color = TealAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Job Cards List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredJobs, key = { it.id }) { job ->
                    JobCardRow(
                        jobCard = job,
                        onClick = { onNavigateToJobDetail(job.id) }
                    )
                }

                if (filteredJobs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (selectedStage == null) "No Job Cards in Garage" else "No jobs in ${selectedStage?.title}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap the + button to book a vehicle into intake.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateJobCardDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { kind, reference, name, phone, desc ->
                    viewModel.createJobCard(kind, reference, name, phone, desc) { created ->
                        showCreateDialog = false
                        onNavigateToJobDetail(created.id)
                    }
                }
            )
        }
    }
}

@Composable
fun JobCardRow(
    jobCard: JobCardEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Surface(
                    color = Color(0xFF2E2211),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = jobCard.vehiclePlate,
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                JobStagePill(stage = jobCard.stage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = jobCard.description,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = jobCard.customerName,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Text(
                    text = "Estimate: KSh ${jobCard.estimateTotal.toInt()}",
                    fontSize = 13.sp,
                    color = if (jobCard.estimateTotal > 0) StatusGreen else TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun JobStagePill(stage: JobStage) {
    val (bgColor, textColor) = when (stage) {
        JobStage.INTAKE -> Pair(Color(0xFF1E2638), Color(0xFF93C5FD))
        JobStage.DIAGNOSIS -> Pair(Color(0xFF2E1A38), StatusPurple)
        JobStage.PARTS_ASSIGNED -> Pair(Color(0xFF1B2B38), StatusBlue)
        JobStage.AWAITING_APPROVAL -> Pair(Color(0xFF382A10), StatusAmber)
        JobStage.IN_PROGRESS -> Pair(Color(0xFF381010), StatusRed)
        JobStage.COMPLETED -> Pair(Color(0xFF103828), StatusGreen)
        JobStage.INVOICED -> Pair(Color(0xFF103828), TealAccent)
        JobStage.CANCELLED -> Pair(Color(0xFF282828), Color(0xFF9CA3AF))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = stage.title,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CreateJobCardDialog(
    onDismiss: () -> Unit,
    onCreate: (kind: JobOrderKind, reference: String, name: String, phone: String, desc: String) -> Unit
) {
    var kind by remember { mutableStateOf(JobOrderKind.CLIENT_ORDER) }
    var reference by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val isClientOrder = kind == JobOrderKind.CLIENT_ORDER

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = AmberAccent,
        unfocusedBorderColor = DarkBorder,
        focusedTextColor = TextPrimary,
        unfocusedTextColor = TextPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text(AppLanguageManager.tr("New job order", "Kazi mpya"), color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isClientOrder,
                        onClick = { kind = JobOrderKind.CLIENT_ORDER },
                        label = { Text(AppLanguageManager.tr("For a customer", "Ya mteja")) }
                    )
                    FilterChip(
                        selected = !isClientOrder,
                        onClick = { kind = JobOrderKind.PRODUCTION },
                        label = { Text(AppLanguageManager.tr("Make for stock", "Tengeneza kwa akiba")) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = {
                        Text(
                            if (isClientOrder) AppLanguageManager.tr("Vehicle plate or item", "Namba ya gari au bidhaa")
                            else AppLanguageManager.tr("What are you making?", "Unatengeneza nini?")
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                if (isClientOrder) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text(AppLanguageManager.tr("Customer name", "Jina la mteja")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text(AppLanguageManager.tr("Customer phone", "Simu ya mteja")) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppLanguageManager.tr("Details", "Maelezo")) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = fieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(kind, reference, customerName, customerPhone, description) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Create", "Unda"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi"), color = TextSecondary) }
        }
    )
}
