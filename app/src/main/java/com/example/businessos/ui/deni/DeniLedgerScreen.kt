package com.example.businessos.ui.deni

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.ui.components.MpesaParser
import com.example.businessos.ui.components.ParsedMpesaTransaction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.DarkSurface
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
fun DeniLedgerScreen(
    viewModel: DeniViewModel,
    onNavigateBack: () -> Unit,
    onSwitchWorkspace: () -> Unit
) {
    val context = LocalContext.current
    val session by viewModel.session.collectAsState()
    val deniCustomers by viewModel.deniList.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showBulkReminderDialog by remember { mutableStateOf(false) }
    var paymentTargetCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var paymentAmountInput by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var mpesaCodeInput by remember { mutableStateOf("") }
    var mpesaSmsInput by remember { mutableStateOf("") }
    var showMpesaPasteBox by remember { mutableStateOf(false) }
    var parsedMpesaInfo by remember { mutableStateOf<ParsedMpesaTransaction?>(null) }
    var reminderTargetCustomer by remember { mutableStateOf<CustomerEntity?>(null) }

    val totalDeni = deniCustomers.sumOf { it.balance }
    val overdueCount = deniCustomers.count { cust ->
        val due = cust.dueDate
        due != null && due < System.currentTimeMillis()
    }
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCustomerDialog = true },
                containerColor = AmberAccent,
                contentColor = TextOnAccent
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BusinessOsTopBar(
                title = "Deni Ledger (Madeni)",
                businessName = session?.activeBusinessName,
                role = session?.activeRole,
                onSwitchWorkspace = onSwitchWorkspace
            )

            // Total Outstanding Banner
            Box(modifier = Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(16.dp))) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF281E10)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "JUMLA YA MADENI (CREDIT OWED)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "KSh ${totalDeni.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                if (overdueCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "⚠️ $overdueCount zimepitiliza muda (Overdue)",
                                        color = StatusRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF382A10))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "${deniCustomers.size} Wateja",
                                    color = AmberAccent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
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
                    text = "ORODHA YA MADENI (${deniCustomers.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )

                if (deniCustomers.any { it.balance > 0 }) {
                    OutlinedButton(
                        onClick = { showBulkReminderDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusGreen),
                        border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp), tint = StatusGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kumbusha Wote WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deniCustomers, key = { it.id }) { cust ->
                    DeniCustomerCard(
                        customer = cust,
                        onRecordPayment = {
                            paymentTargetCustomer = cust
                            paymentAmountInput = cust.balance.toInt().toString()
                        },
                        onSendWhatsAppReminder = {
                            WhatsAppHelper.openWhatsAppDeniReminder(
                                context = context,
                                customerName = cust.name,
                                customerPhone = cust.phone,
                                balance = cust.balance,
                                businessName = session?.activeBusinessName,
                                dueDate = cust.dueDate
                            )
                            viewModel.recordReminderSent(cust.id, cust)
                        },
                        onEditDueDate = {
                            reminderTargetCustomer = cust
                        }
                    )
                }

                if (deniCustomers.isEmpty()) {
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
                                Text(
                                    text = "Hakuna Deni Lililobaki",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Wateja wote wamelipa au bado hujaweka mauzo ya deni.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddCustomerDialog) {
            AddCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onAdd = { name, phone, initialBal, dueDate, notes ->
                    viewModel.addCustomer(name, phone, initialBal, dueDate, notes)
                    showAddCustomerDialog = false
                }
            )
        }

        if (reminderTargetCustomer != null) {
            SetDueDateDialog(
                customer = reminderTargetCustomer!!,
                onDismiss = { reminderTargetCustomer = null },
                onSave = { dueDate, notes ->
                    viewModel.updateCustomerReminder(reminderTargetCustomer!!.id, dueDate, notes)
                    reminderTargetCustomer = null
                }
            )
        }

        if (paymentTargetCustomer != null) {
            AlertDialog(
                onDismissRequest = {
                    paymentTargetCustomer = null
                    showMpesaPasteBox = false
                    parsedMpesaInfo = null
                    mpesaCodeInput = ""
                    mpesaSmsInput = ""
                },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Lipa Deni • ${paymentTargetCustomer?.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Deni Lililopo: KSh ${paymentTargetCustomer?.balance?.toInt()}",
                            color = AmberAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "Njia ya Malipo:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf(PaymentMethod.CASH to "Taslimu (Cash)", PaymentMethod.MPESA to "M-Pesa").forEach { (method, label) ->
                                val isSelected = paymentMethod == method
                                Surface(
                                    color = if (isSelected) AmberAccent else DarkBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) AmberAccent else DarkBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 3.dp)
                                        .clickable { paymentMethod = method }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) TextOnAccent else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        if (paymentMethod == PaymentMethod.MPESA) {
                            Surface(
                                color = DarkBackground,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SMS ya M-Pesa (Auto-Fill):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusGreen
                                        )
                                        TextButton(onClick = { showMpesaPasteBox = !showMpesaPasteBox }) {
                                            Icon(Icons.Default.ContentPaste, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text(if (showMpesaPasteBox) "Funga" else "Bandika SMS", color = StatusGreen, fontSize = 11.sp)
                                        }
                                    }

                                    if (showMpesaPasteBox) {
                                        OutlinedTextField(
                                            value = mpesaSmsInput,
                                            onValueChange = { text ->
                                                mpesaSmsInput = text
                                                val parsed = MpesaParser.parse(text)
                                                if (parsed != null && parsed.isValid) {
                                                    parsedMpesaInfo = parsed
                                                    if (parsed.amount > 0) {
                                                        paymentAmountInput = parsed.amount.toInt().toString()
                                                    }
                                                    if (parsed.code.isNotBlank()) {
                                                        mpesaCodeInput = parsed.code
                                                    }
                                                }
                                            },
                                            label = { Text("Bandika SMS ya M-Pesa hapa...") },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = StatusGreen,
                                                unfocusedBorderColor = DarkBorder,
                                                focusedTextColor = TextPrimary,
                                                unfocusedTextColor = TextPrimary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                            minLines = 2,
                                            maxLines = 4
                                        )
                                    }

                                    if (parsedMpesaInfo != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            color = StatusGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "✓ Code: ${parsedMpesaInfo?.code} • KSh ${parsedMpesaInfo?.amount?.toInt()} • ${parsedMpesaInfo?.senderName}",
                                                color = StatusGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = mpesaCodeInput,
                                        onValueChange = { mpesaCodeInput = it.uppercase() },
                                        label = { Text("M-Pesa Reference Code") },
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = StatusGreen,
                                            unfocusedBorderColor = DarkBorder,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = paymentAmountInput,
                            onValueChange = { paymentAmountInput = it },
                            label = { Text("Kiasi Kilicholipwa (KSh)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = StatusGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amount = paymentAmountInput.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                viewModel.recordPayment(
                                    paymentTargetCustomer!!.id,
                                    amount,
                                    paymentMethod,
                                    if (paymentMethod == PaymentMethod.MPESA) mpesaCodeInput.ifBlank { null } else null
                                )
                            }
                            paymentTargetCustomer = null
                            showMpesaPasteBox = false
                            parsedMpesaInfo = null
                            mpesaCodeInput = ""
                            mpesaSmsInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = TextOnAccent)
                    ) {
                        Text("Hifadhi Malipo", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        paymentTargetCustomer = null
                        showMpesaPasteBox = false
                        parsedMpesaInfo = null
                        mpesaCodeInput = ""
                        mpesaSmsInput = ""
                    }) {
                        Text("Ghairi (Cancel)", color = TextSecondary)
                    }
                }
            )
        }

        if (showBulkReminderDialog) {
            val debtors = deniCustomers.filter { it.balance > 0 }
            AlertDialog(
                onDismissRequest = { showBulkReminderDialog = false },
                containerColor = DarkSurface,
                title = {
                    Text(
                        text = "Kumbusha Wateja WhatsApp (${debtors.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tuma ujumbe wa makubaliano ya deni kwa kila mteja kwa mbofyo 1:",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 350.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(debtors, key = { it.id }) { cust ->
                                Surface(
                                    color = DarkBackground,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, DarkBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(cust.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("Deni: KSh ${cust.balance.toInt()} • ${cust.phone}", color = AmberAccent, fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                WhatsAppHelper.openWhatsAppDeniReminder(
                                                    context = context,
                                                    customerName = cust.name,
                                                    customerPhone = cust.phone,
                                                    balance = cust.balance,
                                                    businessName = session?.activeBusinessName,
                                                    dueDate = cust.dueDate
                                                )
                                                viewModel.recordReminderSent(cust.id, cust)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = TextOnAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Tuma", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBulkReminderDialog = false }) {
                        Text("Imekamilika", color = AmberAccent, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun DeniCustomerCard(
    customer: CustomerEntity,
    onRecordPayment: () -> Unit,
    onSendWhatsAppReminder: () -> Unit,
    onEditDueDate: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val now = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Customer Name + Outstanding Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = customer.phone,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "KSh ${customer.balance.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        color = StatusRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Deni Linadaiwa",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Due Date & Reminder Status Row
            val dueDate = customer.dueDate
            if (dueDate != null && dueDate > 0) {
                val diffDays = ((dueDate - now) / (1000 * 60 * 60 * 24)).toInt()
                val isOverdue = dueDate < now

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isOverdue) Color(0xFF451313) else if (diffDays <= 2) Color(0xFF382A10) else Color(0xFF0F2E28))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                isOverdue -> "⚠️ IMEPITILIZA (${Math.max(1, -diffDays)} siku) • ${dateFormat.format(Date(dueDate))}"
                                diffDays <= 1 -> "⏳ INALIPWA SASA • ${dateFormat.format(Date(dueDate))}"
                                else -> "📅 Tarehe ya Mwisho: ${dateFormat.format(Date(dueDate))}"
                            },
                            color = if (isOverdue) StatusRed else if (diffDays <= 1) AmberAccent else TealAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Badilisha",
                        color = AmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onEditDueDate() }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bado hujaweka tarehe ya makubaliano",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "+ Weka Tarehe",
                        color = AmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onEditDueDate() }
                    )
                }
            }

            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Maelezo: ${customer.notes}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            if (customer.lastReminderSent > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ Kikumbusho kilitumwa: ${dateFormat.format(Date(customer.lastReminderSent))}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons (1-Tap WhatsApp Reminder & Record Payment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSendWhatsAppReminder,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B3D2F),
                        contentColor = StatusGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Kikumbusho", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = TextOnAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lipa Deni", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SetDueDateDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onSave: (dueDate: Long?, notes: String) -> Unit
) {
    val dayMs = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    var selectedDueDate by remember { mutableStateOf<Long?>(customer.dueDate) }
    var notesInput by remember { mutableStateOf(customer.notes) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Tarehe ya Makubaliano • ${customer.name}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Chagua lini mteja anatarajiwa kukamilisha deni hili:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "3 Siku" to (now + 3 * dayMs),
                        "1 Wiki" to (now + 7 * dayMs),
                        "2 Wiki" to (now + 14 * dayMs),
                        "1 Mwezi" to (now + 30 * dayMs)
                    ).forEach { (label, timestamp) ->
                        val isSelected = selectedDueDate != null && Math.abs(selectedDueDate!! - timestamp) < dayMs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberAccent else Color(0xFF282828))
                                .clickable { selectedDueDate = timestamp }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextOnAccent else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedDueDate != null) {
                    Text(
                        text = "Tarehe iliyochaguliwa: ${dateFormat.format(Date(selectedDueDate!!))}",
                        color = TealAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Maelezo ya makubaliano") },
                    placeholder = { Text("Mfano: Aliahidi kulipa baada ya mshahara") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedDueDate != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ondoa tarehe ya makubaliano",
                        color = StatusRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { selectedDueDate = null }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedDueDate, notesInput) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text("Hifadhi", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ghairi", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, initialCredit: Double, dueDate: Long?, notes: String) -> Unit
) {
    val dayMs = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("0") }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Ongeza Mteja Mpya / Deni", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Jina la Mteja") },
                    placeholder = { Text("Mfano: Mama Boi Kibanda 4") },
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
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Nambari ya Simu") },
                    placeholder = { Text("0712345678") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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
                    value = initialBalance,
                    onValueChange = { initialBalance = it },
                    label = { Text("Kiasi cha Deni la Kwanza (KSh)") },
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

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Lini anatarajiwa kulipa? (Due Date):",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "Siku 3" to (now + 3 * dayMs),
                        "1 Wiki" to (now + 7 * dayMs),
                        "2 Wiki" to (now + 14 * dayMs),
                        "30 Siku" to (now + 30 * dayMs)
                    ).forEach { (label, timestamp) ->
                        val isSelected = selectedDueDate != null && Math.abs(selectedDueDate!! - timestamp) < dayMs
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AmberAccent else Color(0xFF282828))
                                .clickable { selectedDueDate = timestamp }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextOnAccent else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = initialBalance.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onAdd(name, phone, bal, selectedDueDate, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text("Hifadhi Mteja", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ghairi", color = TextSecondary) }
        }
    )
}
