package com.example.businessos.ui.accounting

import com.example.businessos.ui.util.AppLanguageManager
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.DarkSurface
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
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel,
    onSwitchWorkspace: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val pnlReport by viewModel.pnlReport.collectAsState()
    val balanceSheetReport by viewModel.balanceSheetReport.collectAsState()
    val trialBalanceReport by viewModel.trialBalanceReport.collectAsState()
    val arAgeingReport by viewModel.arAgeingReport.collectAsState()
    val apAgeingReport by viewModel.apAgeingReport.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf(com.example.businessos.ui.util.AppLanguageManager.tr("Cashbook","Daftari la pesa"), com.example.businessos.ui.util.AppLanguageManager.tr("Invoices","Ankara"), com.example.businessos.ui.util.AppLanguageManager.tr("Suppliers","Wasambazaji"), com.example.businessos.ui.util.AppLanguageManager.tr("Ledger","Leja"), com.example.businessos.ui.util.AppLanguageManager.tr("Reports","Ripoti"), com.example.businessos.ui.util.AppLanguageManager.tr("Close the day","Funga siku"))

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showCreateInvoiceDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var targetInvoiceForPayment by remember { mutableStateOf<InvoiceEntity?>(null) }

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
            Column(modifier = Modifier.fillMaxWidth().background(DarkBackground)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = AppLanguageManager.tr("Back", "Rudi"), tint = TextPrimary)
                    }
                    BusinessOsTopBar(
                        title = com.example.businessos.ui.util.AppLanguageManager.tr("Accounts","Hesabu"),
                        businessName = uiState.businessName,
                        role = uiState.userRole,
                        onSwitchWorkspace = onSwitchWorkspace,
                        modifier = Modifier.weight(1f)
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
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
                                    fontSize = 14.sp
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
                        text = { Text(AppLanguageManager.tr("Add expense", "Rekodi matumizi"), fontWeight = FontWeight.Bold) }
                    )
                }
                1 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showCreateInvoiceDialog = true },
                        containerColor = AmberAccent,
                        contentColor = TextOnAccent,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(AppLanguageManager.tr("New invoice", "Ankara mpya"), fontWeight = FontWeight.Bold) }
                    )
                }
                2 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showAddSupplierDialog = true },
                        containerColor = Color(0xFF3B82F6),
                        contentColor = Color.White,
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        text = { Text(AppLanguageManager.tr("New supplier", "Msambazaji mpya"), fontWeight = FontWeight.Bold) }
                    )
                }
                3 -> {
                    ExtendedFloatingActionButton(
                        onClick = { showTransferDialog = true },
                        containerColor = TealAccent,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Filled.SwapHoriz, contentDescription = null) },
                        text = { Text(AppLanguageManager.tr("Move money", "Hamisha pesa"), fontWeight = FontWeight.Bold) }
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
                2 -> SuppliersTab(
                    suppliers = suppliers
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
                viewModel.recordExpense(cat, title, amount, method, note)
                showAddExpenseDialog = false
            }
        )
    }

    if (showCreateInvoiceDialog) {
        CreateInvoiceDialog(
            onDismiss = { showCreateInvoiceDialog = false },
            onConfirm = { custId, custName, custPhone, items ->
                viewModel.createInvoice(custId, custName, custPhone, items)
                showCreateInvoiceDialog = false
            }
        )
    }

    if (showAddSupplierDialog) {
        AddSupplierDialog(
            onDismiss = { showAddSupplierDialog = false },
            onConfirm = { name, phone, email ->
                viewModel.createSupplier(name, phone, email)
                showAddSupplierDialog = false
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
}

// ==========================================
// TAB 1: CASHBOOK
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
            CashbookHeroCard(
                netProfit = uiState.netProfit,
                totalSales = uiState.totalSales,
                totalExpenses = uiState.totalExpenses,
                totalDeni = uiState.totalDeniOwed,
                totalPayables = uiState.totalPayables
            )
        }

        item {
            Text(
                "MATUMIZI YA HIVI MAJUZI (RECENT EXPENSES)",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
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
                        Text(AppLanguageManager.tr("No expenses yet", "Hakuna matumizi bado"), color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(AppLanguageManager.tr("Tap the button below to add an expense.", "Bofya kitufe cha chini kuweka gharama."), fontSize = 14.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(uiState.recentExpenses, key = { it.id }) { expense ->
                ExpenseRowItem(expense = expense, onDelete = { onDeleteExpense(expense.id) })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun CashbookHeroCard(
    netProfit: Money,
    totalSales: Money,
    totalExpenses: Money,
    totalDeni: Money,
    totalPayables: Money
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = AppLanguageManager.tr("Net profit", "Faida safi"),
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = netProfit.formatted,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(AppLanguageManager.tr("Money in (sales)", "Pesa zilizoingia (mauzo)"), style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text(
                        totalSales.formatted,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399),
                        fontSize = 16.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(AppLanguageManager.tr("Expenses", "Gharama"), style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                    Text(
                        "- ${totalExpenses.formatted}",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF87171),
                        fontSize = 16.sp
                    )
                }
            }

            if (totalDeni > Money.ZERO) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(AppLanguageManager.tr("Owed to you:", "Deni nje:"), fontSize = 12.sp, color = Color(0xFFFBBF24))
                        Text(totalDeni.formatted, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24), fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRowItem(expense: ExpenseEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title.ifBlank { expense.category }, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text(
                    "${expense.category} • ${expense.paymentMethod.name}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "- ${Money(expense.amountCents).formatted}",
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = AppLanguageManager.tr("Delete", "Futa"), tint = Color(0xFFF87171).copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ==========================================
// TAB 2: INVOICES
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
            val totalReceivables = Money(invoices.filter { it.status != InvoiceStatus.PAID && it.status != InvoiceStatus.VOIDED }.sumOf { it.balanceDueCents })
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E281F)),
                border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(AppLanguageManager.tr("Invoices owed to you", "Madeni ya ankara"), style = MaterialTheme.typography.labelSmall, color = StatusGreen, fontWeight = FontWeight.Bold)
                        Text(totalReceivables.formatted, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                    Text(AppLanguageManager.tr("${invoices.size} invoices", "Ankara ${invoices.size}"), color = StatusGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        Text(AppLanguageManager.tr("No invoices yet", "Hakuna ankara bado"), color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(AppLanguageManager.tr("Tap New invoice to bill a customer.", "Bofya 'Ankara mpya' kumtolea mteja ankara."), fontSize = 14.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(invoices, key = { it.id }) { invoice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
                                Text(invoice.customerName, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(Money(invoice.totalAmountCents).formatted, fontWeight = FontWeight.ExtraBold, color = TextPrimary, fontSize = 16.sp)
                                Surface(
                                    color = when (invoice.status) {
                                        InvoiceStatus.PAID -> StatusGreen.copy(alpha = 0.2f)
                                        InvoiceStatus.PARTIALLY_PAID -> AmberAccent.copy(alpha = 0.2f)
                                        InvoiceStatus.OVERDUE -> StatusRed.copy(alpha = 0.2f)
                                        else -> Color.Gray.copy(alpha = 0.2f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = invoice.status.name,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (invoice.status == InvoiceStatus.PAID) StatusGreen else AmberAccent
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.08f)))
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(AppLanguageManager.tr("Date: ${sdf.format(Date(invoice.issueDate))}", "Tarehe: ${sdf.format(Date(invoice.issueDate))}"), fontSize = 12.sp, color = TextMuted)
                            if (invoice.status != InvoiceStatus.PAID && invoice.status != InvoiceStatus.VOIDED) {
                                Button(
                                    onClick = { onPayInvoice(invoice) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(AppLanguageManager.tr("Take payment (${Money(invoice.balanceDueCents).formatted})", "Pokea malipo (${Money(invoice.balanceDueCents).formatted})"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
// TAB 3: SUPPLIERS
// ==========================================
@Composable
private fun SuppliersTab(
    suppliers: List<SupplierEntity>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
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
                        Text(AppLanguageManager.tr("Suppliers", "Wasambazaji"), style = MaterialTheme.typography.labelSmall, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                        Text(AppLanguageManager.tr("${suppliers.size} suppliers", "Wasambazaji ${suppliers.size}"), style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        if (suppliers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(AppLanguageManager.tr("No suppliers yet", "Hakuna wasambazaji bado"), color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(AppLanguageManager.tr("Tap New supplier to add one.", "Bofya 'Msambazaji mpya' kuongeza."), fontSize = 14.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(suppliers, key = { it.id }) { supplier ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(supplier.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(supplier.phone, color = TextSecondary, fontSize = 14.sp)
                            if (supplier.email.isNotBlank()) {
                                Text(supplier.email, color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        if (supplier.balanceCents > 0L) {
                            Text(
                                "Deni: ${Money(supplier.balanceCents).formatted}",
                                color = Color(0xFFF87171),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// TAB 4: GENERAL LEDGER
// ==========================================
@Composable
private fun GeneralLedgerTab(
    accounts: List<AccountEntity>,
    journalEntries: List<JournalEntryEntity>
) {
    var viewSubTab by remember { mutableStateOf(0) }
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TabRow(selectedTabIndex = viewSubTab, containerColor = DarkSurface) {
            Tab(selected = viewSubTab == 0, onClick = { viewSubTab = 0 }, text = { Text(AppLanguageManager.tr("Accounts", AppLanguageManager.tr("Account", "Akaunti")), fontSize = 14.sp) })
            Tab(selected = viewSubTab == 1, onClick = { viewSubTab = 1 }, text = { Text(AppLanguageManager.tr("Entries", "Miamala"), fontSize = 14.sp) })
        }

        Spacer(Modifier.height(12.dp))

        if (viewSubTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts, key = { it.id }) { acc ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${acc.code} • ${acc.name}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                                Text("${acc.category.name} • ${acc.subCategory.name}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Text(Money(acc.balanceCents).formatted, fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(journalEntries, key = { it.id }) { entry ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(entry.entryNumber, fontWeight = FontWeight.Bold, color = TealAccent, fontSize = 14.sp)
                                Text(sdf.format(Date(entry.date)), color = TextMuted, fontSize = 12.sp)
                            }
                            Text(entry.description, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(AppLanguageManager.tr("Type: ${entry.sourceType.name}", "Aina: ${entry.sourceType.name}"), color = TextSecondary, fontSize = 12.sp)
                                Text(AppLanguageManager.tr("Total: ${Money(entry.totalDebitCents).formatted}", "Jumla: ${Money(entry.totalDebitCents).formatted}"), fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 5: FINANCIAL REPORTS
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
    var advanced by remember { mutableStateOf(false) }
    val reportTitles = if (advanced) listOf(com.example.businessos.ui.util.AppLanguageManager.tr("Profit and loss","Faida na hasara"), com.example.businessos.ui.util.AppLanguageManager.tr("Balance sheet","Mizania"), com.example.businessos.ui.util.AppLanguageManager.tr("Trial balance","Mizani ya majaribio"), com.example.businessos.ui.util.AppLanguageManager.tr("Who owes","Madeni"))
    else listOf(com.example.businessos.ui.util.AppLanguageManager.tr("Profit and loss","Faida na hasara"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.FilterChip(
                selected = advanced,
                onClick = { advanced = !advanced; reportType = 0 },
                label = { Text(com.example.businessos.ui.util.AppLanguageManager.tr("Advanced","Zaidi")) }
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = AppLanguageManager.tr("Refresh", "Onyesha upya"), tint = AmberAccent)
            }
        }

        if (advanced) TabRow(selectedTabIndex = reportType, containerColor = DarkSurface) {
            reportTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = reportType == idx,
                    onClick = { reportType = idx },
                    text = { Text(title, fontSize = 12.sp, fontWeight = if (reportType == idx) FontWeight.Bold else FontWeight.Normal) }
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
        Text(AppLanguageManager.tr("Loading profit and loss...", "Inapakia faida na hasara..."), color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(AppLanguageManager.tr("Income", "Mapato"), fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 14.sp)
                    pnl.revenueItems.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 14.sp)
                            Text(it.amount.formatted, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Total income:", "Jumla ya mapato:"), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(pnl.totalRevenue.formatted, fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(AppLanguageManager.tr("Cost of goods sold", "Gharama za bidhaa zilizouzwa"), fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 14.sp)
                    pnl.costOfSalesItems.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 14.sp)
                            Text(it.amount.formatted, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Gross profit:", "Faida ya awali:"), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(pnl.grossProfit.formatted, fontWeight = FontWeight.Bold, color = if (pnl.grossProfit >= Money.ZERO) StatusGreen else StatusRed)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(AppLanguageManager.tr("Running costs", "Gharama za uendeshaji"), fontWeight = FontWeight.Bold, color = Color(0xFFF87171), fontSize = 14.sp)
                    pnl.operatingExpenses.forEach {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(it.accountName, color = TextSecondary, fontSize = 14.sp)
                            Text(it.amount.formatted, color = TextPrimary, fontSize = 14.sp)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Net profit or loss:", "Faida au hasara safi:"), fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(pnl.netProfit.formatted, fontWeight = FontWeight.ExtraBold, color = if (pnl.netProfit >= Money.ZERO) StatusGreen else StatusRed, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceSheetView(bs: BalanceSheetReport?) {
    if (bs == null) {
        Text(AppLanguageManager.tr("Loading balance sheet...", "Inapakia mizania..."), color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(AppLanguageManager.tr("What you own", "Rasilimali"), fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 14.sp)
                    Text(AppLanguageManager.tr("Current assets: ${bs.totalCurrentAssets.formatted}", "Rasilimali za sasa: ${bs.totalCurrentAssets.formatted}"), color = TextSecondary, fontSize = 14.sp)
                    Text(AppLanguageManager.tr("Fixed assets: ${bs.totalFixedAssets.formatted}", "Rasilimali za kudumu: ${bs.totalFixedAssets.formatted}"), color = TextSecondary, fontSize = 14.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Total assets:", "Jumla ya rasilimali:"), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(bs.totalAssets.formatted, fontWeight = FontWeight.Bold, color = StatusGreen)
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(AppLanguageManager.tr("What you owe and your capital", "Madeni na mtaji"), fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 14.sp)
                    Text(AppLanguageManager.tr("Current debts: ${bs.totalCurrentLiabilities.formatted}", "Madeni ya sasa: ${bs.totalCurrentLiabilities.formatted}"), color = TextSecondary, fontSize = 14.sp)
                    Text(AppLanguageManager.tr("Capital and profit: ${bs.totalEquity.formatted}", "Mtaji na faida: ${bs.totalEquity.formatted}"), color = TextSecondary, fontSize = 14.sp)
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)).padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Total debts and capital:", "Jumla ya madeni na mtaji:"), fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(bs.totalLiabilitiesAndEquity.formatted, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
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
                    Text(AppLanguageManager.tr("Balance check:", "Ukaguzi wa mizania:"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(if (bs.isBalanced) AppLanguageManager.tr("Balanced", "Imelingana") else AppLanguageManager.tr("Does not balance", "Haijalingana"), fontWeight = FontWeight.ExtraBold, color = if (bs.isBalanced) StatusGreen else StatusRed)
                }
            }
        }
    }
}

@Composable
private fun TrialBalanceView(tb: TrialBalanceReport?) {
    if (tb == null) {
        Text(AppLanguageManager.tr("Loading trial balance...", "Inapakia mizani ya majaribio..."), color = TextSecondary)
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(AppLanguageManager.tr("Account", "Akaunti"), fontWeight = FontWeight.Bold, color = TextSecondary, fontSize = 14.sp)
                Row {
                    Text(AppLanguageManager.tr("Debit", "Debit"), fontWeight = FontWeight.Bold, color = StatusGreen, fontSize = 14.sp)
                    Spacer(Modifier.width(30.dp))
                    Text(AppLanguageManager.tr("Credit", "Credit"), fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA), fontSize = 14.sp)
                }
            }
        }

        items(tb.items) { item ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.accountCode} ${item.accountName}", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    Row {
                        Text(if (item.debit > Money.ZERO) item.debit.formatted else "-", color = TextSecondary, fontSize = 14.sp)
                        Spacer(Modifier.width(16.dp))
                        Text(if (item.credit > Money.ZERO) item.credit.formatted else "-", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Surface(
                color = if (tb.isBalanced) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(AppLanguageManager.tr("Totals (debit and credit):", "Jumla (debit na credit):"), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("${tb.totalDebit.formatted} / ${tb.totalCredit.formatted}", fontWeight = FontWeight.Bold, color = if (tb.isBalanced) StatusGreen else StatusRed)
                }
            }
        }
    }
}

@Composable
private fun AgeingAnalysisView(
    ar: AccountsReceivableAgeingReport?,
    ap: AccountsPayableAgeingReport?
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text(AppLanguageManager.tr("Customers who owe", "Wateja wanaodaiwa"), fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 14.sp)
            if (ar == null || ar.customers.isEmpty()) {
                Text(AppLanguageManager.tr("Nobody owes you now.", "Hakuna anayekudai sasa."), color = TextMuted, fontSize = 14.sp)
            } else {
                ar.customers.forEach { item ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = CardBackground)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.entityName} (${item.phone})", color = TextPrimary, fontSize = 14.sp)
                            Text(item.bucket.total.formatted, color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 6: DAILY CASH-UP
// ==========================================
@Composable
private fun DailyCashUpTab(
    sales: List<SaleEntity>,
    expenses: List<ExpenseEntity>,
    businessName: String
) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val todaySales = sales.filter { it.createdAt >= startOfToday }
    val todayExpenses = expenses.filter { it.createdAt >= startOfToday }

    val expectedCashSales = Money(todaySales.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.totalCents })
    val expectedMpesaSales = Money(todaySales.filter { it.paymentMethod == PaymentMethod.MPESA }.sumOf { it.totalCents })
    val cashExpenses = Money(todayExpenses.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.amountCents })
    val expectedCashInDrawer = (expectedCashSales - cashExpenses).coerceAtLeast(Money.ZERO)

    var countedCashStr by remember { mutableStateOf("") }
    var actualMpesaStr by remember { mutableStateOf("") }

    val countedCash = Money.parseOrZero(countedCashStr)
    val actualMpesa = Money.parseOrZero(actualMpesaStr)

    val cashDiff = countedCash - expectedCashInDrawer
    val mpesaDiff = actualMpesa - expectedMpesaSales

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = AppLanguageManager.tr("Close the day", "Funga siku"),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = AppLanguageManager.tr("Compare the cash in your drawer and your M-Pesa balance with today's sales.", "Linganisha pesa taslimu kwenye droo na salio la M-Pesa na mauzo ya leo."),
                        fontSize = 14.sp,
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
                    Text(AppLanguageManager.tr("1. Today in the app", "1. Hali ya leo kwenye programu"), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Cash sales:", "Mauzo ya taslimu:"), color = Color(0xFF8B949E), fontSize = 14.sp)
                        Text(expectedCashSales.formatted, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Cash spent:", "Matumizi ya taslimu:"), color = Color(0xFFF87171), fontSize = 14.sp)
                        Text("- ${cashExpenses.formatted}", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF30363D)))
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("Cash that should be in the drawer:", "Pesa inayotakiwa drooni:"), color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(expectedCashInDrawer.formatted, color = AmberAccent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(AppLanguageManager.tr("M-Pesa sales expected:", "Mauzo ya M-Pesa yanayotarajiwa:"), color = TealAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(expectedMpesaSales.formatted, color = TealAccent, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
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
                    Text(AppLanguageManager.tr("2. Count what you have now", "2. Hesabu ulichonacho sasa"), fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = countedCashStr,
                        onValueChange = { countedCashStr = it },
                        label = { Text(AppLanguageManager.tr("Cash you counted (KSh)", "Pesa taslimu ulizohesabu (KSh)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = actualMpesaStr,
                        onValueChange = { actualMpesaStr = it },
                        label = { Text(AppLanguageManager.tr("Real M-Pesa till balance (KSh)", "Salio halisi la M-Pesa till (KSh)")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        if (countedCashStr.isNotBlank() || actualMpesaStr.isNotBlank()) {
            item {
                val isBalanced = cashDiff == Money.ZERO && (actualMpesaStr.isBlank() || mpesaDiff == Money.ZERO)
                val isShortage = cashDiff < Money.ZERO || mpesaDiff < Money.ZERO

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
                                text = if (isBalanced) AppLanguageManager.tr("Everything matches", "Hesabu zimelingana kikamilifu") else if (isShortage) AppLanguageManager.tr("Warning: money is short", "Tahadhari: kuna upungufu") else AppLanguageManager.tr("There is extra money", "Kuna ziada ya pesa"),
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

// ==========================================
// DIALOGS
// ==========================================

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, title: String, amount: Money, method: PaymentMethod, note: String) -> Unit
) {
    var category by remember { mutableStateOf("Rent") }
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var note by remember { mutableStateOf("") }

    val categories = listOf("Rent", "Utilities", "Salaries", "Transport", "Meals", "General")

    FullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(AppLanguageManager.tr("Add expense", "Rekodi matumizi"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(AppLanguageManager.tr("What was it for", "Matumizi ya nini")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text(AppLanguageManager.tr("Amount (KSh)", "Kiasi (KSh)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                Text(AppLanguageManager.tr("Type of expense:", "Aina ya gharama:"), fontSize = 14.sp, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat, fontSize = 12.sp) })
                    }
                }

                Text(AppLanguageManager.tr("Paid with:", "Njia ya malipo:"), fontSize = 14.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = method == PaymentMethod.CASH, onClick = { method = PaymentMethod.CASH }, label = { Text(AppLanguageManager.tr("Cash", "Taslimu")) })
                    FilterChip(selected = method == PaymentMethod.MPESA, onClick = { method = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val money = Money.parseOrZero(amountStr)
                    if (title.isNotBlank() && money > Money.ZERO) {
                        onConfirm(category, title, money, method, note)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) { Text(AppLanguageManager.tr("Save", "Hifadhi"), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } }
    )
}

@Composable
private fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (customerId: String?, customerName: String, customerPhone: String, items: List<InvoiceLineItem>) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(AppLanguageManager.tr("New invoice", "Ankara mpya"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text(AppLanguageManager.tr("Customer name", "Jina la mteja")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = customerPhone, onValueChange = { customerPhone = it }, label = { Text(AppLanguageManager.tr("Phone number", "Nambari ya simu")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(AppLanguageManager.tr("What is it for", "Huduma au bidhaa")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text(AppLanguageManager.tr("Invoice amount (KSh)", "Kiasi cha ankara (KSh)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val money = Money.parseOrZero(amountStr)
                    if (customerName.isNotBlank() && money > Money.ZERO) {
                        val item = InvoiceLineItem(description = description.ifBlank { "Service Invoice" }, unitPrice = money)
                        onConfirm(null, customerName, customerPhone, listOf(item))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text(AppLanguageManager.tr("Create invoice", "Toa ankara"), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } }
    )
}

@Composable
private fun AddSupplierDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(AppLanguageManager.tr("New supplier", "Msambazaji mpya"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(AppLanguageManager.tr("Supplier name", "Jina la msambazaji")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(AppLanguageManager.tr("Phone number", "Nambari ya simu")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text(AppLanguageManager.tr("Email", "Barua pepe")) }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, phone, email)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) { Text(AppLanguageManager.tr("Save", "Hifadhi"), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } }
    )
}

@Composable
private fun AccountTransferDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (fromCode: String, toCode: String, amount: Money, ref: String, desc: String) -> Unit
) {
    var fromCode by remember { mutableStateOf("1020") }
    var toCode by remember { mutableStateOf("1030") }
    var amountStr by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(AppLanguageManager.tr("Move money between accounts", "Hamisha pesa kati ya akaunti"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(AppLanguageManager.tr("Choose accounts:", "Chagua akaunti:"), fontSize = 14.sp, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = fromCode == "1020", onClick = { fromCode = "1020" }, label = { Text(AppLanguageManager.tr("From: M-Pesa", "Kutoka: M-Pesa")) })
                    FilterChip(selected = fromCode == "1010", onClick = { fromCode = "1010" }, label = { Text(AppLanguageManager.tr("From: Cash", "Kutoka: Taslimu")) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = toCode == "1030", onClick = { toCode = "1030" }, label = { Text(AppLanguageManager.tr("To: Bank", "Kwenda: Benki")) })
                    FilterChip(selected = toCode == "1010", onClick = { toCode = "1010" }, label = { Text(AppLanguageManager.tr("To: Cash", "Kwenda: Taslimu")) })
                }
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(AppLanguageManager.tr("Amount to move (KSh)", "Kiasi cha kuhamisha (KSh)")) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reference,
                    onValueChange = { reference = it },
                    label = { Text(AppLanguageManager.tr("Reference", "Namba ya muamala")) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val money = Money.parseOrZero(amountStr)
                    if (money > Money.ZERO && fromCode != toCode) {
                        onConfirm(fromCode, toCode, money, reference, "")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
            ) { Text(AppLanguageManager.tr("Move the money", "Hamisha pesa"), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } }
    )
}

@Composable
private fun RecordInvoicePaymentDialog(
    invoice: InvoiceEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Money, method: PaymentMethod, reference: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(Money(invoice.balanceDueCents).toFormattedDouble()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.MPESA) }
    var reference by remember { mutableStateOf("") }

    FullScreenForm(
        onDismissRequest = onDismiss,
        title = { Text(AppLanguageManager.tr("Take payment for invoice ${invoice.invoiceNumber}", "Pokea malipo ya ankara ${invoice.invoiceNumber}"), fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(AppLanguageManager.tr("Customer: ${invoice.customerName} - Left: ${Money(invoice.balanceDueCents).formatted}", "Mteja: ${invoice.customerName} - Baki: ${Money(invoice.balanceDueCents).formatted}"), color = AmberAccent, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text(AppLanguageManager.tr("Amount paid (KSh)", "Kiasi kilicholipwa (KSh)")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reference, onValueChange = { reference = it }, label = { Text(AppLanguageManager.tr("M-Pesa code or cheque number", "Msimbo wa M-Pesa au namba ya hundi")) }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = paymentMethod == PaymentMethod.MPESA, onClick = { paymentMethod = PaymentMethod.MPESA }, label = { Text("M-Pesa") })
                    FilterChip(selected = paymentMethod == PaymentMethod.CASH, onClick = { paymentMethod = PaymentMethod.CASH }, label = { Text(AppLanguageManager.tr("Cash", "Taslimu")) })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val money = Money.parseOrZero(amountStr)
                    if (money > Money.ZERO) {
                        onConfirm(money, paymentMethod, reference)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text(AppLanguageManager.tr("Save payment", "Hifadhi malipo"), fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } }
    )
}
