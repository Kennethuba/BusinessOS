package com.example.businessos.ui.components

import java.util.regex.Pattern

data class ParsedMpesaTransaction(
    val code: String,
    val amount: Double,
    val senderName: String,
    val senderPhone: String,
    val timestamp: String,
    val isValid: Boolean = true
)

object MpesaParser {

    /**
     * Parses standard Kenyan Safaricom M-Pesa SMS messages:
     * Examples:
     * "QH38KL990 Confirmed. Ksh1,500.00 received from JOHN KAMAU 0712345678 on 26/10/26 at 2:30 PM."
     * "RC74TY882 Confirmed. You have received Ksh3,450.00 from MARY WANJIKU 254722112233 on 26/10/26 at 11:15 AM."
     * "SB19KK334 Confirmed. Ksh 500.00 paid to BUSINESS OS TILL 543210 by HASSAN OMAR 0733123456 on 26/10/26 at 4:10 PM."
     */
    fun parse(sms: String): ParsedMpesaTransaction? {
        val trimmed = sms.trim()
        if (trimmed.isBlank()) return null

        // Extract transaction code (e.g. QH38KL990 Confirmed)
        val codePattern = Pattern.compile("^([A-Z0-9]{8,12})\\s+Confirmed", Pattern.CASE_INSENSITIVE)
        val codeMatcher = codePattern.matcher(trimmed)
        val code = if (codeMatcher.find()) codeMatcher.group(1).uppercase() else ""

        // Extract amount: e.g. Ksh1,500.00 or Ksh 2,400
        val amountPattern = Pattern.compile("(?:Ksh|KES)\\.?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        val amountMatcher = amountPattern.matcher(trimmed)
        val amountStr = if (amountMatcher.find()) amountMatcher.group(1).replace(",", "") else null
        val amount = amountStr?.toDoubleOrNull() ?: 0.0

        // Extract sender name and phone
        val senderPattern = Pattern.compile("(?:received from|from|by)\\s+([A-Z\\s]+?)\\s+(0[0-9]{9}|254[0-9]{9}|\\+254[0-9]{9})", Pattern.CASE_INSENSITIVE)
        val senderMatcher = senderPattern.matcher(trimmed)
        val (senderName, senderPhone) = if (senderMatcher.find()) {
            val name = senderMatcher.group(1).trim().uppercase()
            val phone = senderMatcher.group(2).trim()
            Pair(name, phone)
        } else {
            // Fallback for sender without phone number
            val fallbackPattern = Pattern.compile("(?:received from|from|by)\\s+([A-Z\\s]{3,30})\\s+on", Pattern.CASE_INSENSITIVE)
            val fbMatcher = fallbackPattern.matcher(trimmed)
            if (fbMatcher.find()) {
                Pair(fbMatcher.group(1).trim().uppercase(), "")
            } else {
                Pair("M-Pesa Customer", "")
            }
        }

        // Extract date/time
        val datePattern = Pattern.compile("on\\s+([0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}\\s+at\\s+[0-9]{1,2}:[0-9]{2}\\s*(?:AM|PM)?)", Pattern.CASE_INSENSITIVE)
        val dateMatcher = datePattern.matcher(trimmed)
        val timestamp = if (dateMatcher.find()) dateMatcher.group(1).trim() else ""

        if (code.isBlank() && amount <= 0.0) return null

        return ParsedMpesaTransaction(
            code = code,
            amount = amount,
            senderName = senderName,
            senderPhone = senderPhone,
            timestamp = timestamp
        )
    }
}
