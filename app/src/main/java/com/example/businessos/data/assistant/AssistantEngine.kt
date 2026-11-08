package com.example.businessos.data.assistant

import com.example.businessos.data.local.entity.AccountEntity
import com.example.businessos.data.local.entity.BillEntity
import com.example.businessos.data.local.entity.CustomerEntity
import com.example.businessos.data.local.entity.ExpenseEntity
import com.example.businessos.data.local.entity.InvoiceEntity
import com.example.businessos.data.local.entity.JobCardEntity
import com.example.businessos.data.local.entity.SaleEntity
import com.example.businessos.data.model.AccountCategory
import com.example.businessos.data.model.BillStatus
import com.example.businessos.data.model.JobStage
import com.example.businessos.ui.navigation.Screen
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AssistantEngine {

    private val gson = Gson()

    fun buildSnapshot(
        businessName: String,
        businessType: String,
        sales: List<SaleEntity>,
        expenses: List<ExpenseEntity>,
        customers: List<CustomerEntity>,
        invoices: List<InvoiceEntity>,
        bills: List<BillEntity>,
        accounts: List<AccountEntity>,
        jobCards: List<JobCardEntity>
    ): BusinessSnapshot {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis

        val salesToday = sales.filter { it.createdAt >= startOfToday }.sumOf { it.total }
        val salesAllTime = sales.sumOf { it.total }
        val totalExp = expenses.sumOf { it.amount }
        val netProf = salesAllTime - totalExp
        val margin = if (salesAllTime > 0) (netProf / salesAllTime) * 100.0 else 0.0

        val totalDeni = customers.sumOf { it.balance }
        val debtors = customers.filter { it.balance > 0 }
            .sortedByDescending { it.balance }
            .map {
                DebtorSummary(
                    name = it.name,
                    phone = it.phone,
                    balance = it.balance,
                    daysOverdue = computeDaysOverdue(it.updatedAt)
                )
            }

        val cashOnHand = accounts.filter { it.category == AccountCategory.ASSET && it.code.startsWith("101") }
            .sumOf { it.balance }
        val bankMpesa = accounts.filter { it.category == AccountCategory.ASSET && (it.code.startsWith("102") || it.code.startsWith("103")) }
            .sumOf { it.balance }

        val unpaidBills = bills.filter { it.status == BillStatus.UNPAID || it.status == BillStatus.PARTIALLY_PAID }
        val totalBills = unpaidBills.sumOf { it.balanceDue }
        val overdueBills = unpaidBills.count { it.dueDate < System.currentTimeMillis() }

        val activeJobs = jobCards.count { it.stage != JobStage.INVOICED }

        return BusinessSnapshot(
            businessName = businessName,
            businessType = businessType,
            totalSalesToday = salesToday,
            totalSalesAllTime = salesAllTime,
            totalExpenses = totalExp,
            netProfit = netProf,
            profitMarginPct = margin,
            totalDeniReceivable = totalDeni,
            debtorCount = debtors.size,
            topDebtors = debtors,
            cashOnHand = cashOnHand,
            bankMpesaBalance = bankMpesa,
            totalBillsPayable = totalBills,
            overdueBillsCount = overdueBills,
            activeJobsCount = activeJobs,
            inventoryCount = sales.size
        )
    }

    private fun computeDaysOverdue(timestamp: Long): Int {
        val diff = System.currentTimeMillis() - timestamp
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        return if (days > 0) days else 1
    }

    suspend fun processQuery(
        query: String,
        snapshot: BusinessSnapshot,
        geminiApiKey: String? = null
    ): AssistantMessage {
        val trimmed = query.trim()
        val lower = trimmed.lowercase(Locale.getDefault())

        // 1. Check for Action Assistant intents
        val actionMessage = tryDetectActionIntent(trimmed, lower, snapshot)
        if (actionMessage != null) return actionMessage

        // 2. Check for Live Business Data Assistant intents
        val dataMessage = tryDetectDataIntent(lower, snapshot)
        if (dataMessage != null) return dataMessage

        // 3. Check for Business Analyst intents
        val analystMessage = tryDetectAnalystIntent(lower, snapshot)
        if (analystMessage != null) return analystMessage

        // 4. Check for App Guidance intents
        val guideMessage = tryDetectGuideIntent(lower)
        if (guideMessage != null) return guideMessage

        // 5. External Knowledge (KRA, Taxes, Market info, or General AI query)
        val externalMessage = tryDetectExternalOrAi(trimmed, lower, snapshot, geminiApiKey)
        return externalMessage
    }

    // ==========================================
    // ACTION ASSISTANT INTENTS
    // ==========================================
    private fun tryDetectActionIntent(
        raw: String,
        lower: String,
        snapshot: BusinessSnapshot
    ): AssistantMessage? {
        // Record Expense intent: e.g. "rekodi matumizi 500 chakula" or "record expense 1000 parts"
        val expenseRegex = Regex("""(?:rekodi matumizi|record expense|ongeza matumizi|matumizi ya)\s*(?:ya|of)?\s*(?:ksh\.?|kes)?\s*([0-9]+(?:[.,][0-9]{1,2})?)\s*(?:kwa|kwa ajili ya|for)?\s*(.*)""", RegexOption.IGNORE_CASE)
        val expenseMatch = expenseRegex.find(raw)
        if (expenseMatch != null) {
            val amountStr = expenseMatch.groupValues[1].replace(",", "")
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val rawDesc = expenseMatch.groupValues[2].trim()
            val desc = if (rawDesc.isNotBlank()) rawDesc else "Matumizi ya Biashara"
            val category = guessExpenseCategory(desc)

            return AssistantMessage(
                text = """
                    ✅ **Tayari Kuthibitisha Matumizi (Stage Expense):**
                    
                    • **Kiasi:** KSh ${String.format("%,.2f", amount)}
                    • **Kitengo:** $category
                    • **Maelezo:** $desc
                    
                    Bonyeza kitufe hapa chini ili kurekodi matumizi haya moja kwa moja kwenye Daftari la Hesabu (Cashbook).
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.ACTION,
                actionPayload = AssistantActionPayload.StageExpense(
                    amount = amount,
                    category = category,
                    description = desc
                )
            )
        }

        // Send WhatsApp Reminder intent: e.g. "tuma whatsapp kwa hassan" or "remind hassan"
        if (lower.contains("whatsapp") || lower.contains("kumbusha") || lower.contains("remind") || lower.contains("dai")) {
            val debtor = snapshot.topDebtors.firstOrNull { debtor ->
                lower.contains(debtor.name.lowercase(Locale.getDefault()))
            } ?: snapshot.topDebtors.firstOrNull()

            if (debtor != null && debtor.balance > 0) {
                return AssistantMessage(
                    text = """
                        📲 **Kikumbusho cha Deni Kupitia WhatsApp (Deni Reminder):**
                        
                        • **Mteja:** ${debtor.name}
                        • **Nambari ya Simu:** ${debtor.phone}
                        • **Salio la Deni:** KSh ${String.format("%,.0f", debtor.balance)}
                        
                        Ujumbe wa kirafiki wa Kiswahili na Kiingereza uko tayari kutumwa kwenye WhatsApp yake. Bonyeza kitufe hapa chini ili kufungua WhatsApp:
                    """.trimIndent(),
                    isUser = false,
                    category = AssistantCategory.ACTION,
                    actionPayload = AssistantActionPayload.WhatsAppReminder(
                        customerName = debtor.name,
                        customerPhone = debtor.phone,
                        balance = debtor.balance
                    )
                )
            } else if (snapshot.topDebtors.isEmpty()) {
                return AssistantMessage(
                    text = "Hongera sana! Hakuna mteja anayekudai kwa sasa. Rekodi zote za Deni ziko safi.",
                    isUser = false,
                    category = AssistantCategory.ACTION
                )
            }
        }

        // Navigation shortcuts: "fungua hesabu", "fungua pos", "fungua job cards"
        if (lower.contains("fungua") || lower.contains("open") || lower.contains("nenda") || lower.contains("goto")) {
            when {
                lower.contains("hesabu") || lower.contains("cashbook") || lower.contains("daftari") || lower.contains("accounting") -> {
                    return AssistantMessage(
                        text = "Fungua Daftari la Hesabu, Ankara, na Ripoti za Faida na Hasara:",
                        isUser = false,
                        category = AssistantCategory.ACTION,
                        actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Fungua Daftari ya Hesabu")
                    )
                }
                lower.contains("pos") || lower.contains("mauzo") || lower.contains("duka") || lower.contains("stall") -> {
                    return AssistantMessage(
                        text = "Fungua mfumo wa Mauzo ya Haraka (POS) wa dukani:",
                        isUser = false,
                        category = AssistantCategory.ACTION,
                        actionPayload = AssistantActionPayload.Navigate(Screen.Pos.route, "Fungua Kariakor POS")
                    )
                }
                lower.contains("deni") || lower.contains("madeni") || lower.contains("debtors") -> {
                    return AssistantMessage(
                        text = "Fungua Daftari la Madeni ya Wateja (Deni Ledger):",
                        isUser = false,
                        category = AssistantCategory.ACTION,
                        actionPayload = AssistantActionPayload.Navigate(Screen.DeniLedger.route, "Fungua Daftari la Deni")
                    )
                }
                lower.contains("gari") || lower.contains("garage") || lower.contains("job") || lower.contains("karakana") -> {
                    return AssistantMessage(
                        text = "Fungua Kadi za Kazi na Ukaguzi wa Karakana (Garage Jobs):",
                        isUser = false,
                        category = AssistantCategory.ACTION,
                        actionPayload = AssistantActionPayload.Navigate(Screen.GarageJobs.route, "Fungua Karakana ya Magari")
                    )
                }
            }
        }

        return null
    }

    private fun guessExpenseCategory(desc: String): String {
        val lower = desc.lowercase(Locale.getDefault())
        return when {
            lower.contains("kodi") || lower.contains("rent") -> "Rent"
            lower.contains("stima") || lower.contains("token") || lower.contains("power") || lower.contains("maji") || lower.contains("water") -> "Utilities"
            lower.contains("chakula") || lower.contains("lunch") || lower.contains("tea") || lower.contains("chai") -> "Meals & Refreshments"
            lower.contains("spare") || lower.contains("oil") || lower.contains("part") || lower.contains("ngozi") || lower.contains("leather") -> "Supplies & Parts"
            lower.contains("mshahara") || lower.contains("wages") || lower.contains("staff") || lower.contains("kibarua") -> "Wages"
            lower.contains("fare") || lower.contains("fuel") || lower.contains("boda") || lower.contains("petrol") -> "Transport"
            else -> "Operating Expense"
        }
    }

    // ==========================================
    // BUSINESS DATA ASSISTANT INTENTS
    // ==========================================
    private fun tryDetectDataIntent(lower: String, snapshot: BusinessSnapshot): AssistantMessage? {
        // Sales queries: "mauzo ya leo", "total sales", "mauzo ni ngapi"
        if (lower.contains("mauzo") || lower.contains("sales") || lower.contains("umeuza") || lower.contains("revenue")) {
            val isToday = lower.contains("leo") || lower.contains("today")
            val amount = if (isToday) snapshot.totalSalesToday else snapshot.totalSalesAllTime
            val title = if (isToday) "Mauzo ya Leo (Today's Sales)" else "Jumla ya Mauzo Yote (Total Sales)"

            return AssistantMessage(
                text = """
                    📊 **$title:**
                    
                    • **Kiasi:** **KSh ${String.format("%,.2f", amount)}**
                    • **Faida Halisi (Net Profit):** KSh ${String.format("%,.2f", snapshot.netProfit)} (${String.format("%.1f", snapshot.profitMarginPct)}% margin)
                    • **Madeni Yasiyolipwa (Deni):** KSh ${String.format("%,.2f", snapshot.totalDeniReceivable)}
                    
                    Kumbuka kurekodi kila mauzo ya M-Pesa na Pesa Taslimu (Cash) ili hesabu zako zilingane mwisho wa siku.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Tazama Daftari la Hesabu")
            )
        }

        // Debtors / Deni queries: "nani ananidai", "wateja wenye deni", "who owes me", "deni ni ngapi"
        if (lower.contains("nani ananidai") || lower.contains("nani anadaiwa") || lower.contains("who owes") || (lower.contains("deni") && !lower.contains("jinsi"))) {
            if (snapshot.topDebtors.isEmpty()) {
                return AssistantMessage(
                    text = """
                        🎉 **Hali ya Madeni (Deni Status):**
                        
                        Hakuna mteja yeyote mwenye deni linalosubiriwa kwa sasa! Jumla ya Deni ni **KSh 0.00**.
                    """.trimIndent(),
                    isUser = false,
                    category = AssistantCategory.BUSINESS_DATA
                )
            }

            val top3 = snapshot.topDebtors.take(3)
            val debtorList = top3.joinToString("\n") {
                "• **${it.name}**: KSh ${String.format("%,.0f", it.balance)} (${it.daysOverdue} siku zimepita)"
            }

            val topDebtor = snapshot.topDebtors.first()

            return AssistantMessage(
                text = """
                    📋 **Wateja Wenye Madeni (Top Debtors):**
                    
                    Jumla ya Deni nje: **KSh ${String.format("%,.2f", snapshot.totalDeniReceivable)}** (Wateja ${snapshot.debtorCount})
                    
                    $debtorList
                    
                    Je, ungependa kumtumia **${topDebtor.name}** ujumbe wa kikumbusho cha kirafiki kupitia WhatsApp sasa hivi?
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.WhatsAppReminder(
                    customerName = topDebtor.name,
                    customerPhone = topDebtor.phone,
                    balance = topDebtor.balance
                )
            )
        }

        // Profit / Faida queries
        if (lower.contains("faida") || lower.contains("profit") || lower.contains("p&l") || lower.contains("hasara")) {
            val statusEmoji = if (snapshot.netProfit >= 0) "🟢" else "🔴"
            val statusWord = if (snapshot.netProfit >= 0) "Faida Safi" else "Hasara"

            return AssistantMessage(
                text = """
                    $statusEmoji **Hali ya Faida na Hasara ($statusWord):**
                    
                    • **Jumla ya Mapato (Sales):** KSh ${String.format("%,.2f", snapshot.totalSalesAllTime)}
                    • **Jumla ya Matumizi (Expenses):** KSh ${String.format("%,.2f", snapshot.totalExpenses)}
                    • **$statusWord (Net Profit):** **KSh ${String.format("%,.2f", snapshot.netProfit)}**
                    • **Kiwango cha Faida (Margin):** ${String.format("%.1f", snapshot.profitMarginPct)}%
                    
                    ${if (snapshot.profitMarginPct >= 20.0) "Ufanisi wako ni mzuri sana! Kiwango cha faida kiko zaidi ya 20%." else "Ushauri: Chunguza matumizi yako ya kila siku ili kupandisha kiwango cha faida."}
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Fungua Ripoti ya P&L")
            )
        }

        // Cash / Bank balances
        if (lower.contains("cash") || lower.contains("taslimu") || lower.contains("bank") || lower.contains("benki") || lower.contains("salio") || lower.contains("pesa ngapi")) {
            return AssistantMessage(
                text = """
                    🏦 **Salio la Pesa (Cash & Bank Balances):**
                    
                    • **Pesa Taslimu Mkononi (Cash in Hand):** KSh ${String.format("%,.2f", snapshot.cashOnHand)}
                    • **Akaunti ya Benki / M-Pesa Till:** KSh ${String.format("%,.2f", snapshot.bankMpesaBalance)}
                    • **Jumla ya Fedha Zilizopo (Liquidity):** KSh ${String.format("%,.2f", snapshot.cashOnHand + snapshot.bankMpesaBalance)}
                    • **Madeni ya Wasambazaji (Bills Due):** KSh ${String.format("%,.2f", snapshot.totalBillsPayable)}
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Tazama Daftari la Pesa")
            )
        }

        // Supplier bills / Madeni ya wasambazaji
        if (lower.contains("wasambazaji") || lower.contains("supplier") || lower.contains("bill") || lower.contains("payable") || lower.contains("ankara ya mzigo")) {
            return AssistantMessage(
                text = """
                    📦 **Madeni ya Wasambazaji (Accounts Payable / Bills):**
                    
                    • **Jumla Inayodaiwa:** KSh ${String.format("%,.2f", snapshot.totalBillsPayable)}
                    • **Bili Zilizochelewa (Overdue):** ${snapshot.overdueBillsCount}
                    
                    Kumbuka kulipa wasambazaji wako kwa wakati ili kuweka uaminifu na bei nzuri za jumla.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Tazama Bili za Wasambazaji")
            )
        }

        // Active garage jobs
        if (lower.contains("karakana") || lower.contains("magari") || lower.contains("job card") || lower.contains("workshop")) {
            return AssistantMessage(
                text = """
                    🚗 **Hali ya Karakana (Active Garage Jobs):**
                    
                    • **Magari Yanayoendelea Kurekebishwa:** ${snapshot.activeJobsCount}
                    
                    Fungua bomba la kazi (Job Pipeline) kuona magari yaliyofanyiwa ukaguzi wa 3D au yanayosubiri vipuri.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_DATA,
                actionPayload = AssistantActionPayload.Navigate(Screen.GarageJobs.route, "Fungua Kadi za Kazi")
            )
        }

        return null
    }

    // ==========================================
    // BUSINESS ANALYST INTENTS
    // ==========================================
    private fun tryDetectAnalystIntent(lower: String, snapshot: BusinessSnapshot): AssistantMessage? {
        if (lower.contains("uchambuzi") || lower.contains("ushauri") || lower.contains("analyst") || lower.contains("analysis") || lower.contains("mwelekeo") || lower.contains("hali ya biashara") || lower.contains("ushauri wa biashara")) {
            val totalCash = snapshot.cashOnHand + snapshot.bankMpesaBalance
            val creditRiskRatio = if (snapshot.totalSalesAllTime > 0) (snapshot.totalDeniReceivable / snapshot.totalSalesAllTime) * 100.0 else 0.0

            val healthScore = when {
                snapshot.netProfit > 0 && creditRiskRatio < 20.0 && totalCash >= snapshot.totalBillsPayable -> "IMARA (Strong & Healthy 🟢)"
                snapshot.netProfit >= 0 -> "WASTANI (Fair / Average 🟡)"
                else -> "INAHITAJI TAHADHARI (Critical / High Risk 🔴)"
            }

            val tips = mutableListOf<String>()

            if (creditRiskRatio > 25.0) {
                tips.add("⚠️ **Tahadhari ya Madeni:** Asilimia ${String.format("%.1f", creditRiskRatio)}% ya mauzo yako yamefungwa kwenye Deni la wateja. Punguza kutoa mikopo na ufuatilie madeni kupitia WhatsApp mara moja.")
            } else {
                tips.add("✅ **Udhibiti wa Deni:** Kiwango chako cha madeni ya nje kiko salama (${String.format("%.1f", creditRiskRatio)}%).")
            }

            if (snapshot.profitMarginPct < 15.0 && snapshot.totalSalesAllTime > 0) {
                tips.add("💡 **Ushauri wa Faida:** Kiwango chako cha faida ni ${String.format("%.1f", snapshot.profitMarginPct)}%. Kwenye masoko kama Kariakor au Karakana za Gikomba, faida nzuri inafaa kuwa angalau 20%-35%. Chunguza upotevu wa vifaa au matumizi ya ziada.")
            } else if (snapshot.totalSalesAllTime > 0) {
                tips.add("🌟 **Ufanisi Mzuri:** Faida yako ya ${String.format("%.1f", snapshot.profitMarginPct)}% inaonyesha biashara yako ina mwelekeo mwema wa kujiendesha.")
            }

            if (totalCash < snapshot.totalBillsPayable) {
                tips.add("⚠️ **Mtiririko wa Fedha (Cash Flow):** Pesa ulizonazo (KSh ${String.format("%,.0f", totalCash)}) ni ndogo kuliko madeni ya wasambazaji (KSh ${String.format("%,.0f", snapshot.totalBillsPayable)}). Kusanya madeni ya wateja haraka kabla ya tarehe za wasambazaji.")
            }

            tips.add("📌 **Mbinu ya Soko la Kenya:** Wape wateja motisha ya kulipa kwa M-Pesa papo hapo (mfano punguzo dogo la KSh 50) ili kuondoa Deni kabisa.")

            return AssistantMessage(
                text = """
                    📈 **Uchambuzi wa Kina wa Mchambuzi wa Biashara (Business Health Audit):**
                    
                    • **Hali ya Jumla:** $healthScore
                    • **Faida Safi:** KSh ${String.format("%,.2f", snapshot.netProfit)} (Kiwango: ${String.format("%.1f", snapshot.profitMarginPct)}%)
                    • **Pesa Zilizopo:** KSh ${String.format("%,.2f", totalCash)}
                    • **Deni la Wateja:** KSh ${String.format("%,.2f", snapshot.totalDeniReceivable)}
                    
                    **Mapendekezo na Ushauri wa Kiutendaji:**
                    ${tips.joinToString("\n\n")}
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.BUSINESS_ANALYST,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Tazama Ripoti Kamili za Hesabu")
            )
        }
        return null
    }

    // ==========================================
    // APP GUIDANCE INTENTS
    // ==========================================
    private fun tryDetectGuideIntent(lower: String): AssistantMessage? {
        // POS Guide
        if (lower.contains("jinsi ya kutumia pos") || lower.contains("jinsi ya kuuza") || lower.contains("how to use pos") || lower.contains("how to sell")) {
            return AssistantMessage(
                text = """
                    🛍️ **Mwongozo wa Kutumia POS (Kariakor Stall & Shop):**
                    
                    1. **Fungua POS:** Bonyeza *Kariakor Stall POS* kutoka skrini ya mwanzo.
                    2. **Chagua Bidhaa:** Tumia ubao wa kutafuta au bonyeza bidhaa kwenye orodha.
                    3. **Bei ya Jumla au Rejareja:** Badilisha kitufe cha *Wholesale / Retail* kulingana na aina ya mteja.
                    4. **Malipo:** Chagua kati ya **Pesa Taslimu (Cash)** au **M-Pesa**.
                    5. **Kukamilisha:** Mauzo yatarekodiwa mara moja kwenye Daftari la Hesabu hata kama hauna intaneti!
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.APP_GUIDE,
                actionPayload = AssistantActionPayload.Navigate(Screen.Pos.route, "Fungua POS")
            )
        }

        // Deni / Credit Guide
        if (lower.contains("jinsi ya kuweka deni") || lower.contains("jinsi ya deni") || lower.contains("how does deni work")) {
            return AssistantMessage(
                text = """
                    📒 **Mwongozo wa Kurekodi Deni la Mteja:**
                    
                    1. **Fungua Daftari la Deni:** Nenda kwenye *Deni (Credit) Ledger*.
                    2. **Weka Mteja Mpya:** Bonyeza *Ongeza Mteja*, weka jina, nambari ya simu na tarehe ya makubaliano ya kulipa.
                    3. **Kurekodi Malipo:** Mteja akilipa kiasi chochote, bonyeza *Record Payment* ili salio lipungue kiotomatiki.
                    4. **Kikumbusho cha WhatsApp:** Bonyeza aikoni ya WhatsApp kutuma ujumbe wenye salio lake mara moja bila kuandika kwa mkono.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.APP_GUIDE,
                actionPayload = AssistantActionPayload.Navigate(Screen.DeniLedger.route, "Fungua Daftari la Deni")
            )
        }

        // Garage Job Cards & Vehicle Repair Guide
        if (lower.contains("ukaguzi wa gari") || lower.contains("jinsi ya kutumia job card") || lower.contains("kadi ya kazi") || lower.contains("how to use garage") || lower.contains("bodi") || lower.contains("viti") || lower.contains("tairi")) {
            return AssistantMessage(
                text = """
                    🔧 **Mwongozo wa Kazi za Karakana ya Magari (Auto Garage):**
                    
                    1. **Ingiza Gari (Intake):** Weka nambari ya usajili (plate), jina la mteja na tatizo la gari.
                    2. **Ukaguzi wa Sehemu za Gari:** Chagua sehemu inayohitaji ufundi:
                       • **Bodi & Rangi:** Kutoa bonye (dent removal), welding ya bumper, na kupulizia rangi (respray & buffing).
                       • **Ndani & Viti:** Kushona viti vya ngozi vilivyochanika (torn leather) na dari iliyolegea.
                       • **Matairi & Suspension:** Tairi burst, kunyoosha rim, wheel alignment & balancing, na shock absorbers.
                       • **Injini & Breki:** Radiator leak/overheating, service ya mafuta, na brake pads.
                    3. **Makadirio (WhatsApp Approval):** Tuma makadirio ya gharama ya vipuri na ufundi kwa WhatsApp ya mteja kwa kibali cha mbofyo mmoja.
                    4. **Hatua 7 za Kazi:** Fuatilia kazi kuanzia *Diagnosing* hadi *Completed* na *Invoiced*.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.APP_GUIDE,
                actionPayload = AssistantActionPayload.Navigate(Screen.GarageJobs.route, "Fungua Karakana")
            )
        }

        // Accounting & Cashbook Guide
        if (lower.contains("jinsi ya daftari") || lower.contains("jinsi ya hesabu") || lower.contains("how to use accounting") || lower.contains("how does cashbook work") || lower.contains("funga siku") || lower.contains("cash-up")) {
            return AssistantMessage(
                text = """
                    📖 **Mwongozo wa Daftari ya Hesabu (Cashbook & Accounting):**
                    
                    • **Daftari la Pesa (Cashbook):** Hufuatilia kila sarafu inayoingia au kutoka (Cash vs M-Pesa).
                    • **Kufunga Daftari la Siku (Daily Cash-Up):** Nenda kwenye tab ya *Funga Siku* kuhesabu pesa zilizopo drooni na salio la M-Pesa dhidi ya mauzo ili kugundua upungufu au ziada mwisho wa siku.
                    • **Ankara za Wateja (Invoices):** Tengeneza na fuatilia ankara zenye nambari maalum (INV-0001).
                    • **Bili za Wasambazaji (Supplier Bills):** Rekodi ankara za mzigo na ufuatilie tarehe za malipo.
                    • **Ripoti za Kifedha:** P&L (Faida na Hasara), Balance Sheet, na Trial Balance zinazozalishwa kiotomatiki kwa misingi ya *Double-Entry Accounting*.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.APP_GUIDE,
                actionPayload = AssistantActionPayload.Navigate(Screen.Accounting.route, "Fungua Daftari ya Hesabu")
            )
        }

        // General Help / Jinsi ya kutumia app
        if (lower.contains("msaada") || lower.contains("help") || lower.contains("jinsi ya kutumia app") || lower.contains("how to use this app") || lower.contains("manual")) {
            return AssistantMessage(
                text = """
                    🤝 **Habari Mheshimiwa! Mimi ni Mshauri wako wa Biashara (BusinessOS Copilot):**
                    
                    Niko hapa kurahisisha kazi yako ya kila siku. Unaweza kuniuliza:
                    
                    1. **Takwimu za Biashara:** *"Mauzo ya leo ni ngapi?"*, *"Nani ananidai?"*, *"Salio la benki na cash"*
                    2. **Uchambuzi wa Faida:** *"Nipe ushauri wa biashara"*, *"Je faida yangu ikoje?"*
                    3. **Vitendo vya Haraka:** *"Rekodi matumizi 500 ya lunch"*, *"Tuma WhatsApp ya deni"*
                    4. **KRA & Kodi ya Kenya:** *"KRA eTIMS inafanyaje kazi?"*, *"Turnover Tax ni nini?"*
                    5. **Mwongozo wa App:** *"Jinsi ya kutumia POS"*, *"Jinsi ya kurekodi gari"*
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.APP_GUIDE,
                actionPayload = AssistantActionPayload.Navigate(Screen.UserManual.route, "Fungua Mwongozo Kamili")
            )
        }

        return null
    }

    // ==========================================
    // EXTERNAL KNOWLEDGE & AI QUERY INTENTS
    // ==========================================
    private suspend fun tryDetectExternalOrAi(
        raw: String,
        lower: String,
        snapshot: BusinessSnapshot,
        geminiApiKey: String?
    ): AssistantMessage {
        // Built-in Knowledge Base for KRA eTIMS & Kenyan Small Business Regulations
        if (lower.contains("kra") || lower.contains("etims") || lower.contains("turnover tax") || lower.contains("tot") || lower.contains("kodi") || lower.contains("tax")) {
            return AssistantMessage(
                text = """
                    🇰🇪 **Mwongozo wa KRA eTIMS na Kodi kwa Biashara Ndogo Kenya:**
                    
                    • **KRA eTIMS ni nini?**
                    Ni mfumo wa KRA unaowezesha kutoa ankara za kielektroniki. Kila biashara nchini Kenya inahitajika kuwa na eTIMS ili wanunuzi waweze kudai gharama zao za kibiashara.
                    
                    • **Turnover Tax (TOT):**
                    Kwa biashara ndogo zenye mauzo kati ya **KSh 1,000,000 na KSh 25,000,000** kwa mwaka, kodi ya mauzo (Turnover Tax) inatozwa kwa kiwango cha **3%** ya jumla ya mauzo ghafi (gross sales), ikilipwa kufikia tarehe 20 ya kila mwezi unaofuata.
                    
                    • **eTIMS Lite kwa Simu:**
                    Wafanyabiashara wadogo kama mafundi wa Kariakor au makanika wanaweza kutumia **eTIMS Lite kupitia USSD (*222#)** au tovuti ya eCitizen bila kulazimika kununua mashine ghali ya ETR.
                    
                    • **Ushauri:** BusinessOS inarekodi kila mauzo na matumizi kwa mpangilio mzuri ili kurahisisha kuwasilisha ritani zako kwa KRA bila usumbufu.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.EXTERNAL_KNOWLEDGE
            )
        }

        // Nairobi County Single Business Permit / eCitizen registration
        if (lower.contains("permit") || lower.contains("leseni") || lower.contains("county") || lower.contains("nairobi") || lower.contains("kanjo") || lower.contains("ecitizen")) {
            return AssistantMessage(
                text = """
                    🏛️ **Leseni ya Biashara (Single Business Permit) & eCitizen:**
                    
                    • **Single Business Permit (Kaunti ya Nairobi/Mijini):**
                    Hutolewa kila mwaka kupitia mfumo wa kidijitali wa Kaunti (NairobiPay au eCitizen). Gharama hutegemea ukubwa wa duka, eneo (Zone A/B/C), na idadi ya wafanyakazi.
                    
                    • **Kusajili Jina la Biashara (Business Name Registration):**
                    Hufanywa mtandaoni kupitia **eCitizen (BRS - Business Registration Service)** kwa ada ya KSh 950. Inachukua siku 1 hadi 3 na unapokea cheti rasmi cha CR12 / BN Certificate.
                    
                    • **NSSF na SHIF (zamani NHIF):**
                    Kama umeajiri vijana au mafundi wa kudumu, unahitajika kuwasajili na kuwasilisha michango ya kisheria kila mwezi kabla ya tarehe 9.
                """.trimIndent(),
                isUser = false,
                category = AssistantCategory.EXTERNAL_KNOWLEDGE
            )
        }

        // If Gemini API Key is provided and device is connected to the internet, call Gemini API
        if (!geminiApiKey.isNullOrBlank()) {
            val onlineAnswer = callGeminiOnline(raw, snapshot, geminiApiKey)
            if (onlineAnswer != null) {
                return AssistantMessage(
                    text = onlineAnswer,
                    isUser = false,
                    category = AssistantCategory.EXTERNAL_KNOWLEDGE
                )
            }
        }

        // Offline Fallback for general questions
        return AssistantMessage(
            text = """
                💡 **Mshauri wa Biashara (BusinessOS):**
                
                Nimepokea swali lako: *"$raw"*
                
                Kama mshauri wako wa kibiashara, ninapendekeza:
                • Hakikisha rekodi zako za kila siku za fedha na mauzo zimesajiliwa kwa usahihi.
                • Unaweza kuniuliza kuhusu mauzo, madeni ya wateja (Deni), ushauri wa faida, au taratibu za KRA eTIMS.
                • *(Kidokezo: Ukiunganisha API Key ya Google Gemini kwenye Mipangilio, naweza kujibu maswali yoyote mapana kutoka mtandaoni).*
            """.trimIndent(),
            isUser = false,
            category = AssistantCategory.EXTERNAL_KNOWLEDGE
        )
    }

    // ==========================================
    // GEMINI REST API INTEGRATION (Pure HttpURLConnection)
    // ==========================================
    private suspend fun callGeminiOnline(
        query: String,
        snapshot: BusinessSnapshot,
        apiKey: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; utf-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 12000
                readTimeout = 12000
            }

            val prompt = """
                You are 'Mshauri wa Biashara', an expert AI business advisor built into BusinessOS for small business owners in Kenya (aged 40+, running auto garages, Kariakor/Gikomba market shops, hardware stores).
                Tone: Warm, highly respectful, clear, practical, bilingual (Swahili & English).
                Current business context:
                - Business Name: ${snapshot.businessName}
                - Business Type: ${snapshot.businessType}
                - All-Time Sales: KSh ${snapshot.totalSalesAllTime}
                - Today's Sales: KSh ${snapshot.totalSalesToday}
                - Net Profit: KSh ${snapshot.netProfit} (Margin: ${String.format("%.1f", snapshot.profitMarginPct)}%)
                - Total Customer Credit (Deni): KSh ${snapshot.totalDeniReceivable}
                
                User question: $query
                
                Provide a concise, direct, practical answer tailored to the Kenyan business environment. Keep formatting clean with bullet points.
            """.trimIndent()

            val requestBody = JsonObject().apply {
                val contents = com.google.gson.JsonArray().apply {
                    val contentObj = JsonObject().apply {
                        val parts = com.google.gson.JsonArray().apply {
                            val partObj = JsonObject().apply {
                                addProperty("text", prompt)
                            }
                            add(partObj)
                        }
                        add("parts", parts)
                    }
                    add(contentObj)
                }
                add("contents", contents)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(gson.toJson(requestBody))
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = gson.fromJson(responseText, JsonObject::class.java)
                val candidates = responseJson.getAsJsonArray("candidates")
                if (candidates != null && candidates.size() > 0) {
                    val firstCandidate = candidates[0].asJsonObject
                    val content = firstCandidate.getAsJsonObject("content")
                    val parts = content.getAsJsonArray("parts")
                    if (parts != null && parts.size() > 0) {
                        return@withContext parts[0].asJsonObject.get("text").asString
                    }
                }
            }
            null
        } catch (e: Exception) {
            null // Gracefully falls back to local knowledge engine
        }
    }
}
