package com.example.businessos.ui.workspace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.businessos.ui.components.BusinessLogo
import com.example.businessos.ui.home.HomeViewModel
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.theme.ThemeManager
import com.example.businessos.ui.util.AppLanguageManager.tr
import com.example.businessos.util.CompanyLogo

/** Company name, contact details and logo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(viewModel: HomeViewModel, onBack: () -> Unit) {
    val business by viewModel.currentBusiness.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var footer by remember { mutableStateOf("") }
    var loaded by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(business?.id) {
        business?.let {
            if (!loaded) { name = it.name; phone = it.phone; address = it.address; footer = it.receiptFooter; loaded = true }
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val id = business?.id
        if (uri != null && id != null) {
            note = if (CompanyLogo.save(context, id, uri)) tr("Logo saved", "Nembo imehifadhiwa") else tr("Could not read that picture", "Picha hiyo haikusomeka")
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(tr("Company profile", "Wasifu wa kampuni"), color = TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BusinessLogo(business?.id, size = 88.dp)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { picker.launch("image/*") }) { Text(tr("Choose logo", "Chagua nembo")) }
                    if (CompanyLogo.version.intValue >= 0 && business?.id != null && CompanyLogo.load(context, business!!.id) != null) {
                        OutlinedButton(onClick = { CompanyLogo.remove(context, business!!.id) }) { Text(tr("Remove logo", "Ondoa nembo")) }
                    }
                }
            }
            if (business?.id != null && CompanyLogo.version.intValue >= 0) {
                CompanyLogo.load(context, business!!.id)?.let { bmp ->
                    OutlinedButton(
                        onClick = { ThemeManager.setAccent(context, CompanyLogo.suggestAccent(bmp)); note = tr("Colour matched to your logo", "Rangi imelingana na nembo yako") },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(tr("Match app colour to logo", "Lingana rangi ya programu na nembo")) }
                }
            }
            OutlinedTextField(name, { name = it }, label = { Text(tr("Company name", "Jina la kampuni")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone, { phone = it }, label = { Text(tr("Phone number", "Nambari ya simu")) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(address, { address = it }, label = { Text(tr("Address or stall number", "Anwani au namba ya kibanda")) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(footer, { footer = it }, label = { Text(tr("Message at the bottom of receipts", "Ujumbe chini ya risiti")) }, modifier = Modifier.fillMaxWidth())
            (message ?: note)?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) }
            Button(
                onClick = { viewModel.saveProfile(name, phone, address, footer) { note = tr("Saved", "Imehifadhiwa") } },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) { Text(tr("Save", "Hifadhi")) }
            Text(tr("The logo stays on this phone. It is not part of the backup file.", "Nembo inabaki kwenye simu hii. Haimo kwenye faili la nakala."), color = TextMuted)
        }
    }
}
