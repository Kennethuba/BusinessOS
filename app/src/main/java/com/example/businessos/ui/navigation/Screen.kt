package com.example.businessos.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object WorkspaceSwitcher : Screen("workspace_switcher")
    object SetupWizard : Screen("setup_wizard")
    object BusinessHome : Screen("business_home")

    // Kariakor Stall
    object Pos : Screen("pos")
    object DeniLedger : Screen("deni_ledger")

    // Auto Garage
    object GarageJobs : Screen("garage_jobs")
    object JobDetail : Screen("job_detail/{jobCardId}") {
        fun createRoute(jobCardId: String) = "job_detail/$jobCardId"
    }
    object VehicleHistory : Screen("vehicle_history")

    // Management & Settings
    object StaffManagement : Screen("staff_management")
    object AuditLog : Screen("audit_log")

    // Accounting & Cashbook
    object Accounting : Screen("accounting")

    // Operations & Sales Extensions
    object StockAudit : Screen("stock_audit")
    object Layaway : Screen("layaway")
    object Security : Screen("security")

    // Knowledge & Help
    object UserManual : Screen("user_manual")
}
