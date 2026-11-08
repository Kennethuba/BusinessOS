package com.example.businessos.ui.subscription

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.model.Role
import com.example.businessos.data.model.SubscriptionPlan
import com.example.businessos.ui.components.BusinessOsTopBar
import com.example.businessos.ui.components.RoleBadge
import com.example.businessos.ui.components.SyncBadge
import com.example.businessos.ui.threed.Tactile3DCard
import kotlin.math.*

// ─── Plan Overview Banner (Clean & Responsive) ───────────────────────────────

@Composable
fun PlanHeaderBanner(
    selectedPlan: SubscriptionPlan,
    modifier: Modifier = Modifier
) {
    val tierColor = when (selectedPlan) {
        SubscriptionPlan.STARTER -> Color(0xFF10B981)
        SubscriptionPlan.GROWTH -> Color(0xFF3B82F6)
        SubscriptionPlan.ENTERPRISE -> Color(0xFFF59E0B)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161E2E)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, tierColor.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = tierColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "PLAN: ${selectedPlan.name}",
                        color = tierColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = when (selectedPlan) {
                        SubscriptionPlan.STARTER -> "Bure (Free Trial)"
                        SubscriptionPlan.GROWTH -> "KSh 1,500 / mwezi"
                        SubscriptionPlan.ENTERPRISE -> "KSh 3,500 / mwezi"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = when (selectedPlan) {
                    SubscriptionPlan.STARTER -> "Inafaa kwa duka moja au fundi anayeanza. Wafanyakazi 2 na daftari la msingi la mauzo."
                    SubscriptionPlan.GROWTH -> "Inafaa kwa maduka ya Kariakor na karakana za kati. Wafanyakazi 5 na daftari kamili la P&L."
                    SubscriptionPlan.ENTERPRISE -> "Inafaa kwa karakana kubwa na maduka yenye matawi. Wafanyakazi bila kikomo na ukaguzi wa 2D."
                },
                fontSize = 13.sp,
                color = Color(0xFF8B949E),
                lineHeight = 18.sp
            )
        }
    }
}

// ─── Main Screen Composable ──────────────────────────────────────────────────

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    syncState: com.example.businessos.sync.SyncUiState,
    businessName: String?,
    onSwitchWorkspace: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var previewPlan by remember(uiState.currentPlan) { mutableStateOf(uiState.currentPlan) }

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
                    title = "Subscription & Tier",
                    businessName = businessName,
                    role = uiState.userRole,
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
        ) {
            // Plan Overview Banner
            PlanHeaderBanner(selectedPlan = previewPlan)

            // Current Plan Summary Strip
            CurrentPlanSummary(uiState = uiState)

            Spacer(Modifier.height(16.dp))

            // Plan Selection Cards
            Text(
                text = "Available Plans (Kariakor & Garage)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(8.dp))

            SubscriptionPlan.entries.forEach { plan ->
                val isCurrent = uiState.currentPlan == plan
                val isInspected = previewPlan == plan

                Tactile3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { previewPlan = plan },
                    height = 360.dp
                ) {
                    PlanCardContent(
                        plan = plan,
                        isCurrent = isCurrent,
                        isInspected = isInspected,
                        userRole = uiState.userRole,
                        isLoading = uiState.isLoading,
                        onUpgrade = { viewModel.upgradePlan(plan) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Plan Card Detail ────────────────────────────────────────────────────────

@Composable
private fun PlanCardContent(
    plan: SubscriptionPlan,
    isCurrent: Boolean,
    isInspected: Boolean,
    userRole: Role?,
    isLoading: Boolean,
    onUpgrade: () -> Unit
) {
    val tierColor = when (plan) {
        SubscriptionPlan.STARTER -> Color(0xFF10B981)
        SubscriptionPlan.GROWTH -> Color(0xFF3B82F6)
        SubscriptionPlan.ENTERPRISE -> Color(0xFFF59E0B)
    }

    val priceText = when (plan) {
        SubscriptionPlan.STARTER -> "Free Forever"
        SubscriptionPlan.GROWTH -> "KSh 2,500 / mo"
        SubscriptionPlan.ENTERPRISE -> "KSh 6,500 / mo"
    }

    val limitsText = when (plan) {
        SubscriptionPlan.STARTER -> "1 Business • Up to 2 Staff"
        SubscriptionPlan.GROWTH -> "Up to 3 Businesses • 5 Staff"
        SubscriptionPlan.ENTERPRISE -> "Up to 10 Businesses • Unlimited Staff"
    }

    val features = when (plan) {
        SubscriptionPlan.STARTER -> listOf(
            "Offline Room DB Source-of-Truth",
            "Job Cards & POS Sales",
            "Single Workspace Mode"
        )
        SubscriptionPlan.GROWTH -> listOf(
            "All Starter features",
            "Multi-branch Workspace Switcher",
            "Audit Trail & RBAC",
            "Priority Local Sync"
        )
        SubscriptionPlan.ENTERPRISE -> listOf(
            "All Growth features",
            "Multi-garage & Multi-stall Fleet",
            "Granular Manager / Staff Permissions",
            "Dedicated Kenyan Business Support"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isInspected) 2.dp else 1.dp,
                color = if (isInspected) tierColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = tierColor
                    )
                    Text(
                        text = limitsText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isCurrent) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ACTIVE PLAN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))

            features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = tierColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!isCurrent) {
                Button(
                    onClick = onUpgrade,
                    enabled = userRole == Role.OWNER && !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = tierColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (userRole != Role.OWNER) "Owner Only Action" else "Switch to ${plan.name}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ─── Current Plan Stats Strip ────────────────────────────────────────────────

@Composable
private fun CurrentPlanSummary(uiState: SubscriptionUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiState.businessName.ifEmpty { "Active Business" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.currentPlan.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Staff Enrolled",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.memberCount} Members",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
