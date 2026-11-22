package com.example.businessos.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    fun arithmeticIsExactInCents() {
        val a = Money.fromShillings(100)
        val b = Money(9990)
        assertEquals(Money(10), a - b)
        assertEquals(Money(19990), a + b)
        assertEquals(Money(30000), a * 3)
    }

    @Test
    fun noFloatingPointDrift() {
        // 0.1 + 0.2 style errors must never appear with integer cents
        val total = Money.fromShillingsDouble(0.1) + Money.fromShillingsDouble(0.2)
        assertEquals(Money(30), total)
    }

    @Test
    fun percentageDiscountRoundsToNearestCent() {
        assertEquals(Money(9000), Money(10000).applyPercentageDiscount(10))
        assertEquals(Money(10000), Money(10000).applyPercentageDiscount(0))
        assertEquals(Money.ZERO, Money(10000).applyPercentageDiscount(100))
        assertEquals(Money(330), Money(1000).applyPercentageDiscount(67))
    }

    @Test(expected = IllegalArgumentException::class)
    fun discountOutOfRangeIsRejected() {
        Money(100).applyPercentageDiscount(101)
    }

    @Test
    fun formatting() {
        assertEquals("KSh 1,500.00", Money(150000).format())
        assertEquals("1,500.00", Money(150000).format(includeCurrency = false))
        assertEquals("-KSh 50.00", Money(-5000).format())
        assertEquals("KSh 1,500", Money(150000).formatCompact())
        assertEquals("KSh 1,500.50", Money(150050).formatCompact())
    }

    @Test
    fun parsingUserInput() {
        assertEquals(Money(150000), Money.parseOrZero("KSh 1,500"))
        assertEquals(Money(150050), Money.parseOrZero("1,500.50"))
        assertEquals(Money.ZERO, Money.parseOrZero("abc"))
        assertEquals(Money.ZERO, Money.parseOrZero(null))
        assertEquals(Money.ZERO, Money.parseOrZero("  "))
    }

    @Test
    fun signAndComparison() {
        assertTrue(Money(1).isPositive)
        assertTrue(Money(-1).isNegative)
        assertTrue(Money.ZERO.isZero)
        assertTrue(Money(5) < Money(6))
        assertFalse(Money(5) > Money(6))
        assertEquals(Money(5), Money(2).coerceAtLeast(Money(5)))
    }
}
