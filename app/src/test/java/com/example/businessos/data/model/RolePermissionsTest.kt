package com.example.businessos.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RolePermissionsTest {

    @Test
    fun ownerHasEveryPermission() {
        Permission.entries.forEach { assertTrue("OWNER lacks $it", RolePermissions.has(Role.OWNER, it)) }
    }

    @Test
    fun onlyOwnerManagesStaffSettingsBackupAndDelete() {
        val ownerOnly = listOf(
            Permission.MANAGE_STAFF,
            Permission.EDIT_BUSINESS_SETTINGS,
            Permission.BACKUP_RESTORE,
            Permission.DELETE_BUSINESS
        )
        ownerOnly.forEach { p ->
            assertTrue(RolePermissions.has(Role.OWNER, p))
            listOf(Role.MANAGER, Role.CASHIER, Role.VIEWER).forEach { r ->
                assertFalse("$r should not have $p", RolePermissions.has(r, p))
            }
        }
    }

    @Test
    fun cashierCanSellButNotEditStockVoidOrPayWages() {
        assertTrue(RolePermissions.has(Role.CASHIER, Permission.MAKE_SALE))
        assertTrue(RolePermissions.has(Role.CASHIER, Permission.INTAKE_JOB))
        assertFalse(RolePermissions.has(Role.CASHIER, Permission.EDIT_STOCK))
        assertFalse(RolePermissions.has(Role.CASHIER, Permission.VOID_RECORD))
        assertFalse(RolePermissions.has(Role.CASHIER, Permission.PAY_WAGES))
        assertFalse(RolePermissions.has(Role.CASHIER, Permission.RECORD_EXPENSE))
        assertFalse(RolePermissions.has(Role.CASHIER, Permission.SELL_NEGATIVE_STOCK))
    }

    @Test
    fun viewerIsReadOnly() {
        val allowed = setOf(Permission.VIEW_FINANCIAL_REPORTS, Permission.VIEW_NEGATIVE_STOCK_REPORT)
        assertEquals(allowed, RolePermissions.permissionsFor(Role.VIEWER))
    }

    @Test
    fun managerCanVoidAndRecordExpensesButNotManageStaff() {
        assertTrue(RolePermissions.has(Role.MANAGER, Permission.VOID_RECORD))
        assertTrue(RolePermissions.has(Role.MANAGER, Permission.RECORD_EXPENSE))
        assertTrue(RolePermissions.has(Role.MANAGER, Permission.EDIT_STOCK))
        assertFalse(RolePermissions.has(Role.MANAGER, Permission.MANAGE_STAFF))
    }

    @Test
    fun legacyStaffRoleMapsToCashier() {
        assertEquals(Role.CASHIER, Role.fromLegacy("STAFF"))
        assertEquals(Role.CASHIER, Role.fromLegacy("accountant"))
        assertEquals(Role.CASHIER, Role.fromLegacy(null))
        assertEquals(Role.OWNER, Role.fromLegacy("owner"))
    }
}
