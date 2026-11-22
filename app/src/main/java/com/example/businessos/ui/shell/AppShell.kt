package com.example.businessos.ui.shell

import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.components.FullScreenForm
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.model.InvoiceStatus
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.Permission
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.RolePermissions
import com.example.businessos.ui.components.BackupRestoreHelper
import com.example.businessos.ui.home.HomeViewModel
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.theme.ThemeManager
import com.example.businessos.ui.util.AppLanguageManager
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class ShellTab { HOME, DATA_ENTRY, PENDINGS, MORE }

private data class Entry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private const val PREFS = "app_settings"
private const val KEY_MORE_GRID = "more_grid_view"

/**
 * The app frame: a side drawer opened from the three-line button, and four bottom tabs
 * (Home, Data entry, Pending, More). In landscape a fifth "Go to" button is added to the
 * bottom bar that opens the same drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    viewModel: HomeViewModel,
    onSwitchCompany: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToJobOrders: () -> Unit,
    onNavigateToDeni: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onNavigateToStockAudit: () -> Unit,
    onNavigateToLayaway: () -> Unit,
    onNavigateToVehicleHistory: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToAuditLog: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToSecurity: () -> Unit
) {
    val context = LocalContext.current
    var backupMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var pendingRestore by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.net.Uri?>(null) }
    val restorePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) pendingRestore = uri }
    pendingRestore?.let { uri ->
        FullScreenForm(
            onDismissRequest = { pendingRestore = null },
            title = { Text(AppLanguageManager.tr("Restore a backup", "Rudisha nakala"), fontWeight = FontWeight.Bold) },
            text = {
                Text(AppLanguageManager.tr(
                    "This replaces all data on this phone with the backup. The file is checked first, and your current data is kept if anything goes wrong. The app will restart.",
                    "Hii inabadilisha data yote kwenye simu hii na nakala. Faili hukaguliwa kwanza, na data yako ya sasa hubaki ikiwa kuna tatizo. Programu itaanza upya."
                ))
            },
            dismissButton = { TextButton(onClick = { pendingRestore = null }) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestore = null
                    when (val r = BackupRestoreHelper.restoreDatabaseFromUri(context, uri)) {
                        is BackupRestoreHelper.RestoreResult.Success -> {
                            (context.applicationContext as com.example.businessos.BusinessOsApplication).authRepository.logout()
                            BackupRestoreHelper.restartApp(context)
                        }
                        is BackupRestoreHelper.RestoreResult.Failure -> backupMessage = r.reason
                    }
                }) { Text(AppLanguageManager.tr("Restore", "Rudisha")) }
            }
        )
    }
    backupMessage?.let { msg ->
        FullScreenForm(
            onDismissRequest = { backupMessage = null },
            title = { Text(AppLanguageManager.tr("Backup", "Nakala"), fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { backupMessage = null }) { Text("OK") } }
        )
    }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableStateOf(ShellTab.HOME) }
    var salesJobsOpen by rememberSaveable { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val session by viewModel.session.collectAsState()
    val business by viewModel.currentBusiness.collectAsState()
    val jobCards by viewModel.jobCards.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val owingCustomers by viewModel.deniCustomers.collectAsState()
    val items by viewModel.items.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val message by viewModel.message.collectAsState()

    val role = session?.activeRole ?: Role.VIEWER
    val shopOn = business?.isShopEnabled ?: session?.isShopEnabled ?: true
    val jobsOn = business?.isGarageEnabled ?: session?.isGarageEnabled ?: false
    val companyName = business?.name ?: session?.activeBusinessName ?: ""
    var showDelete by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var deleteTyped by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var deleteError by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val deleteScope = androidx.compose.runtime.rememberCoroutineScope()
    if (showDelete) {
        FullScreenForm(
            onDismissRequest = { showDelete = false; deleteTyped = ""; deleteError = null },
            title = { Text(AppLanguageManager.tr("Delete company", "Futa kampuni"), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(AppLanguageManager.tr(
                        "This permanently deletes $companyName and all its sales, stock, customers and accounts. Make a backup first. Type the company name to confirm.",
                        "Hii inafuta $companyName na mauzo yote, akiba, wateja na hesabu zake milele. Fanya nakala kwanza. Andika jina la kampuni kuthibitisha."
                    ))
                    androidx.compose.material3.OutlinedTextField(
                        value = deleteTyped,
                        onValueChange = { deleteTyped = it },
                        label = { Text(AppLanguageManager.tr("Company name", "Jina la kampuni")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                    deleteError?.let { Text(it, color = StatusRed, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false; deleteTyped = ""; deleteError = null }) { Text(AppLanguageManager.tr("Cancel", "Ghairi")) } },
            confirmButton = {
                TextButton(
                    enabled = deleteTyped.trim() == companyName && companyName.isNotEmpty(),
                    onClick = {
                        val id = session?.activeBusinessId ?: return@TextButton
                        val app = context.applicationContext as com.example.businessos.BusinessOsApplication
                        deleteScope.launch {
                            app.authRepository.deleteBusiness(id, deleteTyped)
                                .onSuccess { showDelete = false; deleteTyped = ""; onSwitchCompany() }
                                .onFailure { deleteError = it.message }
                        }
                    }
                ) { Text(AppLanguageManager.tr("Delete", "Futa"), color = StatusRed) }
            }
        )
    }
    fun can(permission: Permission) = RolePermissions.has(role, permission)

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    /** Closes the drawer, then runs the action. */
    fun go(action: () -> Unit) {
        scope.launch { drawerState.close() }
        action()
    }

    // ---- numbers for Home ----
    val dayStart = startOfToday()
    val salesToday = Money(sales.filter { !it.isVoided && it.createdAt >= dayStart }.sumOf { it.totalCents })
    val expensesToday = Money(expenses.filter { !it.isVoided && it.createdAt >= dayStart }.sumOf { it.amountCents })
    val owedTotal = Money(owingCustomers.filter { it.balanceCents > 0 }.sumOf { it.balanceCents })

    // ---- pending list ----
    val pendings = buildList {
        val owing = owingCustomers.filter { it.balanceCents > 0 }
        if (owing.isNotEmpty() && can(Permission.TAKE_PAYMENT)) {
            add(
                Entry(
                    AppLanguageManager.tr("Customers who owe you", "Wateja wanaodaiwa"),
                    "${owing.size} - ${Money(owing.sumOf { it.balanceCents }).format()}",
                    Icons.Default.People, onNavigateToDeni
                )
            )
        }
        if (jobsOn) {
            val open = jobCards.filter { !it.isVoided && it.stage != JobStage.INVOICED && it.stage != JobStage.CANCELLED }
            if (open.isNotEmpty()) {
                val waiting = open.count { it.stage == JobStage.AWAITING_APPROVAL }
                val detail = if (waiting > 0) {
                    AppLanguageManager.tr("${open.size} open, $waiting waiting for approval", "${open.size} wazi, $waiting zinasubiri idhini")
                } else {
                    AppLanguageManager.tr("${open.size} open", "${open.size} wazi")
                }
                add(Entry(AppLanguageManager.tr("Job orders in progress", "Kazi zinazoendelea"), detail, Icons.Default.Build, onNavigateToJobOrders))
            }
        }
        if (shopOn && can(Permission.STOCK_TAKE_ADJUST)) {
            val low = items.filter { it.isActive && it.stockQty <= it.lowStockThreshold }
            if (low.isNotEmpty()) {
                add(
                    Entry(
                        AppLanguageManager.tr("Running low on stock", "Akiba inaisha"),
                        AppLanguageManager.tr("${low.size} items", "bidhaa ${low.size}"),
                        Icons.Default.Inventory2, onNavigateToStockAudit
                    )
                )
            }
        }
        if (can(Permission.VIEW_FINANCIAL_REPORTS)) {
            val unpaid = invoices.filter {
                it.balanceDueCents > 0 &&
                    (it.status == InvoiceStatus.UNPAID || it.status == InvoiceStatus.PARTIALLY_PAID || it.status == InvoiceStatus.OVERDUE)
            }
            if (unpaid.isNotEmpty()) {
                add(
                    Entry(
                        AppLanguageManager.tr("Unpaid invoices", "Ankara zisizolipwa"),
                        "${unpaid.size} - ${Money(unpaid.sumOf { it.balanceDueCents }).format()}",
                        Icons.Default.Receipt, onNavigateToAccounting
                    )
                )
            }
        }
    }

    // ---- data entry list ----
    val dataEntries = buildList {
        if (shopOn && can(Permission.MAKE_SALE)) {
            add(Entry(AppLanguageManager.tr("New sale", "Mauzo mapya"), AppLanguageManager.tr("Sell items", "Uza bidhaa"), Icons.Default.AddShoppingCart, onNavigateToPos))
        }
        if (jobsOn && can(Permission.INTAKE_JOB)) {
            add(Entry(AppLanguageManager.tr("New job order", "Kazi mpya"), AppLanguageManager.tr("For a customer or to make stock", "Ya mteja au ya kutengeneza akiba"), Icons.Default.Build, onNavigateToJobOrders))
        }
        if (can(Permission.CREATE_CUSTOMER) || can(Permission.TAKE_PAYMENT)) {
            add(Entry(AppLanguageManager.tr("Customers and payments", "Wateja na malipo"), AppLanguageManager.tr("Add a customer, record a payment", "Ongeza mteja, rekodi malipo"), Icons.Default.People, onNavigateToDeni))
        }
        if (shopOn && can(Permission.STOCK_TAKE_ADJUST)) {
            add(Entry(AppLanguageManager.tr("Stock", "Akiba"), AppLanguageManager.tr("Count and correct stock", "Hesabu na rekebisha akiba"), Icons.Default.Inventory2, onNavigateToStockAudit))
        }
        if (can(Permission.RECORD_EXPENSE)) {
            add(Entry(AppLanguageManager.tr("Purchases and expenses", "Manunuzi na gharama"), AppLanguageManager.tr("Suppliers, bills, expenses", "Wasambazaji, bili, gharama"), Icons.Default.ShoppingCart, onNavigateToAccounting))
        }
        if (can(Permission.VIEW_FINANCIAL_REPORTS)) {
            add(Entry(AppLanguageManager.tr("Invoices and receipts", "Ankara na risiti"), AppLanguageManager.tr("Invoices, cashbook", "Ankara, daftari la pesa"), Icons.Default.Receipt, onNavigateToAccounting))
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = DarkSurface) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                        Text(
                            text = companyName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    HorizontalDivider(color = CardBorder)
                    Spacer(Modifier.height(8.dp))

                    DrawerItem(Icons.Default.Business, AppLanguageManager.tr("Companies", "Kampuni")) { go(onSwitchCompany) }

                    if ((shopOn && can(Permission.MAKE_SALE)) || (jobsOn && can(Permission.INTAKE_JOB))) {
                        DrawerItem(
                            icon = Icons.Default.Storefront,
                            label = AppLanguageManager.tr("Sales / Job orders", "Mauzo / Kazi"),
                            trailing = if (salesJobsOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore
                        ) { salesJobsOpen = !salesJobsOpen }
                        if (salesJobsOpen) {
                            if (shopOn && can(Permission.MAKE_SALE)) {
                                DrawerItem(Icons.Default.AddShoppingCart, AppLanguageManager.tr("Sales", "Mauzo"), indent = true) { go(onNavigateToPos) }
                            }
                            if (jobsOn && can(Permission.INTAKE_JOB)) {
                                DrawerItem(Icons.Default.Build, AppLanguageManager.tr("Job orders", "Kazi"), indent = true) { go(onNavigateToJobOrders) }
                            }
                        }
                    }

                    if (can(Permission.RECORD_EXPENSE)) {
                        DrawerItem(Icons.Default.ShoppingCart, AppLanguageManager.tr("Purchases", "Manunuzi")) { go(onNavigateToAccounting) }
                    }
                    if (can(Permission.MANAGE_STAFF)) {
                        DrawerItem(Icons.Default.People, AppLanguageManager.tr("Users", "Watumiaji")) { go(onNavigateToStaff) }
                    }
                    DrawerItem(Icons.Default.Settings, AppLanguageManager.tr("Settings", "Mipangilio")) {
                        go { tab = ShellTab.MORE }
                    }

                    HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (ThemeManager.isDarkMode.value) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = AmberAccent
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            AppLanguageManager.tr("Dark mode", "Hali ya giza"),
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = ThemeManager.isDarkMode.value,
                            onCheckedChange = { ThemeManager.setDarkMode(context, it) }
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            containerColor = DarkBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (tab) {
                                ShellTab.HOME -> AppLanguageManager.tr("Home", "Mwanzo")
                                ShellTab.DATA_ENTRY -> AppLanguageManager.tr("Data entry", "Ingiza data")
                                ShellTab.PENDINGS -> AppLanguageManager.tr("Pending", "Yanayosubiri")
                                ShellTab.MORE -> AppLanguageManager.tr("More", "Zaidi")
                            },
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = AppLanguageManager.tr("Menu", "Menyu"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary,
                        navigationIconContentColor = TextPrimary,
                        actionIconContentColor = TextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(containerColor = DarkSurface) {
                    ShellNavItem(tab == ShellTab.HOME, Icons.Default.Home, AppLanguageManager.tr("Home", "Mwanzo")) { tab = ShellTab.HOME }
                    ShellNavItem(tab == ShellTab.DATA_ENTRY, Icons.Default.EditNote, AppLanguageManager.tr("Data entry", "Ingiza")) { tab = ShellTab.DATA_ENTRY }
                    ShellNavItem(tab == ShellTab.PENDINGS, Icons.Default.PendingActions, AppLanguageManager.tr("Pending", "Yanasubiri")) { tab = ShellTab.PENDINGS }
                    ShellNavItem(tab == ShellTab.MORE, Icons.Default.MoreHoriz, AppLanguageManager.tr("More", "Zaidi")) { tab = ShellTab.MORE }
                    if (isLandscape) {
                        ShellNavItem(false, Icons.Default.ArrowForward, AppLanguageManager.tr("Go to", "Nenda")) {
                            scope.launch { drawerState.open() }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    ShellTab.HOME -> HomeContent(
                        isLandscape = isLandscape,
                        salesToday = salesToday,
                        expensesToday = expensesToday,
                        owed = owedTotal,
                        showNewSale = shopOn && can(Permission.MAKE_SALE),
                        showNewJob = jobsOn && can(Permission.INTAKE_JOB),
                        attentionCount = pendings.size,
                        canSeeReports = can(Permission.VIEW_FINANCIAL_REPORTS),
                        canSeeSalesToday = can(Permission.VIEW_FINANCIAL_REPORTS) || can(Permission.MAKE_SALE),
                        onNewSale = onNavigateToPos,
                        onNewJob = onNavigateToJobOrders,
                        onSeeAttention = { tab = ShellTab.PENDINGS }
                    )

                    ShellTab.DATA_ENTRY -> EntryGrid(dataEntries)

                    ShellTab.PENDINGS -> PendingContent(pendings)

                    ShellTab.MORE -> MoreContent(
                        shopOn = shopOn,
                        jobsOn = jobsOn,
                        role = role,
                        canEditSettings = can(Permission.EDIT_BUSINESS_SETTINGS),
                        onSetModules = viewModel::setModules,
                        entries = buildList {
                            add(Entry(AppLanguageManager.tr("Companies", "Kampuni"), AppLanguageManager.tr("Switch or add a company", "Badilisha au ongeza kampuni"), Icons.Default.Business, onSwitchCompany))
                            if (can(Permission.MANAGE_STAFF)) {
                                add(Entry(AppLanguageManager.tr("Users", "Watumiaji"), AppLanguageManager.tr("Staff and roles", "Wafanyakazi na majukumu"), Icons.Default.People, onNavigateToStaff))
                            }
                            if (can(Permission.VIEW_FINANCIAL_REPORTS)) {
                                add(Entry(AppLanguageManager.tr("Reports", "Ripoti"), AppLanguageManager.tr("Cashbook, profit, ledger", "Daftari la pesa, faida"), Icons.Default.Assessment, onNavigateToAccounting))
                            }
                            if (shopOn && can(Permission.MAKE_SALE)) {
                                add(Entry(AppLanguageManager.tr("Layaway", "Awali"), AppLanguageManager.tr("Pay in instalments", "Lipa kidogo kidogo"), Icons.Default.Receipt, onNavigateToLayaway))
                            }
                            if (jobsOn) {
                                add(Entry(AppLanguageManager.tr("Job order history", "Historia ya kazi"), AppLanguageManager.tr("Find past jobs", "Tafuta kazi za zamani"), Icons.Default.Build, onNavigateToVehicleHistory))
                            }
                            if (can(Permission.BACKUP_RESTORE)) {
                                add(Entry(AppLanguageManager.tr("Backup", "Hifadhi nakala"), AppLanguageManager.tr("Save a copy of your data", "Hifadhi nakala ya data"), Icons.Default.CloudUpload) {
                                    val app = context.applicationContext as com.example.businessos.BusinessOsApplication
                                    BackupRestoreHelper.exportDatabaseBackup(context, app.database)?.let { backupMessage = it }
                                })
                                add(Entry(AppLanguageManager.tr("Restore", "Rudisha nakala"), AppLanguageManager.tr("Replace data with a backup file", "Badilisha data na nakala"), Icons.Default.CloudUpload) {
                                    restorePicker.launch(arrayOf("*/*"))
                                })
                            }
                            if (role == Role.OWNER) {
                                add(Entry(AppLanguageManager.tr("Delete company", "Futa kampuni"), AppLanguageManager.tr("Remove this company and its data", "Ondoa kampuni hii na data yake"), Icons.Default.Security) { showDelete = true })
                            }
                            add(Entry(AppLanguageManager.tr("Security", "Usalama"), AppLanguageManager.tr("PIN and auto-lock", "PIN na kufunga"), Icons.Default.Lock, onNavigateToSecurity))
                            if (role == Role.OWNER) {
                                add(Entry(AppLanguageManager.tr("Activity log", "Kumbukumbu"), AppLanguageManager.tr("Who did what", "Nani alifanya nini"), Icons.Default.Security, onNavigateToAuditLog))
                            }
                            add(Entry(
                                AppLanguageManager.tr("Language", "Lugha"),
                                if (AppLanguageManager.isEnglish()) "English" else "Kiswahili",
                                Icons.Default.Translate
                            ) { AppLanguageManager.toggleLanguage(context) })
                            add(Entry(
                                AppLanguageManager.tr("Light or dark", "Mwanga au giza"),
                                if (ThemeManager.isDarkMode.value) AppLanguageManager.tr("Dark", "Giza") else AppLanguageManager.tr("Light", "Mwanga"),
                                if (ThemeManager.isDarkMode.value) Icons.Default.DarkMode else Icons.Default.LightMode
                            ) { ThemeManager.toggleTheme(context) })
                            add(Entry(AppLanguageManager.tr("User guide", "Mwongozo"), AppLanguageManager.tr("How to use the app", "Jinsi ya kutumia"), Icons.Default.MenuBook, onNavigateToManual))
                        }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Pieces
// ---------------------------------------------------------------------------------------------

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    indent: Boolean = false,
    trailing: ImageVector? = null,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, tint = AmberAccent) },
        label = { Text(label, fontSize = 16.sp, color = TextPrimary) },
        badge = if (trailing != null) {
            { Icon(trailing, contentDescription = null, tint = TextSecondary) }
        } else null,
        modifier = Modifier
            .padding(NavigationDrawerItemDefaults.ItemPadding)
            .padding(start = if (indent) 24.dp else 0.dp)
    )
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AmberAccent,
    selectedTextColor = AmberAccent,
    unselectedIconColor = TextSecondary,
    unselectedTextColor = TextSecondary,
    indicatorColor = DarkSurfaceElevated
)

@Composable
private fun androidx.compose.foundation.layout.RowScope.ShellNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = label) },
        label = { Text(label, fontSize = 12.sp, maxLines = 1) },
        colors = navItemColors()
    )
}

@Composable
private fun HomeContent(
    isLandscape: Boolean,
    salesToday: Money,
    expensesToday: Money,
    owed: Money,
    showNewSale: Boolean,
    showNewJob: Boolean,
    attentionCount: Int,
    canSeeReports: Boolean,
    canSeeSalesToday: Boolean,
    onNewSale: () -> Unit,
    onNewJob: () -> Unit,
    onSeeAttention: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val metrics = buildList {
            if (canSeeSalesToday) add(AppLanguageManager.tr("Sales today", "Mauzo ya leo") to salesToday)
            if (canSeeReports) {
                add(AppLanguageManager.tr("Spent today", "Matumizi ya leo") to expensesToday)
                add(AppLanguageManager.tr("Owed to you", "Unaodai") to owed)
            }
        }
        if (metrics.isNotEmpty()) {
            if (isLandscape) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    metrics.forEach { (label, value) -> MetricCard(label, value, Modifier.weight(1f)) }
                }
            } else {
                metrics.forEach { (label, value) -> MetricCard(label, value, Modifier.fillMaxWidth()) }
            }
        }

        if (showNewSale) {
            BigButton(AppLanguageManager.tr("New sale", "Mauzo mapya"), Icons.Default.AddShoppingCart, onNewSale)
        }
        if (showNewJob) {
            BigButton(AppLanguageManager.tr("New job order", "Kazi mpya"), Icons.Default.Build, onNewJob)
        }

        if (attentionCount > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeeAttention() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, AmberAccent)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PendingActions, contentDescription = null, tint = AmberAccent)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        AppLanguageManager.tr("$attentionCount things need your attention", "Mambo $attentionCount yanahitaji umakini wako"),
                        fontSize = 16.sp,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: Money, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(4.dp))
            Text(value.format(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1)
        }
    }
}

@Composable
private fun BigButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EntryGrid(entries: List<Entry>) {
    if (entries.isEmpty()) {
        EmptyNote(AppLanguageManager.tr("Nothing to add with your role.", "Hakuna cha kuongeza kwa jukumu lako."))
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        gridItems(entries) { EntryCard(it) }
    }
}

@Composable
private fun EntryCard(entry: Entry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { entry.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(entry.icon, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(10.dp))
            Text(entry.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(2.dp))
            Text(entry.subtitle, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EntryRow(entry: Entry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { entry.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(entry.icon, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(entry.subtitle, fontSize = 13.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
private fun PendingContent(pendings: List<Entry>) {
    if (pendings.isEmpty()) {
        EmptyNote(AppLanguageManager.tr("Nothing pending. You are up to date.", "Hakuna kinachosubiri. Uko sawa."))
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(pendings) { EntryRow(it) }
    }
}

@Composable
private fun MoreContent(
    shopOn: Boolean,
    jobsOn: Boolean,
    role: Role,
    canEditSettings: Boolean,
    onSetModules: (Boolean, Boolean) -> Unit,
    entries: List<Entry>
) {
    val context = LocalContext.current
    var gridView by remember {
        mutableStateOf(
            try {
                context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE).getBoolean(KEY_MORE_GRID, true)
            } catch (e: Exception) {
                true
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                AppLanguageManager.tr("Settings and tools", "Mipangilio na zana"),
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                gridView = !gridView
                try {
                    context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
                        .edit().putBoolean(KEY_MORE_GRID, gridView).apply()
                } catch (e: Exception) {
                    // remembering the choice is a convenience only
                }
            }) {
                Icon(
                    if (gridView) Icons.Default.ViewList else Icons.Default.GridView,
                    contentDescription = if (gridView) AppLanguageManager.tr("List view", "Orodha") else AppLanguageManager.tr("Grid view", "Gridi"),
                    tint = AmberAccent
                )
            }
        }

        val header: @Composable () -> Unit = {
            if (canEditSettings) {
                ModulesCard(shopOn, jobsOn, onSetModules)
            }
        }

        if (gridView) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (canEditSettings) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) { header() }
                }
                gridItems(entries) { EntryCard(it) }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (canEditSettings) {
                    item { header() }
                }
                items(entries) { EntryRow(it) }
            }
        }
    }
}

@Composable
private fun ModulesCard(shopOn: Boolean, jobsOn: Boolean, onSetModules: (Boolean, Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                AppLanguageManager.tr("What this company uses", "Kampuni hii inatumia nini"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(8.dp))
            ModuleSwitchRow(AppLanguageManager.tr("Sales", "Mauzo"), shopOn) { onSetModules(it, jobsOn) }
            ModuleSwitchRow(AppLanguageManager.tr("Job orders", "Kazi"), jobsOn) { onSetModules(shopOn, it) }
            Text(
                AppLanguageManager.tr(
                    "Switching one off only hides it. Your records are kept.",
                    "Kuzima kunaficha tu. Rekodi zako zinabaki."
                ),
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ModuleSwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 16.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun EmptyNote(text: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, fontSize = 16.sp, color = TextSecondary)
    }
}

private fun startOfToday(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis
