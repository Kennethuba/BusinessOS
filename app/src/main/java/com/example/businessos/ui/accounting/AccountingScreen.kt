package com.example.businessos.ui.accounting

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.businessos.data.local.entity.*
import com.example.businessos.data.model.*
import com.example.businessos.sync.SyncUiState
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel,
    syncState: SyncUiState = SyncUiState.SYNCED,
    onSwitchWorkspace: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val bills by viewModel.bills.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val pnlReport by viewModel.pnlReport.collectAsState()
    val balanceSheetReport by viewModel.balanceSheetReport.collectAsState()
    val trialBalanceReport by viewModel.trialBalanceReport.collectAsState()
    val arAgeingReport by viewModel.arAgeingReport.collectAsState()
    val apAgeingReport by viewModel.apAgeingReport.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Cashbook", "Invoices (Ankara)", "Bills (Madeni)", "General Ledger", "Reports (Ripoti)", "Funga Siku (Cash-Up)")

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var showCreateBillDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var targetInvoiceForPayment by remember { mutableStateOf<InvoiceEntity?>(null) }
    var targetBillForPayment by remember { mutableStateOf<BillEntity?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF131824))) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    BusinessOsTopBar(
                        title = "Daftari ya Hesabu (Accounting)",
                        businessName = uiState.businessName,
                        role = uiState.userRole,
                        syncState = syncState,
                        onSwitchWorkspace = onSwitchWorkspace,
                        modifier = Modifier.weight(1f)
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1A2234),
                    contentColor = AmberAccent,
                    edgePadding = 12.dp
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                if (index == 4) viewModel.refreshReports()
                            },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            when (selectedTab) {
                0 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddExpenseDialog = true },
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Rekodi Matumizi", fontWeight = FontWeight.Bold) }
                    )
                }
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateInvoiceDialog = true },
                        containerColor = AmberAccent,
                        contentColor = TextOnAccent,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Ankara Mpya (New Invoice)", fontWeight = FontWeight.Bold) }
                    )
                }
                2 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateBillDialog = true },
                        containerColor = Color(0xFF3B82F6),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text("Bili ya Mzigo (New Bill)", fontWeight = FontWeight.Bold) }
                    )
                }
                3 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showTransferDialog = true },
                        containerColor = TealAccent,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
                        text = { Text("Hamisha Pesa (Transfer)", fontWeight = FontWeight.Bold) }
                    )
                }
                else -> {}
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> CashbookTab(
                    uiState = uiState,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
                1 -> InvoicesTab(
                    invoices = invoices,
                    onPayInvoice = { targetInvoiceForPayment = it }
                )
                2 -> BillsTab(
                    bills = bills,
                    onPayBill = { targetBillForPayment = it }
                )
                3 -> GeneralLedgerTab(
                    accounts = accounts,
                    journalEntries = journalEntries
                )
                4 -> ReportsTab(
                    pnl = pnlReport,
                    balanceSheet = balanceSheetReport,
                    trialBalance = trialBalanceReport,
                    arAgeing = arAgeingReport,
                    apAgeing = apAgeingReport,
                    onRefresh = { viewModel.refreshReports() }
                )
                5 -> DailyCashUpTab(
                    sales = sales,
                    expenses = expenses,
                    businessName = uiState.businessName
                )
            }
        }
    }

    // Dialogs
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { cat, title, amount, method, note ->
                viewModel.addExpense(cat, title, amount, method, note)
                showAddExpenseDialog = false
            }
        )
    }

    if (showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            onDismiss = { showCreateInvoiceDialog = false },
            onConfirm = { custName, custPhone, desc, amount, disc, tax, method, notes ->
                viewModel.createInvoice(
                    customerId = UUID.randomUUID().toString(),
                    customerName = custName,
                    customerPhone = custPhone,
                    items = listOf(InvoiceLineItem(description = desc, qty = 1, unitPrice = amount)),
                    discountAmount = disc,
                    taxAmount = tax,
                    dueDate = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000),
                    notes = notes,
                    paymentMethod = method
                )
                showCreateInvoiceDialog = false
            }
        )
    }

    if (showCreateBillDialog) {
        CreateBillDialog(
            suppliers = suppliers,
            onDismiss = { showCreateBillDialog = false },
            onConfirm = { suppName, suppPhone, billNum, amount, cat, desc ->
                val existing = suppliers.find { it.name.equals(suppName.trim(), ignoreCase = true) }
                val suppId = existing?.id ?: UUID.randomUUID().toString()
                if (existing == null) {
                    viewModel.createSupplier(suppName, suppPhone, "")
                }
                viewModel.createBill(
                    supplierId = suppId,
                    supplierName = suppName,
                    billNumber = billNum,
                    totalAmount = amount,
                    dueDate = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000),
                    category = cat,
                    description = desc
                )
                showCreateBillDialog = false
            }
        )
    }

    if (showTransferDialog) {
        AccountTransferDialog(
            accounts = accounts,
            onDismiss = { showTransferDialog = false },
            onConfirm = { fromCode, toCode, amount, ref, desc ->
                viewModel.recordAccountTransfer(fromCode, toCode, amount, ref, desc)
                showTransferDialog = false
            }
        )
    }

    if (targetInvoiceForPayment != null) {
        RecordInvoicePaymentDialog(
            invoice = targetInvoiceForPayment!!,
            onDismiss = { targetInvoiceForPayment = null },
            onConfirm = { amt, method, ref ->
                viewModel.recordInvoicePayment(targetInvoiceForPayment!!.id, amt, method, ref, "")
                targetInvoiceForPayment = null
            }
        )
    }

    if (targetBillForPayment != null) {
        RecordBillPaymentDialog(
            bill = targetBillForPayment!!,
            onDismiss = { targetBillForPayment = null },
            onConfirm = { amt, method, ref ->
                viewModel.recordBillPayment(targetBillForPayment!!.id, amt, method, ref, "")
                targetBillForPayment = null
            }
        )
    }
}

// ==========================================
// TAB 1: CASHBOOK & P&L OVERVIEW
// ==========================================
@Composable
private fun CashbookTab(
    uiState: AccountingUiState,
    onDeleteExpense: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ProfitSummaryCard(
                totalSales = uiState.totalSales,
                totalExpenses = uiState.totalExpenses,
                netProfit = uiState.netProfit,
                totalDeni = uiState.totalDeniOwed,
                totalPayables = uiState.totalPayables
            )
        }

        item {
            AccountBalancesReconciliationCard(
                cashTotal = uiState.cashSales,
                mpesaTotal = uiState.mpesaSales,
                bankTotal = uiState.bankBalance
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Matumizi ya Biashara (Expenses)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${uiState.recentExpenses.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
        }

        if (uiState.recentExpenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Hakuna Matumizi Yaliyorekodiwa", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        Text("Bofya 'Rekodi Matumizi' kuweka gharama za kodi, umeme, vibaruwa, n.k.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(uiState.recentExpenses, key = { it.id }) { expense ->
                ExpenseRowCard(
                    expense = expense,
                    canDelete = uiState.userRole == Role.OWNER,
                    onDelete = { onDeleteExpense(expense.id) }
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// TAB 2: INVOICES (SALES RECEIVABLES)
// ==========================================
@Composable
private fun InvoicesTab(
    invoices: List<InvoiceEntity>,
    onPayInvoice: (InvoiceEntity) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val totalDue = invoices.filter { it.status != InvoiceStatus.PAID && it.status != InvoiceStatus.CANCELLED }.sumOf { it.balanceDue }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF281E10)),
                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("JUMLA YA ANKARA ZINAZODAIWA", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
                        Text("KSh ${totalDue.toInt()}", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("${invoices.size} Ankara", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (invoices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hakuna Ankara Zilizoundwa", color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("Bofya 'Ankara Mpya' kutengeneza ankara yenye kodi, punguzo, na tarehe ya kulipa.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(invoices, key = { it.id }) { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
                                Text(inv.customerName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                                Text(inv.customerPhone, color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("KSh ${inv.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
                                Surface(
                                    color = when (inv.status) {
                                        InvoiceStatus.PAID -> StatusGreen.copy(alpha = 0.2f)
                                        InvoiceStatus.PARTIALLY_PAID -> AmberAccent.copy(alpha = 0.2f)
                                        InvoiceStatus.OVERDUE -> StatusRed.copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = inv.status.name,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (inv.status) {
                                            InvoiceStatus.PAID -> StatusGreen
                                            InvoiceStatus.PARTIALLY_PAID -> AmberAccent
                                            InvoiceStatus.OVERDUE -> StatusRed
                                            else -> Color.White
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tarehe ya Mwisho: ${sdf.format(Date(inv.dueDate))}", fontSize = 11.sp, color = TextMuted)
                            if (inv.status != InvoiceStatus.PAID && inv.status != InvoiceStatus.CANCELLED) {
                                Button(
                                    onClick = { onPayInvoice(inv) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Lipa Ankara (KSh ${inv.balanceDue.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// TAB 3: BILLS & ACCOUNTS PAYABLE
// ==========================================
@Composable
private fun BillsTab(
    bills: List<BillEntity>,
    onPayBill: (BillEntity) -> Unit
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val totalPayable = bills.filter { it.status != BillStatus.PAID && it.status != BillStatus.CANCELLED }.sumOf { it.balanceDue }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF14243B)),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("MADENI YA WASAMBAZAJI (ACCOUNTS PAYABLE)", style = MaterialTheme.typography.labelSmall, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                        Text("KSh ${totalPayable.toInt()}", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("${bills.size} Bili", color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hakuna Bili za Wasambazaji", color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("Bofya 'Bili ya Mzigo' kuweka ununuzi wa mzigo au vipuri kwa deni.", fontSize = 12.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(bills, key = { it.id }) { bill ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(bill.billNumber, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 14.sp)
                                Text(bill.supplierName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                                Text("Aina: ${bill.category}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("KSh ${bill.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
                                Surface(
                                    color = when (bill.status) {
                                        BillStatus.PAID -> StatusGreen.copy(alpha = 0.2f)
                                        BillStatus.PARTIALLY_PAID -> AmberAccent.copy(alpha = 0.2f)
                                        BillStatus.OVERDUE -> StatusRed.copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = bill.status.name,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (bill.status == BillStatus.PAID) StatusGreen else Color(0xFF60A5FA)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tarehe ya Kulipa: ${sdf.format(Date(bill.dueDate))}", fontSize = 11.sp, color = TextMuted)
                            if (bill.status != BillStatus.PAID && bill.status != BillStatus.CANCELLED) {
                                Button(
                                    onClick = { onPayBill(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Lipa Bili (KSh ${bill.balanceDue.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// TAB 4: GENERAL LEDGER & CHART OF ACCOUNTS
// ==========================================
@Composable
private fun GeneralLedgerTab(
    accounts: List<AccountEntity>,
    journalEntries: List<JournalEntryEntity>
) {
    val sdf = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("CHART OF ACCOUNTS (ORODHA YA AKAUNTI)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
        }

        items(accounts, key = { it.id }) { acc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(acc.code, fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 13.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(acc.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                        }
                        Text("${acc.category.name} • ${acc.subCategory.name}", fontSize = 10.sp, color = TextMuted)
                    }
                    Text("KSh ${String.format("%,.0f", acc.balance)}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                }
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
            Text("DOUBLE-ENTRY JOURNAL ENTRIES (DAFTARI KUU)", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.Bold)
        }

        if (journalEntries.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                    Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hakuna Maingizo ya Daftari Kuu Bado", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(journalEntries, key = { it.id }) { je ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, if (je.isBalanced) CardBorder else StatusRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${je.entryNumber} • ${je.sourceType.name}", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 12.sp)
                            Text(sdf.format(Date(je.date)), color = TextMuted, fontSize = 11.sp)
                        }
                        Text(je.description, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))

                        je.lines.forEach { line ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• ${line.accountCode} ${line.accountName}", color = TextSecondary, fontSize = 11.sp)
                                Text(
                                    text = if (line.debit > 0) "Dr KSh ${line.debit.toInt()}" else "Cr KSh ${line.credit.toInt()}",
                                    color = if (line.debit > 0) StatusGreen else Color(0xFF60A5FA),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// TAB 5: FINANCIAL STATEMENTS & REPORTS
// ==========================================
@Composable
private fun ReportsTab(
    pnl: ProfitAndLossReport?,
    balanceSheet: BalanceSheetReport?,
    trialBalance: TrialBalanceReport?,
    arAgeing: AccountsReceivableAgeingReport?,
    apAgeing: AccountsPayableAgeingReport?,
    onRefresh: () -> Unit
) {
    var reportType by remember { mutableStateOf(0) }
    val reportTitles = listOf("P&L Statement", "Balance Sheet", "Trial Balance", "Ageing Analysis")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("RIPOTI ZA FEDHA (FINANCIAL STATEMENTS)", style = MaterialTheme.typography.labelSmall, color = AmberAccent, fontWeight = FontWeight.Bold)
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = AmberAccent)
            }
        }

        TabRow(selectedTabIndex = reportType, containerColor = Color(0xFF1E2638)) {
            reportTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = reportType == idx,
                    onClick = { reportType = idx },
                    text = { Text(title, fontSize = 11.sp, fontWeight = if (reportType == idx) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (reportType) {
            0 -> ProfitAndLossView(pnl)
            1 -> BalanceSheetView(balanceSheet)
            2 -> TrialBalanceView(trialBalance)
            3 -> AgeingAnalysisView(arAgeing, apAgeing)
        }
    }
}

@Composable
private fun ProfitAndLossView(pnl: ProfitAndLossReport?) {
    if (pnl == null) {
        Text("Inapakia taarifa ya faida na hasara...", color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("MAPATO YA UENDESHAJI (REVENUE)", fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 12.sp)
                    pnl.revenueItems.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 12.sp)
                            Text("KSh ${String.format("%,.0f", it.amount)}", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Jumla ya Mapato:", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("KSh ${String.format("%,.0f", pnl.totalRevenue)}", fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("GHARAMA ZA MOJA KWA MOJA (COGS)", fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 12.sp)
                    pnl.costOfSalesItems.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 12.sp)
                            Text("KSh ${String.format("%,.0f", it.amount)}", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Faida ya Awali (Gross Profit):", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("KSh ${String.format("%,.0f", pnl.grossProfit)}", fontWeight = FontWeight.Bold, color = if (pnl.grossProfit >= 0) StatusGreen else StatusRed)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("MATUMIZI YA UENDESHAJI (OPERATING EXPENSES)", fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 12.sp)
                    pnl.operatingExpenses.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 12.sp)
                            Text("KSh ${String.format("%,.0f", it.amount)}", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FAIDA SAFI (NET PROFIT / LOSS):", fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text("KSh ${String.format("%,.0f", pnl.netProfit)}", fontWeight = FontWeight.ExtraBold, color = if (pnl.netProfit >= 0) StatusGreen else StatusRed, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceSheetView(bs: BalanceSheetReport?) {
    if (bs == null) {
        Text("Inapakia mizania ya biashara...", color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("RASILIMALI (ASSETS)", fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 12.sp)
                    Text("Current Assets: KSh ${String.format("%,.0f", bs.totalCurrentAssets)}", color = TextSecondary, fontSize = 12.sp)
                    Text("Fixed Assets: KSh ${String.format("%,.0f", bs.totalFixedAssets)}", color = TextSecondary, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Jumla ya Rasilimali (Total Assets):", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("KSh ${String.format("%,.0f", bs.totalAssets)}", fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("DHIMA & MTAJI (LIABILITIES & EQUITY)", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 12.sp)
                    Text("Current Liabilities (AP & Loans): KSh ${String.format("%,.0f", bs.totalCurrentLiabilities)}", color = TextSecondary, fontSize = 12.sp)
                    Text("Total Equity + Current Net Profit: KSh ${String.format("%,.0f", bs.totalEquity)}", color = TextSecondary, fontSize = 12.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Jumla ya Dhima na Mtaji:", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("KSh ${String.format("%,.0f", bs.totalLiabilitiesAndEquity)}", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    }
                }
            }
        }

        item {
            Surface(
                color = if (bs.isBalanced) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ulingano wa Mizania (Balance Sheet State):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                    Text(if (bs.isBalanced) "✓ IMEPIMIKA (BALANCED)" else "⚠️ HUKO SAWA", fontWeight = FontWeight.ExtraBold, color = if (bs.isBalanced) StatusGreen else StatusRed)
                }
            }
        }
    }
}

@Composable
private fun TrialBalanceView(tb: TrialBalanceReport?) {
    if (tb == null) {
        Text("Inapakia ulinganifu wa hesabu...", color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Surface(
                color = if (tb.isBalanced) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Debit: KSh ${String.format("%,.0f", tb.totalDebit)}", fontWeight = FontWeight.Bold, color = StatusGreen)
                    Text("Credit: KSh ${String.format("%,.0f", tb.totalCredit)}", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    Text(if (tb.isBalanced) "✓ BALANCED" else "⚠️ UNBALANCED", fontWeight = FontWeight.Bold, color = if (tb.isBalanced) StatusGreen else StatusRed)
                }
            }
        }

        items(tb.items) { item ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.accountCode} ${item.accountName}", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text(if (item.debit > 0) "Dr KSh ${item.debit.toInt()}" else "Cr KSh ${item.credit.toInt()}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun AgeingAnalysisView(ar: AccountsReceivableAgeingReport?, ap: AccountsPayableAgeingReport?) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("UCHANGANUZI WA MADENI YA WATEJA (AR AGEING)", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 12.sp)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Jumla ya Madeni: KSh ${ar?.overall?.total?.toInt() ?: 0}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("0 - 30 Siku: KSh ${ar?.overall?.days1to30?.toInt() ?: 0} • 31 - 60 Siku: KSh ${ar?.overall?.days31to60?.toInt() ?: 0}", color = TextSecondary, fontSize = 11.sp)
                    Text("61 - 90 Siku: KSh ${ar?.overall?.days61to90?.toInt() ?: 0} • 90+ Siku: KSh ${ar?.overall?.days90Plus?.toInt() ?: 0}", color = StatusRed, fontSize = 11.sp)
                }
            }
        }

        item {
            Text("UCHANGANUZI WA MADENI YA WASAMBAZAJI (AP AGEING)", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 12.sp)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Jumla ya Bili: KSh ${ap?.overall?.total?.toInt() ?: 0}", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("0 - 30 Siku: KSh ${ap?.overall?.days1to30?.toInt() ?: 0} • 31 - 60 Siku: KSh ${ap?.overall?.days31to60?.toInt() ?: 0}", color = TextSecondary, fontSize = 11.sp)
                    Text("61 - 90 Siku: KSh ${ap?.overall?.days61to90?.toInt() ?: 0} • 90+ Siku: KSh ${ap?.overall?.days90Plus?.toInt() ?: 0}", color = StatusRed, fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// SHARED WIDGETS & DIALOGS
// ==========================================

@Composable
private fun ProfitSummaryCard(
    totalSales: Double,
    totalExpenses: Double,
    netProfit: Double,
    totalDeni: Double,
    totalPayables: Double
) {
    val isProfit = netProfit >= 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isProfit) Color(0xFF0F2E22) else Color(0xFF3B1515)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "FAIDA SAFI (NET PROFIT)",
                style = MaterialTheme.typography.labelSmall,
                color = if (isProfit) Color(0xFF34D399) else Color(0xFFF87171),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "KSh ${String.format("%,.2f", netProfit)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pesa Zimeingia (Sales)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text(
                        "KSh ${String.format("%,.0f", totalSales)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Gharama (Expenses)", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text(
                        "- KSh ${String.format("%,.0f", totalExpenses)}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171),
                        fontSize = 16.sp
                    )
                }
            }

            if (totalDeni > 0 || totalPayables > 0) {
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (totalDeni > 0) {
                        Surface(
                            color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Deni Nje (AR):", fontSize = 10.sp, color = Color(0xFFFBBF24))
                                Text("KSh ${totalDeni.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 13.sp)
                            }
                        }
                    }
                    if (totalPayables > 0) {
                        Surface(
                            color = Color(0xFF3B82F6).copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Madeni ya Mzigo (AP):", fontSize = 10.sp, color = Color(0xFF60A5FA))
                                Text("KSh ${totalPayables.toInt()}", fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountBalancesReconciliationCard(cashTotal: Double, mpesaTotal: Double, bankTotal: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ugawanyaji wa Pesa (Till / M-Pesa / Benki)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Cash Mkononi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("KSh ${String.format("%,.0f", cashTotal)}", fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6), fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("M-Pesa Till", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("KSh ${String.format("%,.0f", mpesaTotal)}", fontWeight = FontWeight.Bold, color = Color(0xFF00A859), fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Akaunti ya Benki", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("KSh ${String.format("%,.0f", bankTotal)}", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ExpenseRowCard(
    expense: ExpenseEntity,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    val sdf = remember { SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = Color(0xFFEF4444).copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(expense.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(sdf.format(Date(expense.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                Text(expense.title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                if (expense.note.isNotBlank()) {
                    Text(expense.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("- KSh ${String.format("%,.0f", expense.amount)}", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 16.sp)
                Text(expense.paymentMethod.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Futa", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, amount: Double, method: PaymentMethod, note: String) -> Unit
) {
    val categories = listOf("Kodi / Rent", "Mzigo / Stock", "Umeme / Tokeni", "Nauli & Usafiri", "Mishahara / Wages", "Chakula / Lunch", "Vipuri / Spares", "Mengineyo / Other")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rekodi Matumizi (New Expense)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Maelezo (e.g. Tokeni za umeme)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Kiasi cha Pesa (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == PaymentMethod.CASH, onClick = { paymentMethod = PaymentMethod.CASH }, label = { Text("Cash") })
                    FilterChip(selected = paymentMethod == PaymentMethod.MPESA, onClick = { paymentMethod = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                    FilterChip(selected = paymentMethod == PaymentMethod.BANK, onClick = { paymentMethod = PaymentMethod.BANK }, label = { Text("Bank") })
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Risiti / Kumbukumbu (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && title.isNotBlank()) onConfirm(selectedCategory, title, amt, paymentMethod, note)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) { Text("Hifadhi", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
private fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (custName: String, custPhone: String, desc: String, amount: Double, disc: Double, tax: Double, method: PaymentMethod, notes: String) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var discountStr by remember { mutableStateOf("0") }
    var taxStr by remember { mutableStateOf("0") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CREDIT) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tengeneza Ankara Mpya (Invoice)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Jina la Mteja") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = { Text("Simu ya Mteja") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Maelezo ya Mauzo/Ufundi") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Bei ya Jumla (KSh)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = discountStr, onValueChange = { discountStr = it }, label = { Text("Punguzo (Disc)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = taxStr, onValueChange = { taxStr = it }, label = { Text("Kodi (VAT)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = paymentMethod == PaymentMethod.CREDIT, onClick = { paymentMethod = PaymentMethod.CREDIT }, label = { Text("Deni (Credit)") })
                    FilterChip(selected = paymentMethod == PaymentMethod.MPESA, onClick = { paymentMethod = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                    FilterChip(selected = paymentMethod == PaymentMethod.CASH, onClick = { paymentMethod = PaymentMethod.CASH }, label = { Text("Cash") })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    val disc = discountStr.toDoubleOrNull() ?: 0.0
                    val tax = taxStr.toDoubleOrNull() ?: 0.0
                    if (customerName.isNotBlank() && amt > 0) {
                        onConfirm(customerName, customerPhone, description, amt, disc, tax, paymentMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text("Tengeneza Ankara", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
private fun CreateBillDialog(
    suppliers: List<SupplierEntity>,
    onDismiss: () -> Unit,
    onConfirm: (supplierName: String, supplierPhone: String, billNumber: String, amount: Double, category: String, description: String) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var supplierPhone by remember { mutableStateOf("") }
    var billNumber by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mzigo / Stock") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bili ya Msambazaji (Purchase Bill)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = supplierName, onValueChange = { supplierName = it }, label = { Text("Jina la Msambazaji") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = supplierPhone, onValueChange = { supplierPhone = it }, label = { Text("Nambari ya Simu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = billNumber, onValueChange = { billNumber = it }, label = { Text("Nambari ya Bili / Inv #") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Kiasi cha Bili (KSh)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Maelezo ya mzigo uliopokelewa") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (supplierName.isNotBlank() && amt > 0) {
                        onConfirm(supplierName, supplierPhone, billNumber, amt, category, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) { Text("Hifadhi Bili", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
private fun AccountTransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fromCode: String, toCode: String, amount: Double, ref: String, desc: String) -> Unit
) {
    var fromCode by remember { mutableStateOf("1020") }
    var toCode by remember { mutableStateOf("1030") }
    var amountStr by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uhamisho wa Pesa (Inter-Account Transfer)", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Chagua kutoka akaunti gani kwenda gani (Mfano: M-Pesa Till kwenda Benki):", fontSize = 12.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = fromCode == "1020", onClick = { fromCode = "1020" }, label = { Text("Kutoka: M-Pesa") })
                    FilterChip(selected = fromCode == "1010", onClick = { fromCode = "1010" }, label = { Text("Kutoka: Cash") })
                    FilterChip(selected = fromCode == "1030", onClick = { fromCode = "1030" }, label = { Text("Kutoka: Benki") })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = toCode == "1030", onClick = { toCode = "1030" }, label = { Text("Kwenda: Benki") })
                    FilterChip(selected = toCode == "1010", onClick = { toCode = "1010" }, label = { Text("Kwenda: Cash") })
                    FilterChip(selected = toCode == "1020", onClick = { toCode = "1020" }, label = { Text("Kwenda: M-Pesa") })
                }
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Kiasi cha Kuhamisha (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text("Nambari ya Muamala / Ref Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && fromCode != toCode) {
                        onConfirm(fromCode, toCode, amt, reference, "")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
            ) { Text("Tekeleza Uhamisho", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
private fun RecordInvoicePaymentDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, reference: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(invoice.balanceDue.toInt().toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.MPESA) }
    var ref by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lipa Ankara: ${invoice.invoiceNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Mteja: ${invoice.customerName} • Baki: KSh ${invoice.balanceDue.toInt()}", fontWeight = FontWeight.SemiBold, color = AmberAccent)
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Kiasi Kinacholipwa (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == PaymentMethod.MPESA, onClick = { paymentMethod = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                    FilterChip(selected = paymentMethod == PaymentMethod.CASH, onClick = { paymentMethod = PaymentMethod.CASH }, label = { Text("Cash") })
                    FilterChip(selected = paymentMethod == PaymentMethod.BANK, onClick = { paymentMethod = PaymentMethod.BANK }, label = { Text("Bank") })
                }
                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("M-Pesa / Cheque Code") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, paymentMethod, ref)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text("Hifadhi Malipo", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
private fun RecordBillPaymentDialog(
    bill: BillEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: PaymentMethod, reference: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(bill.balanceDue.toInt().toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.BANK) }
    var ref by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Lipa Bili ya Msambazaji", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Msambazaji: ${bill.supplierName} • Baki: KSh ${bill.balanceDue.toInt()}", fontWeight = FontWeight.SemiBold, color = Color(0xFF60A5FA))
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Kiasi Kinacholipwa (KSh)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == PaymentMethod.BANK, onClick = { paymentMethod = PaymentMethod.BANK }, label = { Text("Bank") })
                    FilterChip(selected = paymentMethod == PaymentMethod.MPESA, onClick = { paymentMethod = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                    FilterChip(selected = paymentMethod == PaymentMethod.CASH, onClick = { paymentMethod = PaymentMethod.CASH }, label = { Text("Cash") })
                }
                OutlinedTextField(
                    value = ref,
                    onValueChange = { ref = it },
                    label = { Text("Payment Ref / Bank Slip") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt, paymentMethod, ref)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) { Text("Lipa Sasa", fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Ghairi") } }
    )
}

@Composable
fun DailyCashUpTab(
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    businessName: String
) {
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val todaySales = sales.filter { it.createdAt >= startOfToday }
    val todayExpenses = expenses.filter { it.createdAt >= startOfToday }

    val expectedCashSales = todaySales.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.total }
    val expectedMpesaSales = todaySales.filter { it.paymentMethod == PaymentMethod.MPESA }.sumOf { it.total }
    val cashExpenses = todayExpenses.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.amount }
    val expectedCashInDrawer = (expectedCashSales - cashExpenses).coerceAtLeast(0.0)

    var countedCashStr by remember { mutableStateOf("") }
    var actualMpesaStr by remember { mutableStateOf("") }

    val countedCash = countedCashStr.toDoubleOrNull() ?: 0.0
    val actualMpesa = actualMpesaStr.toDoubleOrNull() ?: 0.0

    val cashDiff = countedCash - expectedCashInDrawer
    val mpesaDiff = actualMpesa - expectedMpesaSales

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "KUFUNGA DAFTARI LA SIKU (DAILY CASH-UP)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Linganisha pesa taslimu kwenye droo na salio la M-Pesa dhidi ya mauzo ya mfumo wa leo.",
                        fontSize = 13.sp,
                        color = Color(0xFF8B949E)
                    )
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F26)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3440)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Hali ya Mauzo ya Leo Kwenye Mfumo", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mauzo ya Pesa Taslimu (Cash Sales):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text("KSh ${String.format("%,.0f", expectedCashSales)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Matumizi ya Taslimu (Cash Outflows):", color = Color(0xFFF87171), fontSize = 13.sp)
                        Text("- KSh ${String.format("%,.0f", cashExpenses)}", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF30363D)))
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pesa Inayotakiwa Drooni (Expected Cash):", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("KSh ${String.format("%,.0f", expectedCashInDrawer)}", color = AmberAccent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mauzo ya M-Pesa Yanayotakiwa:", color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("KSh ${String.format("%,.0f", expectedMpesaSales)}", color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F26)),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2E3440)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Hesabu Pesa Zilizopo Sasa (Physical Count)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = countedCashStr,
                        onValueChange = { countedCashStr = it },
                        label = { Text("Pesa Taslimu Ulizohesabu Drooni (KSh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = actualMpesaStr,
                        onValueChange = { actualMpesaStr = it },
                        label = { Text("Salio Halisi la M-Pesa Till / Paybill (KSh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (countedCashStr.isNotBlank() || actualMpesaStr.isNotBlank()) {
            item {
                val isBalanced = kotlin.math.abs(cashDiff) < 1.0 && (actualMpesaStr.isBlank() || kotlin.math.abs(mpesaDiff) < 1.0)
                val isShortage = cashDiff < -1.0 || mpesaDiff < -1.0

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBalanced) Color(0xFF0F2E22) else if (isShortage) Color(0xFF3B1515) else Color(0xFF362812)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, if (isBalanced) StatusGreen else if (isShortage) StatusRed else AmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (isBalanced) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isBalanced) StatusGreen else if (isShortage) StatusRed else AmberAccent
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (isBalanced) "HESABU ZIMELINGANA KIKAMILIFU ✅" else if (isShortage) "TAHADHARI: KUNA UPUNGUFU (SHORTAGE) ⚠️" else "KUNA ZIADA YA PESA (SURPLUS) 🟢",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Tofauti ya Pesa Taslimu (Cash Diff): ${if (cashDiff >= 0) "+KSh " else "-KSh "}${String.format("%,.0f", kotlin.math.abs(cashDiff))}",
                            color = if (cashDiff >= 0) StatusGreen else StatusRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        if (actualMpesaStr.isNotBlank()) {
                            Text(
                                text = "Tofauti ya M-Pesa: ${if (mpesaDiff >= 0) "+KSh " else "-KSh "}${String.format("%,.0f", kotlin.math.abs(mpesaDiff))}",
                                color = if (mpesaDiff >= 0) StatusGreen else StatusRed,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                                val msg = """
                                    *RIPOTI YA KUFUNGA SIKU - ${businessName.ifBlank { "BUSINESS OS" }}*
                                    Tarehe: $dateStr
                                    
                                    • Mauzo ya Taslimu (Cash): KSh ${expectedCashSales.toInt()}
                                    • Matumizi ya Taslimu: KSh ${cashExpenses.toInt()}
                                    • Pesa Inayotakiwa Drooni: KSh ${expectedCashInDrawer.toInt()}
                                    • Pesa Zilizopo Drooni: KSh ${countedCash.toInt()}
                                    • Tofauti ya Cash: ${if (cashDiff >= 0) "+" else ""}${cashDiff.toInt()}
                                    
                                    • Mauzo ya M-Pesa: KSh ${expectedMpesaSales.toInt()}
                                    • M-Pesa Zilizopo: KSh ${actualMpesa.toInt()}
                                    • Tofauti ya M-Pesa: ${if (mpesaDiff >= 0) "+" else ""}${mpesaDiff.toInt()}
                                    
                                    Hali: ${if (isBalanced) "ZIMELINGANA VEMA ✅" else if (isShortage) "KUNA UPUNGUFU ⚠️" else "KUNA ZIADA 🟢"}
                                """.trimIndent()

                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                        `package` = "com.whatsapp"
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val fallback = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, msg)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(fallback, "Share Daily Close"))
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isBalanced) StatusGreen else AmberAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, tint = TextOnAccent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tuma Ripoti ya Kufunga Siku kwa WhatsApp", color = TextOnAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
