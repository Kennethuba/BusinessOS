package com.example.businessos.ui.garage

import com.example.businessos.ui.util.AppLanguageManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.CardBackground
import com.example.businessos.ui.theme.CardBorder
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkBorder
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.TealAccent
import com.example.businessos.ui.theme.TextMuted
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary

@Composable
fun VehicleHistoryScreen(
    viewModel: GarageViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToJobDetail: (String) -> Unit
) {
    var searchPlate by remember { mutableStateOf("") }
    val allJobs by viewModel.jobCards.collectAsState()

    val matchingJobs = remember(allJobs, searchPlate) {
        if (searchPlate.isBlank()) emptyList()
        else allJobs.filter { it.vehiclePlate.contains(searchPlate.trim(), ignoreCase = true) }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = DarkSurface,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = AppLanguageManager.tr("Back", "Rudi"), tint = TextPrimary)
                    }
                    Text(
                        text = AppLanguageManager.tr("Vehicle history", "Historia ya gari"),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchPlate,
                    onValueChange = { searchPlate = it.uppercase() },
                    label = { Text(AppLanguageManager.tr("Vehicle plate", "Namba ya gari")) },
                    placeholder = { Text(AppLanguageManager.tr("e.g. KDL 456Z", "mf. KDL 456Z")) },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AmberAccent)
                    },
                    trailingIcon = {
                        if (searchPlate.isNotEmpty()) {
                            IconButton(onClick = { searchPlate = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = AppLanguageManager.tr("Clear", "Futa"), tint = TextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text(
                text = AppLanguageManager.tr("Past jobs (${matchingJobs.size})", "Kazi za awali (${matchingJobs.size})"),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(matchingJobs, key = { it.id }) { job ->
                    JobCardRow(
                        jobCard = job,
                        onClick = { onNavigateToJobDetail(job.id) }
                    )
                }

                if (matchingJobs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, tint = TealAccent, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchPlate.isEmpty()) AppLanguageManager.tr("Enter a plate to search", "Weka namba ya gari kutafuta") else AppLanguageManager.tr("No records for $searchPlate", "Hakuna rekodi za $searchPlate"),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = AppLanguageManager.tr("Past visits, parts and findings for this vehicle will show here.", "Ziara za awali, vipuri na matokeo ya gari hili yataonekana hapa."),
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
