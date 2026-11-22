package com.example.businessos.ui.manual

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.ui.components.BusinessOsTopBar

@Composable
fun UserManualScreen(
    businessName: String?,
    onSwitchWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF131824)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                BusinessOsTopBar(
                    title = "Manual & User Guide",
                    businessName = businessName,
                    role = null,
                    onSwitchWorkspace = onSwitchWorkspace,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ManualHeaderCard()
            }

            item {
                ManualSectionCard(
                    title = "1. Getting Started & Multi-Tenancy",
                    icon = Icons.Filled.Store,
                    accentColor = Color(0xFF3B82F6),
                    content = listOf(
                        "• Workspaces: You can own multiple businesses (Auto Garages and Kariakor Stalls) under one login.",
                        "• Workspace Switcher: Tap the switch icon in the top right to jump between your shops.",
                        "• Data Isolation: All inventory, job cards, audit logs, and sales belong strictly to the active business."
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = "2. Auto Garage: Bodi, Rangi, Viti & Injini",
                    icon = Icons.Filled.Build,
                    accentColor = Color(0xFFF59E0B),
                    content = listOf(
                        "• Ukaguzi wa Sehemu za Gari: Bodi (bonye na bumper), Rangi (respray & buffing), Viti vya Ngozi (kushona & dari), Matairi (burst, rims, alignment) na Injini/Breki.",
                        "• Kadi za Kazi (Job Pipeline): Hatua 7 kutoka Intake ➔ Diagnosis ➔ Makadirio ➔ Idhini ya Mteja ➔ Kazi Inaendelea ➔ Imekamilika ➔ Ankara.",
                        "• Idhini Kupitia WhatsApp: Tuma makadirio kamili yenye bei ya vipuri na ufundi moja kwa moja kwa WhatsApp ya mteja ili apitishe kabla ya kuanza kazi.",
                        "• Historia ya Gari kwa Namba ya Usajili: Tafuta namba yoyote (mfano KDA 123X) kuona marekebisho yaliyopita na fedha alizotumia."
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = "3. Kariakor POS, Picha za Bidhaa & Deni",
                    icon = Icons.Filled.PointOfSale,
                    accentColor = Color(0xFF10B981),
                    content = listOf(
                        "• Picha za Bidhaa Dukani: Kila bidhaa ina nembo ya kiolezo (viatu, mikanda, jaketi za ngozi, mafuta ya gari) au picha maalum inayohifadhiwa ndani ya app bila kujaza ghala ya simu yako.",
                        "• Bei Mbili (Dual Pricing): Badilisha kirahisi kati ya bei ya Rejareja (Retail) na bei ya Jumla (Wholesale) kwa kila mteja.",
                        "• Bandika SMS ya M-Pesa: Unapochagua malipo ya M-Pesa, bandika ujumbe wa SMS ya Safaricom na mfumo utachomoa kodi ya muamala mara moja bila kuandika kwa mkono.",
                        "• Daftari la Deni: Fuatilia wateja wanaodaiwa na bonyeza aikoni ya WhatsApp kutuma ujumbe wa kikumbusho cha kirafiki chenye salio lao."
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = "4. Who Can Do What (Roles)",
                    icon = Icons.Filled.Security,
                    accentColor = Color(0xFF8B5CF6),
                    content = listOf(
                        "• Owner: Full access. Manages staff, business settings, backups and the audit log.",
                        "• Manager: Sells, edits stock, records expenses, pays wages and voids records (a reason is required). Cannot manage staff or settings.",
                        "• Cashier: Records sales, takes payments, adds customers and opens job cards. Cannot change prices, edit stock or void records.",
                        "• Viewer: Read-only access to reports. Cannot record anything."
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = "5. Works Without Internet",
                    icon = Icons.Filled.Storage,
                    accentColor = Color(0xFF06B6D4),
                    content = listOf(
                        "• All records are saved on this phone. The app never needs internet to work.",
                        "• Nothing is uploaded anywhere. Use Backup in the More tab to keep a copy of your records.",
                        "• Mistakes are corrected by voiding the record, never by deleting it, so your books always add up."
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = "6. Usalama wa Biashara & Kufunga Siku",
                    icon = Icons.Filled.Shield,
                    accentColor = Color(0xFFEF4444),
                    content = listOf(
                        "• Daftari la Usalama (Immutable Audit Log): Mabadiliko ya bei, kufuta mauzo, kurekebisha hisa na mabadiliko ya wafanyakazi yanarekodiwa kisheria bila uwezekano wa kufutwa.",
                        "• Kufunga Daftari la Siku (Daily Cash-Up): Linganisha pesa taslimu zilizopo kwenye droo na salio la M-Pesa dhidi ya mauzo ya mfumo ili kugundua upungufu au ziada mwisho wa siku.",
                        "• Tuma Ripoti WhatsApp: Tuma muhtasari wa siku mzima kwa WhatsApp kwa wamiliki au washirika kwa mbofyo mmoja."
                    )
                )
            }
        }
    }
}

@Composable
private fun ManualHeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2536))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.MenuBook,
                    contentDescription = null,
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Business OS Manual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Complete Field Guide & Operating Procedures",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualSectionCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(Modifier.height(12.dp))

            content.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}
