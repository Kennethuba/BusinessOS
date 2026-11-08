package com.example.businessos.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.assistant.AssistantActionPayload
import com.example.businessos.data.assistant.AssistantCategory
import com.example.businessos.data.assistant.AssistantMessage
import com.example.businessos.sync.SyncUiState
import com.example.businessos.ui.components.WhatsAppHelper
import com.example.businessos.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    viewModel: AssistantViewModel,
    syncState: SyncUiState,
    businessName: String?,
    onSwitchWorkspace: () -> Unit,
    onNavigateToScreen: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var tempApiKey by remember { mutableStateOf(uiState.geminiApiKey) }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(AmberAccent.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = null,
                            tint = AmberAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mshauri wa Biashara",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = if (!uiState.geminiApiKey.isBlank()) "Online AI Connected • ${businessName ?: "Business"}" else "Offline Copilot Active • ${businessName ?: "Business"}",
                            fontSize = 12.sp,
                            color = if (!uiState.geminiApiKey.isBlank()) TealAccent else StatusAmber
                        )
                    }

                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.VpnKey,
                            contentDescription = "API Key Settings",
                            tint = if (uiState.geminiApiKey.isNotBlank()) TealAccent else TextMuted
                        )
                    }

                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear Chat",
                            tint = TextMuted
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick Suggestion Chips Carousel
            QuickPromptsRow(
                onPromptSelected = { prompt ->
                    viewModel.sendMessage(prompt)
                }
            )

            // Message History List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onNavigate = onNavigateToScreen,
                        onWhatsAppClick = { payload ->
                            WhatsAppHelper.openWhatsAppDeniReminder(
                                context = context,
                                customerName = payload.customerName,
                                customerPhone = payload.customerPhone,
                                balance = payload.balance,
                                businessName = businessName,
                                dueDate = payload.dueDate
                            )
                        },
                        onConfirmExpense = { messageId, payload ->
                            viewModel.executeStagedExpense(messageId, payload)
                        }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AmberAccent,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Mshauri anachakata jibu...",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Error banner if any
            uiState.error?.let { err ->
                Surface(
                    color = StatusRed.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = err,
                        color = StatusRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Bottom Input Bar
            Surface(
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        placeholder = {
                            Text(
                                text = "Uliza swali au andika kitendo...",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AmberAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )

                    FloatingActionButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                val query = inputText
                                inputText = ""
                                viewModel.sendMessage(query)
                            }
                        },
                        containerColor = AmberAccent,
                        contentColor = TextOnAccent,
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    // Optional Gemini API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Text("Mipangilio ya Mtandao (AI Settings)", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "Mfumo unafanya kazi 100% bila intaneti ukitumia akili ya ndani (Offline Rules & Kenya MSME Knowledge).",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ukitaka Mshauri ajibu maswali yoyote mapana kupitia Google Gemini mtandaoni, weka API Key yako hapa:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempApiKey,
                        onValueChange = { tempApiKey = it },
                        placeholder = { Text("Weka Gemini API Key", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setGeminiApiKey(tempApiKey)
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                ) {
                    Text("Hifadhi (Save)", color = TextOnAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Ghairi", color = TextMuted)
                }
            },
            containerColor = DarkSurface,
            textContentColor = TextPrimary
        )
    }
}

@Composable
fun QuickPromptsRow(onPromptSelected: (String) -> Unit) {
    val prompts = listOf(
        "📊 Mauzo ya Leo?" to "Mauzo ya leo ni ngapi?",
        "📒 Nani ananidai?" to "Nani ananidai kwa sasa?",
        "💡 Ushauri wa Biashara" to "Nipe uchambuzi na ushauri wa biashara yangu",
        "⚡ Rekodi Matumizi" to "Rekodi matumizi ya 500 kwa ajili ya chakula na chai",
        "🏛️ KRA eTIMS & Kodi" to "KRA eTIMS inafanyaje kazi kwa biashara ndogo?",
        "🛍️ Jinsi ya Kutumia POS" to "Jinsi ya kutumia POS kuuza dukani?",
        "🔧 Kadi za Kazi Karakana" to "Jinsi ya kurekodi gari na ukaguzi wa 3D?"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(prompts) { (chipLabel, promptQuery) ->
            Surface(
                color = DarkSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.clickable { onPromptSelected(promptQuery) }
            ) {
                Text(
                    text = chipLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: AssistantMessage,
    onNavigate: (String) -> Unit,
    onWhatsAppClick: (AssistantActionPayload.WhatsAppReminder) -> Unit,
    onConfirmExpense: (String, AssistantActionPayload.StageExpense) -> Unit
) {
    if (message.isUser) {
        // User Message (Right Aligned)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                color = AmberAccent.copy(alpha = 0.9f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Text(
                    text = message.text,
                    color = TextOnAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    } else {
        // Assistant Message (Left Aligned Card)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                color = if (message.isError) Color(0xFF3B1515) else CardBackground,
                shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (message.isError) StatusRed else CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Category Header Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = when (message.category) {
                                AssistantCategory.BUSINESS_DATA -> TealAccent.copy(alpha = 0.15f)
                                AssistantCategory.BUSINESS_ANALYST -> AmberAccent.copy(alpha = 0.15f)
                                AssistantCategory.ACTION -> Color(0xFF818CF8).copy(alpha = 0.15f)
                                AssistantCategory.EXTERNAL_KNOWLEDGE -> Color(0xFF38BDF8).copy(alpha = 0.15f)
                                else -> Color.White.copy(alpha = 0.08f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = message.category.titleSwahili,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (message.category) {
                                    AssistantCategory.BUSINESS_DATA -> TealAccent
                                    AssistantCategory.BUSINESS_ANALYST -> AmberAccent
                                    AssistantCategory.ACTION -> Color(0xFF818CF8)
                                    AssistantCategory.EXTERNAL_KNOWLEDGE -> Color(0xFF38BDF8)
                                    else -> TextSecondary
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "Mshauri",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Message Content
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    // Embedded Interactive Action Component
                    message.actionPayload?.let { payload ->
                        Spacer(modifier = Modifier.height(14.dp))
                        when (payload) {
                            is AssistantActionPayload.WhatsAppReminder -> {
                                Button(
                                    onClick = { onWhatsAppClick(payload) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF25D366),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tuma WhatsApp kwa ${payload.customerName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            is AssistantActionPayload.Navigate -> {
                                Button(
                                    onClick = { onNavigate(payload.screenRoute) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmberAccent,
                                        contentColor = TextOnAccent
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = payload.label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            is AssistantActionPayload.StageExpense -> {
                                if (message.actionExecuted) {
                                    Surface(
                                        color = StatusGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Imerekodiwa kikamilifu kwenye Daftari la Pesa",
                                                color = StatusGreen,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { onConfirmExpense(message.id, payload) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = StatusGreen,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Filled.Done, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Thibitisha Kurekodi KSh ${payload.amount.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
