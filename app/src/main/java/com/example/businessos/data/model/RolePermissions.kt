package com.example.businessos.data.model

/**
 * Role to permission matrix (PLAN.md section 4.2).
 *
 * Pure Kotlin with no Android dependencies so it can be unit-tested on the JVM.
 * The repository calls [has] before every write operation; the UI only uses it to
 * hide buttons, never as the only line of defence.
 */
object RolePermissions {

    private val ownerPermissions: Set<Permission> = Permission.entries.toSet()

    private val managerPermissions: Set<Permission> = setOf(
        Permission.MAKE_SALE,
        Permission.SELL_NEGATIVE_STOCK,
        Permission.TAKE_PAYMENT,
        Permission.CREATE_CUSTOMER,
        Permission.INTAKE_JOB,
        Permission.UPDATE_JOB_STAGE,
        Permission.EDIT_STOCK,
        Permission.STOCK_TAKE_ADJUST,
        Permission.RECORD_EXPENSE,
        Permission.VOID_RECORD,
        Permission.PAY_WAGES,
        Permission.VIEW_FINANCIAL_REPORTS,
        Permission.VIEW_NEGATIVE_STOCK_REPORT
    )

    private val cashierPermissions: Set<Permission> = setOf(
        Permission.MAKE_SALE,
        Permission.TAKE_PAYMENT,
        Permission.CREATE_CUSTOMER,
        Permission.INTAKE_JOB,
        Permission.UPDATE_JOB_STAGE
    )

    private val viewerPermissions: Set<Permission> = setOf(
        Permission.VIEW_FINANCIAL_REPORTS,
        Permission.VIEW_NEGATIVE_STOCK_REPORT
    )

    fun permissionsFor(role: Role): Set<Permission> = when (role) {
        Role.OWNER -> ownerPermissions
        Role.MANAGER -> managerPermissions
        Role.CASHIER -> cashierPermissions
        Role.VIEWER -> viewerPermissions
    }

    fun has(role: Role, permission: Permission): Boolean = permission in permissionsFor(role)
}
