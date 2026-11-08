package com.example.businessos.ui.feedback

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.sync.SyncUiState
import com.example.businessos.ui.components.BusinessOsTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    userEmail: String?,
    userName: String?,
    syncState: SyncUiState,
    businessName: String?,
    onSwitchWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var feedbackType by remember { mutableStateOf("Feedback") }
    var feedbackMessage by remember { mutableStateOf("") }
    var userContactInput by remember(userEmail) { mutableStateOf(userEmail ?: "") }
    var isSubmitted by remember { mutableStateOf(false) }

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
                    title = "Feedback & Support",
                    businessName = businessName,
                    role = null,
                    syncState = syncState,
                    onSwitchWorkspace = onSwitchWorkspace,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2536))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Welcome to Business OS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Direct contact with the development & support team",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            if (isSubmitted) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Thank you for reaching out!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "We have opened your email client to send your message directly to the Business OS support team.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text(
                text = "Select Category",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Feedback", "Feature Request", "Report Bug").forEach { type ->
                    FilterChip(
                        selected = feedbackType == type,
                        onClick = { feedbackType = type },
                        label = { Text(type) }
                    )
                }
            }

            OutlinedTextField(
                value = userContactInput,
                onValueChange = { userContactInput = it },
                label = { Text("Your Email or Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = feedbackMessage,
                onValueChange = { feedbackMessage = it },
                label = { Text("Your Message or Feature Idea") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = {
                    sendFeedbackEmail(
                        context = context,
                        userName = userName ?: "Business OS User",
                        userContact = userContactInput,
                        category = feedbackType,
                        message = feedbackMessage,
                        businessName = businessName ?: "Unassigned"
                    )
                    isSubmitted = true
                },
                enabled = feedbackMessage.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
            ) {
                Icon(Icons.Filled.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Send to Developer / Support", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun sendFeedbackEmail(
    context: Context,
    userName: String,
    userContact: String,
    category: String,
    message: String,
    businessName: String
) {
    val subject = "[Business OS $category] From $userName ($businessName)"
    val body = """
        Business OS Feedback & Client Inquiry
        ------------------------------------
        User: $userName
        Contact: $userContact
        Business Workspace: $businessName
        Category: $category

        Message:
        $message
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@businessos.co.ke"))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Email via..."))
    } catch (_: Exception) {}
}
