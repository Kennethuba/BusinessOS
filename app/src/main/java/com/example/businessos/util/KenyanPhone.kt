package com.example.businessos.util

/**
 * Normalises Kenyan mobile numbers so the same person is never stored twice.
 * Accepts 0712345678, 712345678, 254712345678, +254 712 345 678 and the 01xx range.
 * Returns +254XXXXXXXXX, or null when the input is not a Kenyan mobile number.
 */
object KenyanPhone {

    fun normalize(input: String): String? {
        val digits = input.filter { it.isDigit() }
        val national = when {
            input.trim().startsWith("+") && digits.startsWith("254") -> digits.removePrefix("254")
            digits.startsWith("254") && digits.length == 12 -> digits.removePrefix("254")
            digits.startsWith("0") && digits.length == 10 -> digits.removePrefix("0")
            digits.length == 9 -> digits
            else -> return null
        }
        if (national.length != 9) return null
        if (national[0] != '7' && national[0] != '1') return null
        return "+254$national"
    }

    /** Digits only, no plus sign, as WhatsApp links expect (254712345678). */
    fun forWhatsApp(input: String): String? = normalize(input)?.removePrefix("+")
}
