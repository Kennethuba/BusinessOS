package com.example.businessos.ui.staff

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import com.example.businessos.data.local.entity.WagePaymentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import com.example.businessos.data.security.PinSecurity
import com.example.businessos.ui.auth.PinField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.local.dao.MemberWithUser
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.Role
import com.example.businessos.ui.components.RoleBadge
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

@Composable
fun StaffManagementScreen(
    viewModel: StaffViewModel,
    onNavigateBack: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val staffMembers by viewModel.staffMembers.collectAsState()
    val wagePayments by viewModel.wagePayments.collectAsState()
    val userRole = session?.activeRole ?: Role.CASHIER

    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showRecordWageDialog by remember { mutableStateOf(false) }
    var editRoleTarget by remember { mutableStateOf<MemberWithUser?>(null) }
    var resetPinTarget by remember { mutableStateOf<MemberWithUser?>(null) }

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
            if (userRole == Role.OWNER || userRole == Role.MANAGER) {
                FloatingActionButton(
                    onClick = {
                        if (selectedTab == 0) showInviteDialog = true else showRecordWageDialog = true
                    },
                    containerColor = AmberAccent,
                    contentColor = TextOnAccent
                ) {
                    Icon(
                        if (selectedTab == 0) Icons.Default.Add else Icons.Default.Payments,
                        contentDescription = if (selectedTab == 0) "Invite Staff" else "Record Wage"
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
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Text(
                        text = "Wafanyakazi & Mishahara",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
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
                    text = { Text("Wafanyakazi (${staffMembers.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Mishahara (${wagePayments.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            if (selectedTab == 0) {
                Text(
                    text = "TEAM MEMBERS (${staffMembers.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(staffMembers, key = { it.userId }) { member ->
                        StaffMemberCard(
                            member = member,
                            isOwnerViewing = userRole == Role.OWNER,
                            isSelf = member.userId == session?.userId,
                            onChangeRole = { editRoleTarget = member },
                            onResetPin = { resetPinTarget = member },
                            onRemove = { viewModel.removeStaff(member.userId) }
                        )
                    }
                }
            } else {
                val totalWagesPaid = Money(wagePayments.filter { !it.isVoided }.sumOf { it.amountCents })
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "JUMLA YA MISHAHARA ILIYOLIPWA (TOTAL WAGES)",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalWagesPaid.formatted,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusGreen
                        )
                        Text(
                            text = "Malipo ${wagePayments.size} yaliyothibitishwa",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = "HISTORIA YA MALIPO YA MISHAHARA",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                if (wagePayments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hakuna malipo ya mishahara yaliyorekodiwa bado.\nBonyeza kitufe hapa chini kulipa mshahara.",
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(wagePayments, key = { it.id }) { payment ->
                            WagePaymentCard(payment = payment)
                        }
                    }
                }
            }
        }

        if (showRecordWageDialog) {
            RecordWagePaymentDialog(
                staffMembers = staffMembers,
                onDismiss = { showRecordWageDialog = false },
                onConfirm = { staffId, staffName, amount, method, notes ->
                    viewModel.recordWagePayment(staffId, staffName, amount, method, notes)
                    showRecordWageDialog = false
                }
            )
        }

        if (showInviteDialog) {
            InviteStaffDialog(
                onDismiss = { showInviteDialog = false },
                onInvite = { name, phone, role, pin ->
                    viewModel.inviteStaff(name, phone, role, pin)
                    showInviteDialog = false
                }
            )
        }

        resetPinTarget?.let { target ->
            ResetPinDialog(
                name = target.name,
                onDismiss = { resetPinTarget = null },
                onReset = { pin ->
                    viewModel.resetPin(target.userId, pin)
                    resetPinTarget = null
                }
            )
        }

        if (editRoleTarget != null) {
            EditRoleDialog(
                member = editRoleTarget!!,
                onDismiss = { editRoleTarget = null },
                onSave = { newRole ->
                    viewModel.updateRole(editRoleTarget!!.userId, newRole)
                    editRoleTarget = null
                }
            )
        }
    }
}

@Composable
fun StaffMemberCard(
    member: MemberWithUser,
    isOwnerViewing: Boolean,
    isSelf: Boolean,
    onChangeRole: () -> Unit,
    onResetPin: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = member.name + if (isSelf) " (You)" else "",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RoleBadge(role = member.role)
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (member.phone.isNotBlank()) {
                    Text(text = member.phone, fontSize = 14.sp, color = TextSecondary)
                }
            }

            if (isOwnerViewing && !isSelf && member.role != Role.OWNER) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onResetPin) {
                        Icon(Icons.Default.Lock, contentDescription = "Reset PIN", tint = AmberAccent)
                    }
                    IconButton(onClick = onChangeRole) {
                        Icon(Icons.Default.Edit, contentDescription = "Change Role", tint = AmberAccent)
                    }
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove Staff", tint = StatusRed)
                    }
                }
            }
        }
    }
}

private val assignableRoles = listOf(
    Triple(Role.CASHIER, "Cashier", "Can sell, take payments and open job cards. No price changes or deleting."),
    Triple(Role.MANAGER, "Manager", "Can also edit stock, record expenses, pay wages and void records."),
    Triple(Role.VIEWER, "Viewer", "Can only look at reports. Cannot record anything.")
)

@Composable
fun InviteStaffDialog(
    onDismiss: () -> Unit,
    onInvite: (name: String, phone: String, role: Role, pin: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(Role.CASHIER) }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Add staff member", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full name") },
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
                    label = { Text("Phone number") },
                    placeholder = { Text("0712345678") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                PinField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = "PIN for this person (${PinSecurity.PIN_LENGTH} digits)"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Role", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.height(6.dp))

                assignableRoles.forEach { (role, title, description) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .clickable { selectedRole = role }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text(description, fontSize = 14.sp, color = TextSecondary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && pin.length == PinSecurity.PIN_LENGTH) {
                        onInvite(name, phone, selectedRole, pin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun EditRoleDialog(
    member: MemberWithUser,
    onDismiss: () -> Unit,
    onSave: (Role) -> Unit
) {
    var selectedRole by remember { mutableStateOf(member.role) }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Change role: ${member.name}", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                assignableRoles.forEach { (role, title, _) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRole = role }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedRole == role,
                            onClick = { selectedRole = role },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(title, color = TextPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedRole) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}

@Composable
fun WagePaymentCard(payment: WagePaymentEntity) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(payment.createdAt) { sdf.format(Date(payment.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = payment.staffName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.sp
                    )
                    Text(
                        text = payment.paymentNumber,
                        color = AmberAccent,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = payment.amountMoney.formatted,
                    fontWeight = FontWeight.ExtraBold,
                    color = StatusGreen,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = when (payment.paymentMethod) {
                        PaymentMethod.MPESA -> StatusGreen.copy(alpha = 0.15f)
                        PaymentMethod.BANK -> Color(0xFF60A5FA).copy(alpha = 0.15f)
                        else -> AmberAccent.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = payment.paymentMethod.name,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            if (!payment.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Maelezo: ${payment.notes}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun RecordWagePaymentDialog(
    staffMembers: List<MemberWithUser>,
    onDismiss: () -> Unit,
    onConfirm: (staffId: String, staffName: String, amount: Money, method: PaymentMethod, notes: String) -> Unit
) {
    var selectedStaffIndex by remember { mutableStateOf(0) }
    var amountText by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var mpesaCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "Lipa Mshahara (Record Wage)",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (staffMembers.isEmpty()) {
                    Text(
                        text = "Hakuna wafanyakazi waliosajiliwa. Ongeza mfanyakazi kwanza.",
                        color = StatusRed,
                        fontSize = 14.sp
                    )
                } else {
                    Text(
                        text = "Chagua Mfanyakazi:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Column {
                        staffMembers.forEachIndexed { index, member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedStaffIndex = index }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStaffIndex == index,
                                    onClick = { selectedStaffIndex = index },
                                    colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${member.name} (${member.role.name})",
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Kiasi cha Pesa (KSh)") },
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

                    Text(
                        text = "Njia ya Malipo:",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(PaymentMethod.CASH to "Taslimu", PaymentMethod.MPESA to "M-Pesa", PaymentMethod.BANK to "Benki").forEach { (code, label) ->
                            val isSelected = selectedMethod == code
                            Surface(
                                color = if (isSelected) AmberAccent else DarkSurfaceElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, if (isSelected) AmberAccent else DarkBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp)
                                    .clickable { selectedMethod = code }
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

                    if (selectedMethod == PaymentMethod.MPESA) {
                        OutlinedTextField(
                            value = mpesaCode,
                            onValueChange = { mpesaCode = it.uppercase() },
                            label = { Text("M-Pesa Reference Code (e.g. QK89...)") },
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

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Maelezo ya Ziada (Hiari)") },
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
            }
        },
        confirmButton = {
            if (staffMembers.isNotEmpty()) {
                Button(
                    onClick = {
                        val amount = Money.parseOrZero(amountText)
                        if (amount.isPositive && selectedStaffIndex in staffMembers.indices) {
                            val member = staffMembers[selectedStaffIndex]
                            val fullNotes = listOfNotNull(
                                notes.takeIf { it.isNotBlank() },
                                mpesaCode.takeIf { selectedMethod == PaymentMethod.MPESA && it.isNotBlank() }?.let { "M-Pesa: $it" }
                            ).joinToString(" | ")
                            onConfirm(
                                member.userId,
                                member.name,
                                amount,
                                selectedMethod,
                                fullNotes
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                ) {
                    Text("Thibitisha Malipo")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Ghairi", color = TextSecondary) }
        }
    )
}

@Composable
fun ResetPinDialog(
    name: String,
    onDismiss: () -> Unit,
    onReset: (pin: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    FullScreenForm(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("New PIN for $name", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Choose a new ${PinSecurity.PIN_LENGTH}-digit PIN and tell $name privately.", color = TextSecondary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))
                PinField(value = pin, onValueChange = { pin = it }, label = "New PIN")
            }
        },
        confirmButton = {
            Button(
                enabled = pin.length == PinSecurity.PIN_LENGTH,
                onClick = { onReset(pin) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text("Save PIN") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
