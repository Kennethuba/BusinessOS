package com.example.businessos.data.security

import org.junit.Assert.assertEquals
import org.junit.Test

class LockoutPolicyTest {

    @Test
    fun firstFourMistakesAreFree() {
        (0..4).forEach { assertEquals(0L, LockoutPolicy.lockoutMillis(it)) }
    }

    @Test
    fun waitDoublesAfterEachFurtherMistake() {
        assertEquals(30_000L, LockoutPolicy.lockoutMillis(5))
        assertEquals(60_000L, LockoutPolicy.lockoutMillis(6))
        assertEquals(120_000L, LockoutPolicy.lockoutMillis(7))
        assertEquals(240_000L, LockoutPolicy.lockoutMillis(8))
        assertEquals(480_000L, LockoutPolicy.lockoutMillis(9))
    }

    @Test
    fun waitNeverExceedsFifteenMinutes() {
        assertEquals(15 * 60_000L, LockoutPolicy.lockoutMillis(10))
        assertEquals(15 * 60_000L, LockoutPolicy.lockoutMillis(500))
    }

    @Test
    fun triesLeftCountsDown() {
        assertEquals(4, LockoutPolicy.triesLeftBeforeLock(1))
        assertEquals(1, LockoutPolicy.triesLeftBeforeLock(4))
        assertEquals(0, LockoutPolicy.triesLeftBeforeLock(9))
    }
}
