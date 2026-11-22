package com.example.businessos.data.security

/**
 * Wrong-PIN backoff. The first few mistakes are free, then each further mistake
 * doubles the wait: 30s, 1m, 2m, 4m, 8m and finally 15 minutes.
 */
object LockoutPolicy {
    const val FREE_ATTEMPTS = 4
    private const val BASE_MILLIS = 30_000L
    private const val MAX_MILLIS = 15 * 60_000L

    /** How long to lock after [failedAttempts] wrong PINs in a row (0 = not locked). */
    fun lockoutMillis(failedAttempts: Int): Long {
        if (failedAttempts <= FREE_ATTEMPTS) return 0L
        val steps = (failedAttempts - FREE_ATTEMPTS - 1).coerceAtMost(10)
        return (BASE_MILLIS shl steps).coerceAtMost(MAX_MILLIS)
    }

    fun triesLeftBeforeLock(failedAttempts: Int): Int =
        (FREE_ATTEMPTS + 1 - failedAttempts).coerceAtLeast(0)
}
