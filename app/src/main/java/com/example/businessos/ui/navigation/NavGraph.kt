package com.example.businessos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.businessos.data.repository.AuthRepository
import com.example.businessos.sync.SyncManager
import com.example.businessos.ui.ViewModelFactory
import com.example.businessos.ui.accounting.AccountingScreen
import com.example.businessos.ui.accounting.AccountingViewModel
import com.example.businessos.ui.audit.AuditLogScreen
import com.example.businessos.ui.audit.AuditLogViewModel
import com.example.businessos.ui.auth.AuthViewModel
import com.example.businessos.ui.auth.LoginScreen
import com.example.businessos.ui.auth.SignupScreen
import com.example.businessos.ui.deni.DeniLedgerScreen
import com.example.businessos.ui.deni.DeniViewModel
import com.example.businessos.ui.garage.GarageViewModel
import com.example.businessos.ui.garage.JobCardDetailScreen
import com.example.businessos.ui.garage.JobCardListScreen
import com.example.businessos.ui.garage.VehicleHistoryScreen
import com.example.businessos.ui.home.HomeScreen
import com.example.businessos.ui.home.HomeViewModel
import com.example.businessos.ui.pos.PosScreen
import com.example.businessos.ui.pos.PosViewModel
import com.example.businessos.ui.staff.StaffManagementScreen
import com.example.businessos.ui.staff.StaffViewModel
import com.example.businessos.ui.subscription.SubscriptionScreen
import com.example.businessos.ui.subscription.SubscriptionViewModel
import com.example.businessos.ui.workspace.WorkspaceSwitcherScreen
import com.example.businessos.ui.workspace.WorkspaceViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModelFactory: ViewModelFactory,
    authRepository: AuthRepository,
    syncManager: SyncManager
) {
    val session by authRepository.session.collectAsState()
    val syncState by syncManager.syncState.collectAsState()

    val startDestination = when {
        session == null -> Screen.Login.route
        session?.activeBusinessId == null -> Screen.WorkspaceSwitcher.route
        else -> Screen.BusinessHome.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth: Login
        composable(Screen.Login.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToSignup = {
                    navController.navigate(Screen.Signup.route)
                },
                onLoginSuccess = { hasBusiness ->
                    if (hasBusiness) {
                        navController.navigate(Screen.BusinessHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.WorkspaceSwitcher.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Auth: Signup
        composable(Screen.Signup.route) {
            val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
            SignupScreen(
                viewModel = authViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignupSuccess = {
                    navController.navigate(Screen.WorkspaceSwitcher.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Workspace Switcher
        composable(Screen.WorkspaceSwitcher.route) {
            val workspaceViewModel: WorkspaceViewModel = viewModel(factory = viewModelFactory)
            WorkspaceSwitcherScreen(
                viewModel = workspaceViewModel,
                onWorkspaceSelected = {
                    navController.navigate(Screen.BusinessHome.route) {
                        popUpTo(Screen.WorkspaceSwitcher.route) { inclusive = true }
                    }
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Business Home Dashboard
        composable(Screen.BusinessHome.route) {
            val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
            HomeScreen(
                viewModel = homeViewModel,
                onSwitchWorkspace = {
                    navController.navigate(Screen.WorkspaceSwitcher.route)
                },
                onNavigateToPos = {
                    navController.navigate(Screen.Pos.route)
                },
                onNavigateToDeniLedger = {
                    navController.navigate(Screen.DeniLedger.route)
                },
                onNavigateToGarageJobs = {
                    navController.navigate(Screen.GarageJobs.route)
                },
                onNavigateToVehicleHistory = {
                    navController.navigate(Screen.VehicleHistory.route)
                },
                onNavigateToStaff = {
                    navController.navigate(Screen.StaffManagement.route)
                },
                onNavigateToAuditLog = {
                    navController.navigate(Screen.AuditLog.route)
                },
                onNavigateToSubscription = {
                    navController.navigate(Screen.SubscriptionBilling.route)
                },
                onNavigateToManual = {
                    navController.navigate(Screen.UserManual.route)
                },
                onNavigateToFeedback = {
                    navController.navigate(Screen.Feedback.route)
                },
                onNavigateToAccounting = {
                    navController.navigate(Screen.Accounting.route)
                },
                onNavigateToAssistant = {
                    navController.navigate(Screen.Assistant.route)
                },
                onNavigateToStockAudit = {
                    navController.navigate(Screen.StockAudit.route)
                },
                onNavigateToLayaway = {
                    navController.navigate(Screen.Layaway.route)
                },
                onNavigateToPinLock = {
                    navController.navigate(Screen.PinLock.route)
                }
            )
        }

        // Module: Accounting & Cashbook (P&L, Cash-In vs Cash-Out, M-Pesa Reconciliation)
        composable(Screen.Accounting.route) {
            val accountingViewModel: AccountingViewModel = viewModel(factory = viewModelFactory)
            AccountingScreen(
                viewModel = accountingViewModel,
                syncState = syncState,
                onNavigateBack = { navController.popBackStack() },
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) }
            )
        }

        // Module 2: POS (Kariakor Stall)
        composable(Screen.Pos.route) {
            val posViewModel: PosViewModel = viewModel(factory = viewModelFactory)
            PosScreen(
                viewModel = posViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) }
            )
        }

        // Module 2: Deni Ledger (Customer Credit)
        composable(Screen.DeniLedger.route) {
            val deniViewModel: DeniViewModel = viewModel(factory = viewModelFactory)
            DeniLedgerScreen(
                viewModel = deniViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) }
            )
        }

        // Module 1: Auto Garage Job Cards
        composable(Screen.GarageJobs.route) {
            val garageViewModel: GarageViewModel = viewModel(factory = viewModelFactory)
            JobCardListScreen(
                viewModel = garageViewModel,
                onNavigateToJobDetail = { jobCardId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobCardId))
                },
                onNavigateToVehicleHistory = {
                    navController.navigate(Screen.VehicleHistory.route)
                },
                onSwitchWorkspace = {
                    navController.navigate(Screen.WorkspaceSwitcher.route)
                }
            )
        }

        // Module 1: Job Card Detail (with 3D Vehicle Inspector)
        composable(
            route = Screen.JobDetail.route,
            arguments = listOf(navArgument("jobCardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val jobCardId = backStackEntry.arguments?.getString("jobCardId") ?: ""
            val garageViewModel: GarageViewModel = viewModel(factory = viewModelFactory)
            JobCardDetailScreen(
                jobCardId = jobCardId,
                viewModel = garageViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Module 1: Vehicle History by Plate
        composable(Screen.VehicleHistory.route) {
            val garageViewModel: GarageViewModel = viewModel(factory = viewModelFactory)
            VehicleHistoryScreen(
                viewModel = garageViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToJobDetail = { jobCardId ->
                    navController.navigate(Screen.JobDetail.createRoute(jobCardId))
                }
            )
        }

        // Management: Staff Management (Owner / Manager)
        composable(Screen.StaffManagement.route) {
            val staffViewModel: StaffViewModel = viewModel(factory = viewModelFactory)
            StaffManagementScreen(
                viewModel = staffViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Management: Append-Only Audit Log (Owner Only, 3D Shield)
        composable(Screen.AuditLog.route) {
            val auditLogViewModel: AuditLogViewModel = viewModel(factory = viewModelFactory)
            AuditLogScreen(
                viewModel = auditLogViewModel,
                syncState = syncState,
                businessName = session?.activeBusinessName,
                userRole = session?.activeRole,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // Management: Subscription & Billing (3D Plan Vault)
        composable(Screen.SubscriptionBilling.route) {
            val subscriptionViewModel: SubscriptionViewModel = viewModel(factory = viewModelFactory)
            SubscriptionScreen(
                viewModel = subscriptionViewModel,
                syncState = syncState,
                businessName = session?.activeBusinessName,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // Field Operations & User Manual
        composable(Screen.UserManual.route) {
            com.example.businessos.ui.manual.UserManualScreen(
                syncState = syncState,
                businessName = session?.activeBusinessName,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // Developer Feedback & Client Welcome
        composable(Screen.Feedback.route) {
            com.example.businessos.ui.feedback.FeedbackScreen(
                userEmail = session?.email,
                userName = session?.name,
                syncState = syncState,
                businessName = session?.activeBusinessName,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onBack = { navController.popBackStack() }
            )
        }

        // Module: AI Business Copilot & Assistant (Mshauri wa Biashara)
        composable(Screen.Assistant.route) {
            val assistantViewModel: com.example.businessos.ui.assistant.AssistantViewModel = viewModel(factory = viewModelFactory)
            com.example.businessos.ui.assistant.AssistantScreen(
                viewModel = assistantViewModel,
                syncState = syncState,
                businessName = session?.activeBusinessName,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onNavigateToScreen = { route -> navController.navigate(route) },
                onBack = { navController.popBackStack() }
            )
        }

        // Module: End-of-Month Stock Take / Audit
        composable(Screen.StockAudit.route) {
            val posViewModel: PosViewModel = viewModel(factory = viewModelFactory)
            val items by posViewModel.items.collectAsState()
            com.example.businessos.ui.pos.StockAuditScreen(
                items = items,
                businessName = session?.activeBusinessName,
                syncState = syncState,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onNavigateBack = { navController.popBackStack() },
                onConfirmAudit = { updates ->
                    updates.forEach { (item, physical) ->
                        if (physical != item.stockQty) {
                            posViewModel.updateStockItemCount(item.id, physical)
                        }
                    }
                }
            )
        }

        // Module: Layaway / Awali (Installment Sales)
        composable(Screen.Layaway.route) {
            val layawayViewModel: com.example.businessos.ui.pos.LayawayViewModel = viewModel(factory = viewModelFactory)
            val layaways by layawayViewModel.layaways.collectAsState()
            com.example.businessos.ui.pos.LayawayScreen(
                layaways = layaways,
                businessName = session?.activeBusinessName,
                syncState = syncState,
                onSwitchWorkspace = { navController.navigate(Screen.WorkspaceSwitcher.route) },
                onNavigateBack = { navController.popBackStack() },
                onCreateLayaway = { name, phone, desc, total, deposit, dueDate ->
                    layawayViewModel.createLayaway(name, phone, desc, total, deposit, dueDate)
                },
                onRecordPayment = { id, amount, method ->
                    layawayViewModel.recordPayment(id, amount, method)
                }
            )
        }

        // Security: PIN Setup / Lock Screen
        composable(Screen.PinLock.route) {
            com.example.businessos.ui.auth.PinLockScreen(
                isSetupMode = true,
                onSuccess = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}
