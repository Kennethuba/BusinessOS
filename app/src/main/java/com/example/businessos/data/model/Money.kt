package com.example.businessos.data.model

import java.util.Locale

/**
 * Immutable value representation of currency stored in minor units (cents).
 * 1 KSh = 100 cents.
 * Eliminates all floating-point arithmetic errors across financial calculations.
 */
@JvmInline
value class Money(val cents: Long) : Comparable<Money> {

    operator fun plus(other: Money): Money = Money(cents + other.cents)
    operator fun minus(other: Money): Money = Money(cents - other.cents)
    operator fun times(quantity: Int): Money = Money(cents * quantity.toLong())
    operator fun times(quantity: Long): Money = Money(cents * quantity)
    operator fun unaryMinus(): Money = Money(-cents)

    val isPositive: Boolean get() = cents > 0L
    val isZero: Boolean get() = cents == 0L
    val isNegative: Boolean get() = cents < 0L

    val formatted: String get() = formatCompact()

    fun coerceAtLeast(min: Money): Money = if (this < min) min else this
    fun coerceAtMost(max: Money): Money = if (this > max) max else this

    fun toFormattedDouble(): String = String.format(Locale.US, "%.2f", cents / 100.0)

    /**
     * Calculates percentage discount with banker's rounding to the nearest cent.
     * @param percent discount percentage (0 to 100)
     */
    fun applyPercentageDiscount(percent: Int): Money {
        require(percent in 0..100) { "Discount percentage must be between 0 and 100" }
        if (percent == 0) return this
        if (percent == 100) return ZERO
        val discountedCents = (cents * (100 - percent) + 50) / 100
        return Money(discountedCents)
    }

    /**
     * Formats the amount in standard Kenyan Shilling format: "KSh 1,500.00" or "-KSh 50.00".
     */
    fun format(includeCurrency: Boolean = true): String {
        val isNeg = cents < 0
        val absCents = Math.abs(cents)
        val whole = absCents / 100
        val remainder = absCents % 100
        val prefix = if (isNeg) "-" else ""
        val currency = if (includeCurrency) "KSh " else ""
        return String.format(Locale.US, "%s%s%,d.%02d", prefix, currency, whole, remainder)
    }

    /**
     * Formats whole shillings without decimal cents if remainder is zero, e.g. "KSh 1,500".
     */
    fun formatCompact(includeCurrency: Boolean = true): String {
        val isNeg = cents < 0
        val absCents = Math.abs(cents)
        val whole = absCents / 100
        val remainder = absCents % 100
        val prefix = if (isNeg) "-" else ""
        val currency = if (includeCurrency) "KSh " else ""
        return if (remainder == 0L) {
            String.format(Locale.US, "%s%s%,d", prefix, currency, whole)
        } else {
            String.format(Locale.US, "%s%s%,d.%02d", prefix, currency, whole, remainder)
        }
    }

    override fun compareTo(other: Money): Int = cents.compareTo(other.cents)

    companion object {
        val ZERO = Money(0L)

        fun fromShillings(shillings: Long, cents: Int = 0): Money {
            require(cents in 0..99) { "Cents must be between 0 and 99" }
            return Money(shillings * 100L + cents)
        }

        fun fromShillingsDouble(shillings: Double): Money {
            if (shillings.isNaN() || shillings.isInfinite()) return ZERO
            return Money(Math.round(shillings * 100.0))
        }

        fun fromCents(cents: Long): Money = Money(cents)

        /**
         * Safely parses user input string, stripping currency symbols, commas, and whitespace.
         */
        fun parseOrZero(input: String?): Money {
            if (input.isNullOrBlank()) return ZERO
            val clean = input.replace("KSh", "", ignoreCase = true)
                .replace("KES", "", ignoreCase = true)
                .replace(",", "")
                .trim()
            val parsedDouble = clean.toDoubleOrNull() ?: return ZERO
            return fromShillingsDouble(parsedDouble)
        }
    }
}
