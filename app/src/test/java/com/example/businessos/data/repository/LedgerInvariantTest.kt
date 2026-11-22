package com.example.businessos.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.businessos.data.local.AppDatabase
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.data.model.SaleLineItem
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real Room database in memory. After every operation the books must still balance. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LedgerInvariantTest {

    private lateinit var db: AppDatabase
    private lateinit var auth: AuthRepository
    private lateinit var repo: BusinessOsRepository
    private lateinit var bizId: String

    @Before
    fun setUp() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries().build()
        auth = AuthRepository(db, db.userDao(), db.businessDao(), db.businessMemberDao())
        assertTrue(auth.signup("Owner", "0712345678", "123456").isSuccess)
        assertTrue(auth.login("0712345678", "123456").isSuccess)
        val biz = auth.createBusiness(
            "Test Shop",
            openingCash = Money(100_000),
            openingMpesa = Money(50_000)
        ).getOrThrow()
        bizId = biz.id
        repo = BusinessOsRepository(
            database = db, authRepository = auth,
            businessDao = db.businessDao(), businessMemberDao = db.businessMemberDao(),
            userDao = db.userDao(), itemDao = db.itemDao(), saleDao = db.saleDao(),
            saleItemDao = db.saleItemDao(), stockMovementDao = db.stockMovementDao(),
            documentCounterDao = db.documentCounterDao(), customerDao = db.customerDao(),
            jobCardDao = db.jobCardDao(), jobPartDao = db.jobPartDao(),
            jobStageTransitionDao = db.jobStageTransitionDao(), auditLogDao = db.auditLogDao(),
            expenseDao = db.expenseDao(), accountDao = db.accountDao(),
            journalEntryDao = db.journalEntryDao(), journalLineDao = db.journalLineDao(),
            invoiceDao = db.invoiceDao(), invoiceItemDao = db.invoiceItemDao(),
            supplierDao = db.supplierDao(), paymentDao = db.paymentDao(),
            wagePaymentDao = db.wagePaymentDao(), layawayDao = db.layawayDao()
        )
    }

    @After
    fun tearDown() = db.close()

    private fun scalar(sql: String): Long =
        db.openHelper.readableDatabase.query(sql).use { c -> c.moveToFirst(); c.getLong(0) }

    private fun assertBooksBalance() {
        // Every journal entry: debits equal credits, and match the header totals.
        assertEquals(
            "unbalanced entries", 0L,
            scalar(
                """SELECT COUNT(*) FROM journal_entries e WHERE
                   (SELECT COALESCE(SUM(debitCents),0) FROM journal_lines l WHERE l.entryId = e.id)
                   != (SELECT COALESCE(SUM(creditCents),0) FROM journal_lines l WHERE l.entryId = e.id)
                   OR e.totalDebitCents != e.totalCreditCents"""
            )
        )
        // Every account balance equals the signed sum of its journal lines.
        assertEquals(
            "account balance drift", 0L,
            scalar(
                """SELECT COUNT(*) FROM accounts a WHERE a.balanceCents !=
                   (SELECT COALESCE(SUM(CASE WHEN a.category IN ('ASSET','EXPENSE')
                        THEN l.debitCents - l.creditCents ELSE l.creditCents - l.debitCents END),0)
                    FROM journal_lines l WHERE l.accountId = a.id)"""
            )
        )
        // Stock on hand equals the sum of its movements.
        assertEquals(
            "stock drift", 0L,
            scalar(
                """SELECT COUNT(*) FROM items i WHERE i.stockQty !=
                   (SELECT COALESCE(SUM(quantityDelta),0) FROM stock_movements m WHERE m.itemId = i.id)"""
            )
        )
    }

    @Test
    fun newBusinessOnlyHasOpeningBalances() {
        assertEquals(0L, scalar("SELECT COUNT(*) FROM items"))
        assertEquals(0L, scalar("SELECT COUNT(*) FROM customers"))
        assertEquals(0L, scalar("SELECT COUNT(*) FROM sales"))
        assertEquals(2L, scalar("SELECT COUNT(*) FROM journal_entries"))
        assertEquals(100_000L, scalar("SELECT balanceCents FROM accounts WHERE code='1010'"))
        assertEquals(50_000L, scalar("SELECT balanceCents FROM accounts WHERE code='1020'"))
        assertEquals(150_000L, scalar("SELECT balanceCents FROM accounts WHERE code='3010'"))
        assertBooksBalance()
    }

    @Test
    fun saleKeepsBooksBalancedAndReducesStock() = runBlocking {
        val item = repo.createItem(
            "Shoe", Money(150_000), Money(120_000), initialStock = 10, costPrice = Money(90_000)
        ).getOrThrow()
        assertBooksBalance()
        val sale = repo.recordSale(
            null, null,
            listOf(SaleLineItem(item.id, item.name, 3, Money(150_000), Money(90_000))),
            paymentMethod = PaymentMethod.CASH
        ).getOrThrow()
        assertEquals(7L, scalar("SELECT stockQty FROM items WHERE id='${item.id}'"))
        assertEquals(450_000L, sale.totalCents)
        assertBooksBalance()
    }

    @Test
    fun voidingASaleRestoresStockAndKeepsBooksBalanced() = runBlocking {
        val item = repo.createItem("Shoe", Money(150_000), Money(120_000), initialStock = 10).getOrThrow()
        val sale = repo.recordSale(
            null, null, listOf(SaleLineItem(item.id, item.name, 4, Money(150_000)))
        ).getOrThrow()
        assertTrue(repo.voidSale(sale.id, "customer returned").isSuccess)
        assertEquals(10L, scalar("SELECT stockQty FROM items WHERE id='${item.id}'"))
        // The sale is kept (voided), never deleted.
        assertEquals(1L, scalar("SELECT COUNT(*) FROM sales WHERE id='${sale.id}'"))
        assertBooksBalance()
    }

    @Test
    fun creditSaleAndDeniPaymentKeepCustomerBalanceCorrect() = runBlocking {
        val customer = repo.createCustomer("Mama Wanjiru", "0722000111").getOrThrow()
        val item = repo.createItem("Bag", Money(100_000), Money(80_000), initialStock = 5).getOrThrow()
        repo.recordSale(
            customer.id, customer.name,
            listOf(SaleLineItem(item.id, item.name, 2, Money(100_000))),
            paymentMethod = PaymentMethod.CREDIT
        ).getOrThrow()
        assertEquals(200_000L, scalar("SELECT balanceCents FROM customers WHERE id='${customer.id}'"))
        repo.recordDeniPayment(customer.id, Money(50_000)).getOrThrow()
        assertEquals(150_000L, scalar("SELECT balanceCents FROM customers WHERE id='${customer.id}'"))
        assertBooksBalance()
    }

    @Test
    fun cannotSellMoreThanInStock() = runBlocking {
        // Owners may oversell by default; turn that off to test the stock guard.
        val userId = auth.session.value!!.userId
        val member = db.businessMemberDao().getMember(bizId, userId)!!
        db.businessMemberDao().insertMember(member.copy(canSellNegativeStock = false))
        val item = repo.createItem("Hat", Money(50_000), Money(40_000), initialStock = 2).getOrThrow()
        val result = repo.recordSale(
            null, null, listOf(SaleLineItem(item.id, item.name, 5, Money(50_000)))
        )
        assertTrue(result.isFailure)
        assertEquals(2L, scalar("SELECT stockQty FROM items WHERE id='${item.id}'"))
        assertBooksBalance()
    }

    @Test
    fun deletingBusinessNeedsOwnerAndExactName() = runBlocking {
        assertTrue(auth.deleteBusiness(bizId, "wrong name").isFailure)
        assertEquals(1L, scalar("SELECT COUNT(*) FROM businesses"))
        assertTrue(auth.deleteBusiness(bizId, "Test Shop").isSuccess)
        assertEquals(0L, scalar("SELECT COUNT(*) FROM businesses"))
        assertEquals(0L, scalar("SELECT COUNT(*) FROM accounts"))
        assertEquals(0L, scalar("SELECT COUNT(*) FROM journal_entries"))
    }

    @Test
    fun sameCustomerPhoneIsNotAddedTwice() = runBlocking {
        assertTrue(repo.createCustomer("Mama Wanjiru", "0722000111").isSuccess)
        assertTrue(repo.createCustomer("Another", "+254 722 000 111").isFailure)
        assertEquals(1L, scalar("SELECT COUNT(*) FROM customers"))
        assertEquals("+254722000111", db.openHelper.readableDatabase.query("SELECT phone FROM customers").use { it.moveToFirst(); it.getString(0) })
    }
}
