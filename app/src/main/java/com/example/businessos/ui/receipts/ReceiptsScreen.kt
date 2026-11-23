package com.example.businessos.ui.receipts

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.businessos.data.local.entity.PaymentEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.local.entity.SaleItemEntity
import com.example.businessos.data.model.Money
import com.example.businessos.ui.components.FullScreenForm
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager.tr
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun fmtDate(ms: Long) = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(ms))

private sealed class Open {
    data class Sale(val sale: SaleEntity) : Open()
    data class Payment(val payment: PaymentEntity) : Open()
}

/** Sales receipts and customer payment receipts, newest first. Tap one to see it and share it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsScreen(viewModel: ReceiptsViewModel, onBack: () -> Unit) {
    val sales by viewModel.sales.collectAsState()
    val payments by viewModel.payments.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var open by remember { mutableStateOf<Open?>(null) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(tr("Receipts", "Risiti"), color = TextPrimary) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )
                TabRow(selectedTabIndex = tab, containerColor = DarkBackground) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(tr("Sales", "Mauzo")) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(tr("Payments received", "Malipo yaliyopokelewa")) })
                }
            }
        }
    ) { padding ->
        val empty = if (tab == 0) sales.isEmpty() else payments.isEmpty()
        if (empty) {
            Text(
                tr("Nothing here yet.", "Hakuna bado."),
                color = TextSecondary,
                modifier = Modifier.padding(padding).padding(24.dp)
            )
        } else LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (tab == 0) items(sales, key = { it.id }) { s ->
                Row(Modifier.fillMaxWidth().clickable { open = Open.Sale(s) }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(s.saleNumber, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("${fmtDate(s.createdAt)}${s.customerName?.let { " - $it" } ?: ""}", color = TextSecondary)
                    }
                    Column {
                        Text(Money(s.totalCents).formatted, color = if (s.isVoided) StatusRed else TextPrimary, fontWeight = FontWeight.Bold)
                        if (s.isVoided) Text(tr("Voided", "Imefutwa"), color = StatusRed)
                    }
                }
            } else items(payments, key = { it.id }) { p ->
                Row(Modifier.fillMaxWidth().clickable { open = Open.Payment(p) }.padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(p.paymentNumber, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("${fmtDate(p.createdAt)}${if (p.notes.isNotBlank()) " - ${p.notes}" else ""}", color = TextSecondary)
                    }
                    Text(Money(p.amountCents).formatted, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    open?.let { o ->
        var lines by remember(o) { mutableStateOf<List<SaleItemEntity>>(emptyList()) }
        var business by remember(o) { mutableStateOf("") }
        LaunchedEffect(o) {
            business = viewModel.businessName()
            if (o is Open.Sale) lines = viewModel.saleItems(o.sale.id)
        }
        val text = when (o) {
            is Open.Sale -> saleText(business, o.sale, lines)
            is Open.Payment -> paymentText(business, o.payment)
        }
        val context = LocalContext.current
        FullScreenForm(
            onDismissRequest = { open = null },
            title = { Text(tr("Receipt", "Risiti"), fontWeight = FontWeight.Bold) },
            text = { Text(text, style = MaterialTheme.typography.bodyLarge, color = TextPrimary) },
            dismissButton = { TextButton(onClick = { open = null }) { Text(tr("Close", "Funga")) } },
            confirmButton = { TextButton(onClick = { share(context, text) }) { Text(tr("Share", "Tuma")) } }
        )
    }
}

private fun saleText(business: String, s: SaleEntity, lines: List<SaleItemEntity>): String = buildString {
    appendLine(business)
    appendLine("${tr("Receipt", "Risiti")} ${s.saleNumber}")
    appendLine(fmtDate(s.createdAt))
    s.customerName?.let { appendLine("${tr("Customer", "Mteja")}: $it") }
    appendLine()
    lines.forEach { appendLine("${it.quantity} x ${it.itemName}  ${Money(it.subtotalCents).formatted}") }
    appendLine()
    if (s.discountCents > 0) appendLine("${tr("Discount", "Punguzo")}: ${Money(s.discountCents).formatted}")
    appendLine("${tr("Total", "Jumla")}: ${Money(s.totalCents).formatted}")
    appendLine("${tr("Paid by", "Imelipwa kwa")}: ${s.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() }}")
    s.mpesaReference?.let { appendLine("M-Pesa: $it") }
    if (s.isVoided) appendLine(tr("VOIDED", "IMEFUTWA").lowercase().replaceFirstChar { it.uppercase() })
    appendLine()
    append(tr("Thank you", "Asante"))
}

private fun paymentText(business: String, p: PaymentEntity): String = buildString {
    appendLine(business)
    appendLine("${tr("Receipt", "Risiti")} ${p.paymentNumber}")
    appendLine(fmtDate(p.createdAt))
    if (p.notes.isNotBlank()) appendLine("${tr("Received from", "Kutoka kwa")}: ${p.notes}")
    appendLine("${tr("Amount", "Kiasi")}: ${Money(p.amountCents).formatted}")
    appendLine("${tr("Paid by", "Imelipwa kwa")}: ${p.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() }}")
    p.mpesaCode?.let { appendLine("M-Pesa: $it") }
    appendLine()
    append(tr("Thank you", "Asante"))
}

private fun share(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
