package com.example.businessos.ui.pos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.businessos.data.local.entity.ItemEntity
import com.example.businessos.data.model.Money
import com.example.businessos.data.model.PaymentMethod
import com.example.businessos.ui.components.FullScreenForm
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager.tr

private fun money(text: String): Money? {
    val t = text.trim().replace(",", "")
    if (t.isEmpty()) return Money.ZERO
    val d = t.toDoubleOrNull() ?: return null
    return if (d < 0 || d.isNaN() || d.isInfinite()) null else Money.fromShillingsDouble(d)
}

/** All items with their stock. Add an item, buy more of one, or go to a stock count. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: PosViewModel, onBack: () -> Unit, onStockCount: () -> Unit) {
    val items by viewModel.items.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val ok by viewModel.successMessage.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var buying by remember { mutableStateOf<ItemEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(error, ok) {
        (error ?: ok)?.let { snackbar.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(tr("Stock", "Akiba"), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary) }
                },
                actions = { TextButton(onClick = onStockCount) { Text(tr("Count stock", "Hesabu akiba")) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(tr("Add item", "Ongeza bidhaa")) }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(tr("No items yet", "Hakuna bidhaa bado"), style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(tr("Tap Add item to start.", "Gusa Ongeza bidhaa kuanza."), color = TextSecondary)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable { buying = item }.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text("${tr("Price", "Bei")} ${Money(item.retailPriceCents).formatted}", color = TextSecondary)
                        }
                        Column {
                            val low = item.stockQty <= item.lowStockThreshold
                            Text("${item.stockQty} ${item.unit}", color = if (low) StatusRed else TextPrimary, fontWeight = FontWeight.Bold)
                            if (low) Text(tr("Low", "Chini"), color = StatusRed)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAdd) AddItemForm(viewModel) { showAdd = false }
    buying?.let { BuyStockForm(it, viewModel) { buying = null } }
}

@Composable
private fun AddItemForm(viewModel: PosViewModel, onClose: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var retail by remember { mutableStateOf("") }
    var wholesale by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var low by remember { mutableStateOf("3") }
    var category by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    val context = androidx.compose.ui.platform.LocalContext.current
    val kind = com.example.businessos.util.BusinessKindStore.get(context, viewModel.session.value?.activeBusinessId)
    val categories = when (kind) {
        com.example.businessos.util.BusinessKind.STALL -> listOf(
            tr("Leather", "Ngozi"), tr("Soles", "Soli"), tr("Shoes", "Viatu"), tr("Belts", "Mikanda"),
            tr("Bags", "Mikoba"), tr("Thread and glue", "Uzi na gundi"), tr("Dye and polish", "Rangi na kiwi")
        )
        com.example.businessos.util.BusinessKind.GARAGE -> listOf(
            tr("Engine parts", "Vipuri vya injini"), tr("Brakes", "Breki"), tr("Tyres", "Matairi"),
            tr("Oil and fluids", "Mafuta"), tr("Electrical", "Umeme"), tr("Body and paint", "Bodi na rangi"), tr("Tools", "Zana")
        )
        else -> emptyList()
    }
    val units = if (kind == com.example.businessos.util.BusinessKind.STALL)
        listOf("pcs", "pair", "sq ft", "ft", "roll", "kg", "litre") else listOf("pcs", "pair", "set", "litre", "kg", "metre")
    val r = money(retail); val w = money(wholesale); val c = money(cost)
    val q = qty.trim().ifEmpty { "0" }.toLongOrNull()
    val valid = name.isNotBlank() && r != null && r > Money.ZERO && w != null && c != null && q != null && q >= 0

    FullScreenForm(
        onDismissRequest = onClose,
        title = { Text(tr("Add item", "Ongeza bidhaa"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(tr("Item name", "Jina la bidhaa")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(category, { category = it }, label = { Text(tr("Category (optional)", "Aina (si lazima)")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (categories.isNotEmpty()) androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { c -> FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) }) }
                }
                Text(tr("Sold by", "Inauzwa kwa"), color = TextSecondary)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(units) { u -> FilterChip(selected = unit == u, onClick = { unit = u }, label = { Text(u) }) }
                }
                NumberField(retail, { retail = it }, tr("Selling price (KES)", "Bei ya kuuza (KES)"))
                NumberField(wholesale, { wholesale = it }, tr("Wholesale price (KES), optional", "Bei ya jumla (KES), si lazima"))
                NumberField(cost, { cost = it }, tr("Buying price (KES), optional", "Bei ya kununua (KES), si lazima"))
                NumberField(qty, { qty = it }, tr("Stock you have now", "Akiba uliyonayo sasa"), decimal = false)
                NumberField(low, { low = it }, tr("Warn me when stock is at or below", "Nionye akiba ikiwa chini ya"), decimal = false)
            }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(tr("Cancel", "Ghairi")) } },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                viewModel.addItemToInventory(
                    name = name.trim(), retail = r!!,
                    wholesale = if (w!! > Money.ZERO) w else r,
                    stock = q!!, minStockLevel = low.toIntOrNull() ?: 3, cost = c!!,
                    category = category.trim().ifBlank { null }, unit = unit
                )
                onClose()
            }) { Text(tr("Save", "Hifadhi")) }
        }
    )
}

@Composable
private fun BuyStockForm(item: ItemEntity, viewModel: PosViewModel, onClose: () -> Unit) {
    var qty by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf(Money(item.costPriceCents).let { if (it > Money.ZERO) (it.cents / 100.0).toString().removeSuffix(".0") else "" }) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    val q = qty.trim().toLongOrNull()
    val c = money(cost)
    FullScreenForm(
        onDismissRequest = onClose,
        title = { Text(tr("Buy stock: ${item.name}", "Nunua akiba: ${item.name}"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${tr("In stock now", "Akiba sasa")}: ${item.stockQty} ${item.unit}", color = TextMuted)
                NumberField(qty, { qty = it }, tr("How many did you buy", "Umenunua ngapi"), decimal = false)
                NumberField(cost, { cost = it }, tr("Buying price each (KES)", "Bei ya kununua kila moja (KES)"))
                Text(tr("How did you pay?", "Ulilipa vipi?"), color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        PaymentMethod.CASH to tr("Cash", "Taslimu"),
                        PaymentMethod.MPESA to "M-Pesa",
                        PaymentMethod.CREDIT to tr("On credit", "Deni")
                    ).forEach { (m, label) ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(label) })
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onClose) { Text(tr("Cancel", "Ghairi")) } },
        confirmButton = {
            TextButton(enabled = q != null && q > 0 && c != null, onClick = {
                viewModel.receiveStock(item.id, q!!, c!!, method) { }
                onClose()
            }) { Text(tr("Save", "Hifadhi")) }
        }
    )
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, decimal: Boolean = true) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
