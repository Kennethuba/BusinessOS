package com.example.businessos.ui.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.model.BusinessType
import com.example.businessos.util.FeatureFlag
import com.example.businessos.data.model.JobStage
import com.example.businessos.data.model.Role
import com.example.businessos.ui.auth.PinSecurityManager
import com.example.businessos.ui.components.BackupRestoreHelper
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
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TealAccent
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.theme.ThemeManager
import com.example.businessos.ui.util.AppLanguageManager

enum class HomeTab {
    DASHBOARD,
    TRANSACTIONS,
    REPORTS,
    MORE
}

data class ActionModule(
    val titleEn: String,
    val titleSw: String,
    val descEn: String,
    val descSw: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSwitchWorkspace: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToDeniLedger: () -> Unit,
    onNavigateToGarageJobs: () -> Unit,
    onNavigateToVehicleHistory: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onNavigateToAuditLog: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onNavigateToStockAudit: () -> Unit = {},
    onNavigateToLayaway: () -> Unit = {},
    onNavigateToPinLock: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(HomeTab.DASHBOARD) }
    var showDemoSuccessBanner by remember { mutableStateOf(false) }

    val session by viewModel.session.collectAsState()
    val jobCards by viewModel.jobCards.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val deniCustomers by viewModel.deniCustomers.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    val totalSales = Money(sales.sumOf { it.totalCents })
    val totalExpenses = Money(expenses.sumOf { it.amountCents })
    val netProfit = totalSales - totalExpenses
    val totalDeni = Money(deniCustomers.sumOf { it.balanceCents })

    val businessType = if (session?.isGarageEnabled == true) BusinessType.GARAGE else BusinessType.STALL
    val role = session?.activeRole ?: Role.CASHIER

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            BusinessOsTopBar(
                title = when (selectedTab) {
                    HomeTab.DASHBOARD -> AppLanguageManager.tr("Dashboard", "Dashibodi")
                    HomeTab.TRANSACTIONS -> AppLanguageManager.tr("Transactions", "Miamala")
                    HomeTab.REPORTS -> AppLanguageManager.tr("Reports", "Ripoti")
                    HomeTab.MORE -> AppLanguageManager.tr("Settings & Tools", "Mipangilio")
                },
                businessName = session?.activeBusinessName,
                role = role,
                onSwitchWorkspace = onSwitchWorkspace
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.DASHBOARD,
                    onClick = { selectedTab = HomeTab.DASHBOARD },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text(AppLanguageManager.tr("Dashboard", "Mwanzo"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.TRANSACTIONS,
                    onClick = { selectedTab = HomeTab.TRANSACTIONS },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                    label = { Text(AppLanguageManager.tr("Transactions", "Miamala"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.REPORTS,
                    onClick = { selectedTab = HomeTab.REPORTS },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "Reports") },
                    label = { Text(AppLanguageManager.tr("Reports", "Ripoti"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.MORE,
                    onClick = { selectedTab = HomeTab.MORE },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "More") },
                    label = { Text(AppLanguageManager.tr("More", "Zaidi"), fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AmberAccent,
                        selectedTextColor = AmberAccent,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = DarkSurfaceElevated
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                HomeTab.DASHBOARD -> DashboardView(
                    netProfit = netProfit,
                    totalSales = totalSales,
                    totalExpenses = totalExpenses,
                    totalDeni = totalDeni,
                    debtorCount = deniCustomers.size,
                    businessType = businessType,
                    activeJobCount = jobCards.count { it.stage != JobStage.INVOICED },
                    approvalJobCount = jobCards.count { it.stage == JobStage.AWAITING_APPROVAL },
                    salesCount = sales.size,
                    onNavigateToPos = onNavigateToPos,
                    onNavigateToGarageJobs = onNavigateToGarageJobs,
                    onNavigateToDeniLedger = onNavigateToDeniLedger,
                    onNavigateToAccounting = onNavigateToAccounting,
                    onSwitchToTransactions = { selectedTab = HomeTab.TRANSACTIONS },
                    onSwitchToReports = { selectedTab = HomeTab.REPORTS }
                )

                HomeTab.TRANSACTIONS -> TransactionsView(
                    businessType = businessType,
                    role = role,
                    onNavigateToPos = onNavigateToPos,
                    onNavigateToGarageJobs = onNavigateToGarageJobs,
                    onNavigateToVehicleHistory = onNavigateToVehicleHistory,
                    onNavigateToDeniLedger = onNavigateToDeniLedger,
                    onNavigateToLayaway = onNavigateToLayaway,
                    onNavigateToAccounting = onNavigateToAccounting,
                    onNavigateToStaff = onNavigateToStaff
                )

                HomeTab.REPORTS -> ReportsView(
                    role = role,
                    onNavigateToAccounting = onNavigateToAccounting,
                    onNavigateToPos = onNavigateToPos,
                    onNavigateToDeniLedger = onNavigateToDeniLedger,
                    onNavigateToStockAudit = onNavigateToStockAudit,
                    onNavigateToAuditLog = onNavigateToAuditLog
                )

                HomeTab.MORE -> MoreSettingsView(
                    businessType = businessType,
                    role = role,
                    onNavigateToStaff = onNavigateToStaff,
                    onNavigateToPinLock = onNavigateToPinLock,
                    onNavigateToManual = onNavigateToManual,
                    onSeedDemoData = {
                        viewModel.seedDemoData(businessType)
                        showDemoSuccessBanner = true
                    }
                )
            }
        }
    }
}

/**
 * Clean executive dashboard showing ONLY business highlights without clutter.
 */
@Composable
private fun DashboardView(
    netProfit: Money,
    totalSales: Money,
    totalExpenses: Money,
    totalDeni: Money,
    debtorCount: Int,
    businessType: BusinessType,
    activeJobCount: Int,
    approvalJobCount: Int,
    salesCount: Int,
    onNavigateToPos: () -> Unit,
    onNavigateToGarageJobs: () -> Unit,
    onNavigateToDeniLedger: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onSwitchToTransactions: () -> Unit,
    onSwitchToReports: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Net Profit & Turnover Highlight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToAccounting() },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLanguageManager.tr("NET PROFIT", "FAIDA SAFI"),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (netProfit >= Money.ZERO) StatusGreen else StatusRed,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = AppLanguageManager.tr("View Cashbook →", "Daftari ya Hesabu →"),
                        color = AmberAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(6.dp))
                Text(
                    text = netProfit.formatted,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CardBorder)
                )
                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            text = AppLanguageManager.tr("Total Sales", "Mauzo Yote"),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = totalSales.formatted,
                            color = StatusGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = AppLanguageManager.tr("Total Expenses", "Matumizi Yote"),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = totalExpenses.formatted,
                            color = StatusRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Balances & Liquidity (Cashbook vs Receivables)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToAccounting() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppLanguageManager.tr("Cash & M-Pesa", "Pesa Mkononi"),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = (totalSales - totalExpenses).coerceAtLeast(Money.ZERO).formatted,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = AppLanguageManager.tr("Cashbook balance", "Salio la hesabu"),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToDeniLedger() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = AppLanguageManager.tr("Receivables (Deni)", "Madeni ya Wateja"),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = totalDeni.formatted,
                        color = StatusAmber,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "$debtorCount " + AppLanguageManager.tr("debtors pending", "wanaodaiwa"),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Actions Row (1-Tap execution)
        Text(
            text = AppLanguageManager.tr("QUICK ACTIONS", "HATUA ZA HARAKA"),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = if (businessType == BusinessType.GARAGE) onNavigateToGarageJobs else onNavigateToPos,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Icon(
                    if (businessType == BusinessType.GARAGE) Icons.Default.Build else Icons.Default.AddShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (businessType == BusinessType.GARAGE) AppLanguageManager.tr("Job Card", "Kazi Mpya") else AppLanguageManager.tr("New Sale", "Mauzo"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onNavigateToDeniLedger,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppLanguageManager.tr("Deni", "Deni"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Button(
                onClick = onNavigateToAccounting,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text(
                    text = AppLanguageManager.tr("+ Expense", "+ Gharama"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Operational Pulse Highlight
        if (businessType == BusinessType.GARAGE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGarageJobs() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppLanguageManager.tr("Garage Job Status", "Hali ya Matengenezo Garage"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "$activeJobCount " + AppLanguageManager.tr("vehicles currently in repair", "magari yanatengenezwa") +
                                    (if (approvalJobCount > 0) " • $approvalJobCount " + AppLanguageManager.tr("awaiting quote", "subiri idhini") else ""),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSwitchToTransactions() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppLanguageManager.tr("Stall Sales Active", "Duka na Mauzo Tayari"),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "$salesCount " + AppLanguageManager.tr("sales recorded today", "mauzo yamerekodiwa leo"),
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Jump to Reports or Transactions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSwitchToTransactions() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = AppLanguageManager.tr("Transactions", "Miamala Yote"),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = AppLanguageManager.tr("Sales, Awali & Wages", "Mauzo, Awali na Mishahara"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSwitchToReports() },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = AppLanguageManager.tr("Reports Hub", "Ripoti za Hesabu"),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = AppLanguageManager.tr("P&L, Debtors & Stock Audit", "Faida, Madeni & Ukaguzi"),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Transactions Page with Grid / List view options.
 */
@Composable
private fun TransactionsView(
    businessType: BusinessType,
    role: Role,
    onNavigateToPos: () -> Unit,
    onNavigateToGarageJobs: () -> Unit,
    onNavigateToVehicleHistory: () -> Unit,
    onNavigateToDeniLedger: () -> Unit,
    onNavigateToLayaway: () -> Unit,
    onNavigateToAccounting: () -> Unit,
    onNavigateToStaff: () -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }

    val modules = remember(businessType, role) {
        val list = mutableListOf<ActionModule>()

        if (businessType == BusinessType.GARAGE) {
            list.add(
                ActionModule(
                    titleEn = "Garage Job Cards",
                    titleSw = "Kadi za Matengenezo (Jobs)",
                    descEn = "Intake vehicles, assign repairs (bodywork, paint, interior, tyres) & billing",
                    descSw = "Mapokezi ya magari, matengenezo na malipo",
                    icon = Icons.Default.Build,
                    onClick = onNavigateToGarageJobs
                )
            )
            list.add(
                ActionModule(
                    titleEn = "Vehicle Service History",
                    titleSw = "Historia ya Magari",
                    descEn = "Search past repairs, invoices and parts replaced by number plate",
                    descSw = "Tafuta matengenezo ya awali kwa nambari ya gari",
                    icon = Icons.Default.DirectionsCar,
                    onClick = onNavigateToVehicleHistory
                )
            )
        } else {
            list.add(
                ActionModule(
                    titleEn = "Point of Sale (POS)",
                    titleSw = "Mauzo ya Dukani (POS)",
                    descEn = "Record sales, scan stock, wholesale/retail pricing, Cash & M-Pesa",
                    descSw = "Rekodi mauzo, bei ya jumla na rejareja, M-Pesa na taslimu",
                    icon = Icons.Default.PointOfSale,
                    onClick = onNavigateToPos
                )
            )
        }

        list.add(
            ActionModule(
                titleEn = "Deni (Credit) Ledger",
                titleSw = "Daftari la Madeni (Deni)",
                descEn = "Track customer balances, auto-paste M-Pesa SMS and send WhatsApp reminders",
                descSw = "Fuatilia madeni ya wateja, thibitisha M-Pesa na kumbusha WhatsApp",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = onNavigateToDeniLedger
            )
        )

        list.add(
            ActionModule(
                titleEn = "Layaway Installments (Awali)",
                titleSw = "Malipo ya Awamu (Awali)",
                descEn = "Track customer deposits, payment milestones and item pickup due dates",
                descSw = "Weka amana za wateja na malipo ya kidogo kidogo ya bidhaa",
                icon = Icons.Default.Savings,
                onClick = onNavigateToLayaway
            )
        )

        list.add(
            ActionModule(
                titleEn = "Record Business Expense",
                titleSw = "Rekodi Gharama ya Biashara",
                descEn = "Log rent, vehicle spares, tools, electricity, fuel and shop supplies",
                descSw = "Rekodi kodi, vipuri, bili za umeme na matumizi ya duka",
                icon = Icons.Default.ReceiptLong,
                onClick = onNavigateToAccounting
            )
        )

        if (role == Role.OWNER || role == Role.MANAGER) {
            list.add(
                ActionModule(
                    titleEn = "Staff Wages & Salaries",
                    titleSw = "Mishahara ya Wafanyakazi",
                    descEn = "Disburse staff wages, advances and view wage payment ledger",
                    descSw = "Lipa mishahara na marupurupu ya wafanyakazi",
                    icon = Icons.Default.People,
                    onClick = onNavigateToStaff
                )
            )
        }

        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // View Toggle Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppLanguageManager.tr("Select Operation", "Chagua Muamala"),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGridView) AmberAccent else Color.Transparent)
                        .clickable { isGridView = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid", tint = if (isGridView) TextOnAccent else TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Grid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGridView) TextOnAccent else TextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isGridView) AmberAccent else Color.Transparent)
                        .clickable { isGridView = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewList, contentDescription = "List", tint = if (!isGridView) TextOnAccent else TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("List", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isGridView) TextOnAccent else TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(modules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable { module.onClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(module.icon, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = AppLanguageManager.tr(module.titleEn, module.titleSw),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = AppLanguageManager.tr(module.descEn, module.descSw),
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(modules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { module.onClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(module.icon, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppLanguageManager.tr(module.titleEn, module.titleSw),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = AppLanguageManager.tr(module.descEn, module.descSw),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Reports Page with Grid / List view options.
 */
@Composable
private fun ReportsView(
    role: Role,
    onNavigateToAccounting: () -> Unit,
    onNavigateToPos: () -> Unit,
    onNavigateToDeniLedger: () -> Unit,
    onNavigateToStockAudit: () -> Unit,
    onNavigateToAuditLog: () -> Unit
) {
    var isGridView by remember { mutableStateOf(false) }

    val reportModules = remember(role) {
        val list = mutableListOf<ActionModule>()

        list.add(
            ActionModule(
                titleEn = "Profit & Loss / Cashbook",
                titleSw = "Daftari la Faida na Hasara",
                descEn = "Gross margin, net profit, operating expenses, cash vs M-Pesa balances",
                descSw = "Faida halisi, mapato, matumizi na fedha zilizopo",
                icon = Icons.Default.AccountBalanceWallet,
                onClick = onNavigateToAccounting
            )
        )

        list.add(
            ActionModule(
                titleEn = "Sales & Turnover Report",
                titleSw = "Ripoti ya Mauzo",
                descEn = "Daily sales summary, product turnover and transaction breakdown",
                descSw = "Mauzo ya siku, mwezi na takwimu za bidhaa zilizouzwa",
                icon = Icons.Default.Assessment,
                onClick = onNavigateToPos
            )
        )

        list.add(
            ActionModule(
                titleEn = "Customer Debtors Aging",
                titleSw = "Ripoti ya Madeni ya Wateja",
                descEn = "Unpaid balances, overdue accounts and batch WhatsApp reminder export",
                descSw = "Orodha ya wateja wanaodaiwa na vikumbusho vya WhatsApp",
                icon = Icons.Default.PointOfSale,
                onClick = onNavigateToDeniLedger
            )
        )

        list.add(
            ActionModule(
                titleEn = "Stock Audit & Valuation",
                titleSw = "Ukaguzi wa Akiba Dukani",
                descEn = "End-of-month stocktake: physical count vs system variance & shortage cost",
                descSw = "Hesabu ya mwisho wa mwezi: hesabu halisi vs mfumo na thamani ya upungufu",
                icon = Icons.Default.Checklist,
                onClick = onNavigateToStockAudit
            )
        )

        if (role == Role.OWNER) {
            list.add(
                ActionModule(
                    titleEn = "Security Audit Trail",
                    titleSw = "Kumbukumbu ya Ukaguzi (Audit)",
                    descEn = "Append-only trail of price overrides, invoice voids & inventory adjustments",
                    descSw = "Kumbukumbu ya mabadiliko ya bei, kufuta risiti na kurekebisha akiba",
                    icon = Icons.Default.Security,
                    onClick = onNavigateToAuditLog
                )
            )
        }

        list
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppLanguageManager.tr("Financial & Stock Reports", "Ripoti za Hesabu na Akiba"),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGridView) AmberAccent else Color.Transparent)
                        .clickable { isGridView = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GridView, contentDescription = "Grid", tint = if (isGridView) TextOnAccent else TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Grid", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isGridView) TextOnAccent else TextSecondary)
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (!isGridView) AmberAccent else Color.Transparent)
                        .clickable { isGridView = false }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewList, contentDescription = "List", tint = if (!isGridView) TextOnAccent else TextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("List", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!isGridView) TextOnAccent else TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (isGridView) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reportModules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clickable { module.onClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(module.icon, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(24.dp))
                            Column {
                                Text(
                                    text = AppLanguageManager.tr(module.titleEn, module.titleSw),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = AppLanguageManager.tr(module.descEn, module.descSw),
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reportModules) { module ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { module.onClick() },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(module.icon, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppLanguageManager.tr(module.titleEn, module.titleSw),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = AppLanguageManager.tr(module.descEn, module.descSw),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated More / Settings Page.
 */
@Composable
private fun MoreSettingsView(
    businessType: BusinessType,
    role: Role,
    onNavigateToStaff: () -> Unit,
    onNavigateToPinLock: () -> Unit,
    onNavigateToManual: () -> Unit,
    onSeedDemoData: () -> Unit
) {
    val context = LocalContext.current
    val isPinOn = remember { PinSecurityManager.isPinEnabled(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Language & Appearance Settings Box
        Text(
            text = AppLanguageManager.tr("PREFERENCES", "MIPANGILIO YA MFUMO"),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Language Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppLanguageManager.tr("Language", "Lugha ya Mfumo"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (AppLanguageManager.isEnglish()) "English (Main)" else "Kiswahili (Hiari)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (AppLanguageManager.isEnglish()) AmberAccent else Color.Transparent)
                                .clickable { AppLanguageManager.setLanguage(context, com.example.businessos.ui.util.AppLanguage.ENGLISH) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("English", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (AppLanguageManager.isEnglish()) TextOnAccent else TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!AppLanguageManager.isEnglish()) AmberAccent else Color.Transparent)
                                .clickable { AppLanguageManager.setLanguage(context, com.example.businessos.ui.util.AppLanguage.SWAHILI) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Kiswahili", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!AppLanguageManager.isEnglish()) TextOnAccent else TextSecondary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CardBorder))
                Spacer(modifier = Modifier.height(14.dp))

                // Theme Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = AppLanguageManager.tr("Theme Appearance", "Muonekano wa Rangi"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (!ThemeManager.isDarkMode.value) "White Background (Default)" else "Dark Matte",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!ThemeManager.isDarkMode.value) AmberAccent else Color.Transparent)
                                .clickable { ThemeManager.setDarkMode(context, false) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("White", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!ThemeManager.isDarkMode.value) TextOnAccent else TextSecondary)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (ThemeManager.isDarkMode.value) AmberAccent else Color.Transparent)
                                .clickable { ThemeManager.setDarkMode(context, true) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Black", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (ThemeManager.isDarkMode.value) TextOnAccent else TextSecondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Security & Tools
        Text(
            text = AppLanguageManager.tr("SECURITY & TOOLS", "USALAMA NA ZANA"),
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Staff
        if (role == Role.OWNER || role == Role.MANAGER) {
            SettingsItemCard(
                title = AppLanguageManager.tr("Staff Management & Roles", "Usimamizi wa Wafanyakazi"),
                description = AppLanguageManager.tr("Invite staff, assign Owner/Manager/Staff roles & permissions", "Ongeza wafanyakazi na kugawa majukumu"),
                icon = Icons.Default.People,
                onClick = onNavigateToStaff
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // PIN Lock
        SettingsItemCard(
            title = AppLanguageManager.tr("PIN App Lock", "PIN ya Usalama"),
            description = if (isPinOn) AppLanguageManager.tr("PIN enabled ✓ Tap to change or reset", "PIN imewashwa ✓ Gusa kubadilisha") else AppLanguageManager.tr("Set 4-digit PIN to protect records", "Weka PIN ya tarakimu 4 kulinda hesabu"),
            icon = Icons.Default.Lock,
            onClick = onNavigateToPinLock
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Backup
        SettingsItemCard(
            title = AppLanguageManager.tr("Google Drive & Local Backup", "Hifadhi Nakala (Google Drive)"),
            description = AppLanguageManager.tr("Export full database to Drive, WhatsApp or device in 1 tap", "Hifadhi nakala ya hesabu zako kwa mbofyo mmoja"),
            icon = Icons.Default.CloudUpload,
            onClick = { BackupRestoreHelper.exportDatabaseBackup(context) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Field Manual
        SettingsItemCard(
            title = AppLanguageManager.tr("User Manual & Guide", "Mwongozo wa Mtumiaji"),
            description = AppLanguageManager.tr("Operations manual for POS, Garages, Deni & Accounting", "Maelekezo kamili ya kutumia mfumo huu"),
            icon = Icons.Default.MenuBook,
            onClick = onNavigateToManual
        )

        if (role == Role.OWNER && FeatureFlag.ENABLE_DEMO_WORKSPACE) {
            Spacer(modifier = Modifier.height(8.dp))
            SettingsItemCard(
                title = AppLanguageManager.tr("Load Sample Demo Data", "Weka Data za Mfano"),
                description = AppLanguageManager.tr(
                    if (businessType == BusinessType.GARAGE) "Populate sample vehicles (Bodywork, Paint, Torn Leather, Tyres & Spares)" else "Populate sample products (Shoes, Belts, Spares) & Deni",
                    if (businessType == BusinessType.GARAGE) "Weka magari ya mfano (Bodi, Rangi, Viti, Matairi)" else "Weka bidhaa za mfano na madeni"
                ),
                icon = Icons.Default.AutoFixHigh,
                onClick = onSeedDemoData
            )
        }
    }
}

@Composable
private fun SettingsItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
