package com.example.businessos.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.model.PartItem
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object WhatsAppHelper {

    private fun normalizeKenyanPhone(phone: String): String =
        com.example.businessos.util.KenyanPhone.forWhatsApp(phone) ?: phone.filter { it.isDigit() }

    fun openWhatsAppEstimateApproval(context: Context, jobCard: JobCardEntity, businessName: String?, parts: List<PartItem>) {
        val phone = normalizeKenyanPhone(jobCard.customerPhone)
        val partsSummary = if (parts.isNotEmpty()) {
            parts.joinToString("\n") { "• ${it.name}: ${it.unitPrice.formatCompact()}" }
        } else {
            "• Inspection & Diagnostic Services"
        }

        val message = """
            Hello ${jobCard.customerName},
            
            Here is the repair estimate for your vehicle *${jobCard.vehiclePlate}* at *${businessName ?: "our Garage"}*:
            
            *Job Description:*
            ${jobCard.description}
            
            *Parts & Labor Breakdown:*
            $partsSummary
            
            *Total Estimate:* KSh ${jobCard.estimateTotal.toInt()}
            
            Please reply with *APPROVED* to confirm and begin work, or let us know if you have questions.
        """.trimIndent()

        launchWhatsApp(context, phone, message)
    }

    fun openWhatsAppDeniReminder(
        context: Context,
        customerName: String,
        customerPhone: String,
        balance: Double,
        businessName: String?,
        dueDate: Long? = null
    ) {
        val phone = normalizeKenyanPhone(customerPhone)
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        val dueDateText = if (dueDate != null && dueDate > 0) {
            val dateStr = dateFormat.format(java.util.Date(dueDate))
            "\n*Tarehe ya Makubaliano:* $dateStr\n"
        } else ""

        val message = """
            Jambo $customerName,
            
            Hiki ni kikumbusho cha kirafiki kutoka *${businessName ?: "Business OS"}*.
            Salio lako la Deni lililobaki ni *KSh ${balance.toInt()}*.$dueDateText
            Tafadhali lipa kupitia M-Pesa kwa nambari hii au dukani unavyopata nafasi.
            
            Asante sana kwa ushirikiano wako na uaminifu!
        """.trimIndent()

        launchWhatsApp(context, phone, message)
    }

    fun shareGeneralText(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                `package` = "com.whatsapp"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to standard chooser if WhatsApp package alone fails
            try {
                val chooser = Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }, "Tuma Ripoti Kupitia")
                chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(chooser)
            } catch (err: Exception) {
                Toast.makeText(context, "Haikuweza kutuma: ${err.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchWhatsApp(context: Context, phone: String, message: String) {
        try {
            val encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            val url = "https://wa.me/$phone?text=$encodedMessage"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
