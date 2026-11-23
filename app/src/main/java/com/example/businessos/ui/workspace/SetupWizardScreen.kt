package com.example.businessos.ui.workspace

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.businessos.data.model.Money
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager
import com.example.businessos.util.BusinessKind
import com.example.businessos.util.BusinessKindStore
import com.example.businessos.util.CompanyLogo

/** Three steps: name and modules, opening balances (optional), what to do next. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    viewModel: WorkspaceViewModel,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var useSales by rememberSaveable { mutableStateOf(true) }
    var useJobOrders by rememberSaveable { mutableStateOf(false) }
    var kind by rememberSaveable { mutableStateOf(BusinessKind.GENERAL) }
    var logoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val logoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) logoUri = uri.toString() }
    var cashText by rememberSaveable { mutableStateOf("") }
    var mpesaText by rememberSaveable { mutableStateOf("") }
    var created by rememberSaveable { mutableStateOf(false) }
    val error by viewModel.errorMessage.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()

    val cash = parseShillings(cashText)
    val mpesa = parseShillings(mpesaText)
    val balancesValid = cash != null && mpesa != null
    val modulesValid = useSales || useJobOrders

    BackHandler(enabled = !created) {
        if (step > 0) step-- else onCancel()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        AppLanguageManager.tr("New company", "Kampuni mpya") + "  (${step + 1}/3)",
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    if (!created) {
                        IconButton(onClick = { if (step > 0) step-- else onCancel() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (step) {
                0 -> {
                    Text(
                        AppLanguageManager.tr("What is your company called?", "Kampuni yako inaitwaje?"),
                        style = MaterialTheme.typography.titleMedium, color = TextPrimary
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(AppLanguageManager.tr("Company name", "Jina la kampuni")) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    Text(
                        AppLanguageManager.tr("What kind of business is it?", "Biashara yako ni ya aina gani?"),
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary
                    )
                    ModuleChoiceRow(
                        title = AppLanguageManager.tr("Auto garage", "Gereji"),
                        description = AppLanguageManager.tr("Repairs, spare parts, job orders", "Ukarabati, vipuri, kazi za wateja"),
                        checked = kind == BusinessKind.GARAGE,
                        onCheckedChange = { kind = BusinessKind.GARAGE; useSales = true; useJobOrders = true }
                    )
                    ModuleChoiceRow(
                        title = AppLanguageManager.tr("Kariakor stall (leather)", "Kibanda cha Kariakor (ngozi)"),
                        description = AppLanguageManager.tr("Leather, soles and goods; sell and make to order", "Ngozi, soli na bidhaa; uza na tengeneza kwa oda"),
                        checked = kind == BusinessKind.STALL,
                        onCheckedChange = { kind = BusinessKind.STALL; useSales = true; useJobOrders = true }
                    )
                    OutlinedButton(
                        onClick = { logoPicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(if (logoUri == null) AppLanguageManager.tr("Add your logo (optional)", "Ongeza nembo yako (si lazima)") else AppLanguageManager.tr("Logo chosen. Tap to change", "Nembo imechaguliwa. Gusa kubadilisha")) }
                    Text(
                        AppLanguageManager.tr("What will you use? Pick one or both.", "Utatumia nini? Chagua kimoja au vyote."),
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary
                    )
                    ModuleChoiceRow(
                        title = AppLanguageManager.tr("Sales", "Mauzo"),
                        description = AppLanguageManager.tr("Sell items and keep stock", "Uza bidhaa na utunze akiba"),
                        checked = useSales,
                        onCheckedChange = { useSales = it }
                    )
                    ModuleChoiceRow(
                        title = AppLanguageManager.tr("Job orders", "Kazi za wateja"),
                        description = AppLanguageManager.tr(
                            "Repairs, orders for customers, or making goods for sale",
                            "Ukarabati, oda za wateja, au kutengeneza bidhaa za kuuza"
                        ),
                        checked = useJobOrders,
                        onCheckedChange = { useJobOrders = it }
                    )
                    Text(
                        AppLanguageManager.tr("You can change this later in Settings.", "Unaweza kubadilisha baadaye kwenye Mipangilio."),
                        style = MaterialTheme.typography.bodyMedium, color = TextMuted
                    )
                    Button(
                        onClick = { step = 1 },
                        enabled = name.isNotBlank() && modulesValid,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                    ) { Text(AppLanguageManager.tr("Next", "Endelea")) }
                }

                1 -> {
                    Text(
                        AppLanguageManager.tr("How much money do you have today?", "Una pesa ngapi leo?"),
                        style = MaterialTheme.typography.titleMedium, color = TextPrimary
                    )
                    Text(
                        AppLanguageManager.tr(
                            "Optional. Leave empty to start from zero. You can add stock and customers later.",
                            "Si lazima. Acha wazi uanze na sifuri. Unaweza kuongeza bidhaa na wateja baadaye."
                        ),
                        style = MaterialTheme.typography.bodyMedium, color = TextSecondary
                    )
                    OutlinedTextField(
                        value = cashText,
                        onValueChange = { cashText = it },
                        label = { Text(AppLanguageManager.tr("Cash in hand (KES)", "Pesa taslimu (KES)")) },
                        singleLine = true,
                        isError = cash == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = mpesaText,
                        onValueChange = { mpesaText = it },
                        label = { Text(AppLanguageManager.tr("M-Pesa balance (KES)", "Salio la M-Pesa (KES)")) },
                        singleLine = true,
                        isError = mpesa == null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors()
                    )
                    error?.let { Text(it, color = StatusRed, style = MaterialTheme.typography.bodyMedium) }
                    Button(
                        onClick = {
                            viewModel.createBusiness(
                                name = name,
                                isShopEnabled = useSales,
                                isGarageEnabled = useJobOrders,
                                openingCash = cash ?: Money.ZERO,
                                openingMpesa = mpesa ?: Money.ZERO
                            ) { newId ->
                                BusinessKindStore.set(context, newId, kind)
                                logoUri?.let { CompanyLogo.save(context, newId, android.net.Uri.parse(it)) }
                                created = true
                                step = 2
                            }
                        },
                        enabled = balancesValid && !isCreating,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                    ) { Text(AppLanguageManager.tr("Create company", "Fungua kampuni")) }
                    OutlinedButton(
                        onClick = { cashText = ""; mpesaText = "" },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) { Text(AppLanguageManager.tr("Start from zero", "Anza na sifuri")) }
                }

                else -> {
                    Text(
                        AppLanguageManager.tr("Your company is ready", "Kampuni yako iko tayari"),
                        style = MaterialTheme.typography.titleLarge, color = TextPrimary
                    )
                    Text(
                        AppLanguageManager.tr("Next steps, in any order:", "Hatua zinazofuata, kwa mpangilio wowote:"),
                        style = MaterialTheme.typography.bodyLarge, color = TextSecondary
                    )
                    val steps = buildList {
                        if (useSales) {
                            add(AppLanguageManager.tr("Add your items and stock", "Ongeza bidhaa na akiba yako"))
                            add(AppLanguageManager.tr("Make your first sale", "Fanya mauzo ya kwanza"))
                        }
                        if (useJobOrders) add(AppLanguageManager.tr("Create your first job order", "Unda kazi ya kwanza"))
                        add(AppLanguageManager.tr("Add your staff under Users", "Ongeza wafanyakazi kwenye Watumiaji"))
                    }
                    steps.forEachIndexed { i, t ->
                        Row { Text("${i + 1}.  $t", style = MaterialTheme.typography.bodyLarge, color = TextPrimary) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
                    ) { Text(AppLanguageManager.tr("Open company", "Fungua kampuni")) }
                }
            }
        }
    }
}

/** Blank is zero. Returns null when the text is not a valid non-negative amount. */
internal fun parseShillings(text: String): Money? {
    val t = text.trim().replace(",", "")
    if (t.isEmpty()) return Money.ZERO
    val d = t.toDoubleOrNull() ?: return null
    if (d < 0 || d.isNaN() || d.isInfinite()) return null
    return Money.fromShillingsDouble(d)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AmberAccent,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = AmberAccent,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)
