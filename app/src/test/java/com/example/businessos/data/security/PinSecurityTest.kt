package com.example.businessos.data.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinSecurityTest {

    @Test
    fun correctPinVerifiesAndWrongPinDoesNot() {
        val salt = PinSecurity.generateSalt()
        val hash = PinSecurity.hashPin("123456", salt)
        assertTrue(PinSecurity.verifyPin("123456", salt, hash))
        assertFalse(PinSecurity.verifyPin("123457", salt, hash))
    }

    @Test
    fun samePinWithDifferentSaltGivesDifferentHash() {
        val a = PinSecurity.hashPin("123456", PinSecurity.generateSalt())
        val b = PinSecurity.hashPin("123456", PinSecurity.generateSalt())
        assertNotEquals(a, b)
    }

    @Test
    fun hashNeverContainsThePin() {
        val salt = PinSecurity.generateSalt()
        assertFalse(PinSecurity.hashPin("123456", salt).contains("123456"))
        assertEquals(64, PinSecurity.hashPin("123456", salt).length)
    }

    @Test
    fun emptyStoredValuesNeverVerify() {
        assertFalse(PinSecurity.verifyPin("123456", "", ""))
    }

    @Test
    fun pinMustBeExactlySixDigits() {
        assertTrue(PinSecurity.isValidPin("000000"))
        listOf("", "12345", "1234567", "12345a", "12 456").forEach { assertFalse(it, PinSecurity.isValidPin(it)) }
    }
}
