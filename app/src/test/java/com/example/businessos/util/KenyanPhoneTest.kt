package com.example.businessos.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KenyanPhoneTest {

    @Test
    fun allCommonFormatsBecomeTheSameNumber() {
        val expected = "+254712345678"
        listOf("0712345678", "712345678", "254712345678", "+254712345678", "+254 712 345 678", "0712-345-678")
            .forEach { assertEquals(it, expected, KenyanPhone.normalize(it)) }
    }

    @Test
    fun newerOneRangeIsAccepted() {
        assertEquals("+254112345678", KenyanPhone.normalize("0112345678"))
    }

    @Test
    fun invalidNumbersAreRejected() {
        listOf("", "abc", "12345", "0612345678", "07123456789", "+1 555 123 4567").forEach {
            assertNull(it, KenyanPhone.normalize(it))
        }
    }

    @Test
    fun whatsAppFormatHasNoPlus() {
        assertEquals("254712345678", KenyanPhone.forWhatsApp("0712 345 678"))
        assertNull(KenyanPhone.forWhatsApp("nope"))
    }
}
