package com.example.businessos.ui.manual

import com.example.businessos.ui.theme.DarkSurfaceElevated
import com.example.businessos.ui.util.AppLanguageManager
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.TextPrimary
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
                modifier = Modifier.fillMaxWidth().background(DarkBackground),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = AppLanguageManager.tr("Back", "Rudi"), tint = TextPrimary)
                }
                BusinessOsTopBar(
                    title = AppLanguageManager.tr("User guide", "Mwongozo"),
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
                    title = AppLanguageManager.tr("1. Companies", "1. Kampuni"),
                    icon = Icons.Filled.Store,
                    accentColor = Color(0xFF3B82F6),
                    content = listOf(
                        AppLanguageManager.tr("Each company is one kind of business: an auto garage or a Kariakor leather stall. Its features and its data belong only to it.", "Kila kampuni ni aina moja ya biashara: gereji au kibanda cha ngozi cha Kariakor. Kazi zake na data yake ni zake peke yake."),
                        AppLanguageManager.tr("To run both kinds, add a second company under the same login. The two never share items, customers, sales or money.", "Kuendesha aina zote mbili, ongeza kampuni ya pili kwa login ile ile. Hazishiriki bidhaa, wateja, mauzo wala pesa."),
                        AppLanguageManager.tr("Open More, then Companies, to switch or add a company. Open Company profile to change the name or add your logo.", "Fungua Zaidi, kisha Kampuni, kubadilisha au kuongeza kampuni. Fungua Wasifu wa kampuni kubadilisha jina au kuweka nembo.")
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = AppLanguageManager.tr("2. Auto garage", "2. Gereji"),
                    icon = Icons.Filled.Build,
                    accentColor = Color(0xFFF59E0B),
                    content = listOf(
                        AppLanguageManager.tr("Job orders follow a vehicle from intake, diagnosis and estimate, to approval, work in progress, done, and invoiced.", "Kazi hufuata gari kutoka kupokelewa, ukaguzi na makadirio, idhini, kazi inaendelea, imekamilika, hadi ankara."),
                        AppLanguageManager.tr("Send the estimate to the customer on WhatsApp so they can approve before work starts.", "Tuma makadirio kwa mteja kwa WhatsApp ili aidhinishe kabla kazi kuanza."),
                        AppLanguageManager.tr("Spare parts are kept under Spare parts. Search a plate under Vehicle history to see past repairs.", "Vipuri vinawekwa chini ya Vipuri. Tafuta namba ya gari kwenye Historia ya gari kuona marekebisho ya awali.")
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = AppLanguageManager.tr("3. Kariakor leather stall", "3. Kibanda cha ngozi cha Kariakor"),
                    icon = Icons.Filled.PointOfSale,
                    accentColor = Color(0xFF10B981),
                    content = listOf(
                        AppLanguageManager.tr("Keep leather, soles, thread, glue and finished goods under Stock. Pick a category and how it is sold, for example pair or sq ft.", "Weka ngozi, soli, uzi, gundi na bidhaa zilizokamilika kwenye Akiba. Chagua aina na jinsi inavyouzwa, mfano pair au sq ft."),
                        AppLanguageManager.tr("Sell with two prices, retail and wholesale. Pay by cash, M-Pesa or on credit. Paste the M-Pesa SMS and the code is filled in for you.", "Uza kwa bei mbili, rejareja na jumla. Lipa kwa taslimu, M-Pesa au deni. Bandika SMS ya M-Pesa na msimbo hujazwa."),
                        AppLanguageManager.tr("Orders are for a customer, or to make goods for stock. Layaway lets a customer pay in instalments.", "Oda ni za mteja, au za kutengeneza bidhaa za akiba. Awali humruhusu mteja kulipa kidogo kidogo.")
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = AppLanguageManager.tr("4. Customers, payments and receipts", "4. Wateja, malipo na risiti"),
                    icon = Icons.Filled.People,
                    accentColor = Color(0xFF06B6D4),
                    content = listOf(
                        AppLanguageManager.tr("Customers who owe you are under Customers and payments. Send a reminder on WhatsApp in one tap.", "Wateja wanaodaiwa wako kwenye Wateja na malipo. Tuma kikumbusho kwa WhatsApp kwa mbofyo mmoja."),
                        AppLanguageManager.tr("Every sale and every payment you take has a receipt. Find it under Receipts and share it.", "Kila mauzo na kila malipo unayopokea yana risiti. Itafute kwenye Risiti na uitume.")
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = AppLanguageManager.tr("5. Who can do what", "5. Nani anaweza kufanya nini"),
                    icon = Icons.Filled.Security,
                    accentColor = Color(0xFF8B5CF6),
                    content = listOf(
                        AppLanguageManager.tr("Owner: full access. Manages staff, settings, backups and the activity log.", "Mmiliki: ruhusa zote. Anasimamia wafanyakazi, mipangilio, nakala na kumbukumbu."),
                        AppLanguageManager.tr("Manager: sells, edits stock, records expenses, pays wages and voids records with a reason. Cannot manage staff or settings.", "Meneja: anauza, anahariri akiba, anarekodi matumizi, analipa mishahara na kufuta rekodi kwa sababu. Hawezi kusimamia wafanyakazi au mipangilio."),
                        AppLanguageManager.tr("Cashier: records sales, takes payments, adds customers and opens orders. Cannot change prices, edit stock or void records.", "Mhasibu wa mauzo: anarekodi mauzo, anapokea malipo, anaongeza wateja na kufungua oda. Hawezi kubadilisha bei, kuhariri akiba wala kufuta rekodi."),
                        AppLanguageManager.tr("Viewer: can only read reports.", "Mtazamaji: anaweza kusoma ripoti tu.")
                    )
                )
            }

            item {
                ManualSectionCard(
                    title = AppLanguageManager.tr("6. Safe and works without internet", "6. Salama na inafanya kazi bila intaneti"),
                    icon = Icons.Filled.Storage,
                    accentColor = Color(0xFFEF4444),
                    content = listOf(
                        AppLanguageManager.tr("Everything is saved on this phone. The app never needs internet and nothing is uploaded.", "Kila kitu kinahifadhiwa kwenye simu hii. Programu haihitaji intaneti na hakuna kinachopakiwa."),
                        AppLanguageManager.tr("Open a PIN-locked app with your 6-digit PIN. The app locks itself after the time you choose in Security.", "Fungua programu kwa PIN yako ya tarakimu 6. Programu hujifunga yenyewe baada ya muda uliochagua kwenye Usalama."),
                        AppLanguageManager.tr("Take a Backup often and keep the file somewhere safe. Restore checks the file first and keeps your current data if it fails.", "Chukua Nakala mara kwa mara na uweke faili mahali salama. Kurudisha hukagua faili kwanza na kuweka data yako ya sasa ikishindikana."),
                        AppLanguageManager.tr("Mistakes are fixed by voiding a record with a reason, never by deleting it, so your books always add up.", "Makosa hurekebishwa kwa kufuta rekodi kwa sababu, si kuiondoa, ili hesabu zako zilingane kila wakati.")
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
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
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
                        text = AppLanguageManager.tr("BusinessOS guide", "Mwongozo wa BusinessOS"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = AppLanguageManager.tr("How to use the app, step by step", "Jinsi ya kutumia programu, hatua kwa hatua"),
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
