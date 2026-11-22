package com.example.businessos.ui.pos

import com.example.businessos.ui.util.AppLanguageManager
import com.example.businessos.ui.components.FullScreenForm
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
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
import com.example.businessos.data.local.entity.LayawayEntity
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LayawayScreen(
    layaways: List<LayawayEntity>,
    businessName: String?,
    onSwitchWorkspace: () -> Unit,
    onNavigateBack: () -> Unit,
    onCreateLayaway: suspend (name: String, phone: String, desc: String, total: Double, deposit: Double, dueDate: Long) -> Unit,
    onRecordPayment: suspend (id: String, amount: Double, method: PaymentMethod) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableStateOf(0) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var targetPaymentLayaway by remember { mutableStateOf<LayawayEntity?>(null) }

    val activeLayaways = layaways.filter { it.status == "ACTIVE" }
    val completedLayaways = layaways.filter { it.status != "ACTIVE" }

    val totalActiveAmount = activeLayaways.sumOf { it.totalAmount }
    val totalDepositsCollected = activeLayaways.sumOf { it.totalPaid }
    val totalBalanceRemaining = activeLayaways.sumOf { it.balanceDue }

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
                        title = "Awali / Layaway (Malipo ya Kidogo)",
                        businessName = businessName,
                        role = null,
                        onSwitchWorkspace = onSwitchWorkspace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = AmberAccent,
                contentColor = TextOnAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Layaway")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "JUMLA YA MALIPO YA AWALI (LAYAWAY OVERVIEW)",
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
                            Text("Iliyolipwa (Deposits)", fontSize = 12.sp, color = TextSecondary)
                            Text("KSh ${totalDepositsCollected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = StatusGreen)
                        }
                        Column {
                            Text("Inayodaiwa (Balance)", fontSize = 12.sp, color = TextSecondary)
                            Text("KSh ${totalBalanceRemaining.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AmberAccent)
                        }
                        Column {
                            Text("Thamani Yote", fontSize = 12.sp, color = TextSecondary)
                            Text("KSh ${totalActiveAmount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = AmberAccent
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Zinazoendelea (${activeLayaways.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Zilizokamilika (${completedLayaways.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            val displayedList = if (selectedTab == 0) activeLayaways else completedLayaways

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == 0) "Hakuna mauzo ya awali yanayoendelea kwa sasa.\nBonyeza + kuweka mteja wa awali."
                        else "Hakuna mauzo yaliyokamilika bado.",
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedList, key = { it.id }) { layaway ->
                        LayawayCard(
                            layaway = layaway,
                            onRecordPayment = { targetPaymentLayaway = layaway },
                            onSendReminder = {
                                val message = """
                                    Jambo ${layaway.customerName},
                                    
                                    Hiki ni kikumbusho cha kirafiki kutoka *${businessName ?: "BusinessOS"}*.
                                    Bidhaa yako ya awali: *${layaway.description}*
                                    Jumla: *KSh ${layaway.totalAmount.toInt()}*
                                    Umelipa: *KSh ${layaway.totalPaid.toInt()}*
                                    Salio lililobaki: *KSh ${layaway.balanceDue.toInt()}*
                                    
                                    Tafadhali kamilisha malipo ili uchukue bidhaa yako. Asante sana!
                                """.trimIndent()
                                WhatsAppHelper.shareGeneralText(context, message)
                            }
                        )
                    }
                }
            }
        }

        if (showCreateDialog) {
            CreateLayawayDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { name, phone, desc, total, deposit, dueDate ->
                    coroutineScope.launch {
                        onCreateLayaway(name, phone, desc, total, deposit, dueDate)
                        showCreateDialog = false
                        snackbarHostState.showSnackbar("Mauzo ya Awali yamehifadhiwa kikamilifu!")
                    }
                }
            )
        }

        if (targetPaymentLayaway != null) {
            RecordLayawayPaymentDialog(
                layaway = targetPaymentLayaway!!,
                onDismiss = { targetPaymentLayaway = null },
                onConfirm = { amount, method ->
                    coroutineScope.launch {
                        onRecordPayment(targetPaymentLayaway!!.id, amount, method)
                        targetPaymentLayaway = null
                        snackbarHostState.showSnackbar("Malipo ya KSh ${amount.toInt()} yamethibitishwa!")
                    }
                }
            )
        }
    }
}

@Composable
fun LayawayCard(
    layaway: LayawayEntity,
    onRecordPayment: () -> Unit,
    onSendReminder: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dueDateStr = remember(layaway.dueDate) { sdf.format(Date(layaway.dueDate)) }
    val progress = if (layaway.totalAmount > 0) (layaway.totalPaid / layaway.totalAmount).toFloat().coerceIn(0f, 1f) else 1f
    val isOverdue = layaway.dueDate < System.currentTimeMillis() && layaway.status == "ACTIVE"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isOverdue) StatusRed.copy(alpha = 0.7f) else CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(layaway.customerName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Text(layaway.customerPhone, color = TextMuted, fontSize = 12.sp)
                }

                Surface(
                    color = if (layaway.status == "COMPLETED") StatusGreen.copy(alpha = 0.15f)
                    else if (isOverdue) StatusRed.copy(alpha = 0.15f) else AmberAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (layaway.status == "COMPLETED") AppLanguageManager.tr("Completed", "Imekamilika")
                        else if (isOverdue) AppLanguageManager.tr("Overdue", "Imepitiliza") else AppLanguageManager.tr("In progress", "Inaendelea"),
                        color = if (layaway.status == "COMPLETED") StatusGreen
                        else if (isOverdue) StatusRed else AmberAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Bidhaa: ${layaway.description}",
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = StatusGreen,
                trackColor = DarkSurfaceElevated,
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Imelipwa: KSh ${layaway.totalPaid.toInt()} (${(progress * 100).toInt()}%)", color = StatusGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Baki: KSh ${layaway.balanceDue.toInt()}", color = if (layaway.balanceDue > 0) AmberAccent else TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Jumla: KSh ${layaway.totalAmount.toInt()}", color = TextSecondary, fontSize = 14.sp)
            }

            Spacer(Modifier.height(6.dp))
            Text("Tarehe ya Mwisho: $dueDateStr", color = if (isOverdue) StatusRed else TextMuted, fontSize = 12.sp)

            if (layaway.status == "ACTIVE") {
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onSendReminder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Kumbusha WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onRecordPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Lipa Sehemu", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateLayawayDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, desc: String, total: Double, deposit: Double, dueDate: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var depositPaidText by remember { mutableStateOf("") }
    var daysCount by remember { mutableStateOf("30") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Mauzo Mapya ya Awali (Layaway)", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Jina la Mteja") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nambari ya Simu (M-Pesa)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Bidhaa Inayohifadhiwa (e.g. Leather Jacket)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalAmountText,
                        onValueChange = { totalAmountText = it },
                        label = { Text("Jumla (KSh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = depositPaidText,
                        onValueChange = { depositPaidText = it },
                        label = { Text("Amana (Deposit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = daysCount,
                    onValueChange = { daysCount = it },
                    label = { Text("Muda wa Kukamilisha (Siku, e.g. 30)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalAmountText.toDoubleOrNull() ?: 0.0
                    val dep = depositPaidText.toDoubleOrNull() ?: 0.0
                    val days = daysCount.toLongOrNull() ?: 30L
                    val dueDate = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L)
                    if (name.isNotBlank() && desc.isNotBlank() && tot > 0) {
                        onConfirm(name, phone, desc, tot, dep, dueDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text("Hifadhi Awali", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ghairi", color = TextSecondary) }
        }
    )
}

@Composable
fun RecordLayawayPaymentDialog(
    layaway: LayawayEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Lipa Sehemu ya Awali • ${layaway.customerName}", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Salio Lililobaki: KSh ${layaway.balanceDue.toInt()}", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                Text("Njia ya Malipo:", fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(PaymentMethod.CASH to "Taslimu", PaymentMethod.MPESA to "M-Pesa", PaymentMethod.BANK to "Benki").forEach { (method, label) ->
                        val isSelected = selectedMethod == method
                        Surface(
                            color = if (isSelected) AmberAccent else DarkSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (isSelected) AmberAccent else DarkBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedMethod = method }
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextOnAccent else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Kiasi Kinacholipwa Sasa (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(amt, selectedMethod)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = TextOnAccent)
            ) {
                Text("Thibitisha Malipo", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ghairi", color = TextSecondary) }
        }
    )
}
