package com.example.businessos.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.security.AppLock
import com.example.businessos.data.security.PinSecurity
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.DarkSurface
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextPrimary
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: AuthViewModel,
    appLock: AppLock,
    onBack: () -> Unit
) {
    val timeout by appLock.timeoutMillis.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.clearError() }
    LaunchedEffect(message) {
        message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    val options = listOf(
        AppLock.IMMEDIATELY to AppLanguageManager.tr("Right away", "Mara moja"),
        AppLock.ONE_MINUTE to AppLanguageManager.tr("After 1 minute", "Baada ya dakika 1"),
        AppLock.FIVE_MINUTES to AppLanguageManager.tr("After 5 minutes", "Baada ya dakika 5"),
        AppLock.FIFTEEN_MINUTES to AppLanguageManager.tr("After 15 minutes", "Baada ya dakika 15")
    )

    Scaffold(
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(AppLanguageManager.tr("Security", "Usalama"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = AppLanguageManager.tr("Back", "Rudi")) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(AppLanguageManager.tr("Lock the app", "Funga programu"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(
                AppLanguageManager.tr(
                    "Ask for the PIN again when the app has been in the background for:",
                    "Uliza PIN tena programu ikiwa nyuma kwa:"
                ),
                fontSize = 14.sp,
                color = TextSecondary
            )
            options.forEach { (millis, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { appLock.setTimeout(millis) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = timeout == millis,
                        onClick = { appLock.setTimeout(millis) },
                        colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                    )
                    Text(label, fontSize = 16.sp, color = TextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(AppLanguageManager.tr("Change my PIN", "Badilisha PIN yangu"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            PinField(oldPin, { oldPin = it; viewModel.clearError() }, AppLanguageManager.tr("Current PIN", "PIN ya sasa"))
            PinField(newPin, { newPin = it; viewModel.clearError() }, AppLanguageManager.tr("New PIN (${PinSecurity.PIN_LENGTH} digits)", "PIN mpya (tarakimu ${PinSecurity.PIN_LENGTH})"))
            PinField(
                confirmPin,
                { confirmPin = it; viewModel.clearError() },
                AppLanguageManager.tr("Repeat the new PIN", "Rudia PIN mpya"),
                imeAction = ImeAction.Done
            )
            error?.let { Text(it, color = StatusRed, fontSize = 14.sp) }

            Button(
                onClick = {
                    viewModel.changePin(oldPin, newPin, confirmPin) {
                        oldPin = ""; newPin = ""; confirmPin = ""
                    }
                },
                enabled = !loading && oldPin.length == PinSecurity.PIN_LENGTH &&
                    newPin.length == PinSecurity.PIN_LENGTH && confirmPin.length == PinSecurity.PIN_LENGTH,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Save new PIN", "Hifadhi PIN mpya"), fontSize = 18.sp)
            }
        }
    }
}
