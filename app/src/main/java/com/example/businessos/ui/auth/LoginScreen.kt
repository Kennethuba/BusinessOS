package com.example.businessos.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.security.PinSecurity
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (hasBusiness: Boolean) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val error by viewModel.errorMessage.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    fun submit() = viewModel.login(phone, pin, onLoginSuccess)

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader(
                title = AppLanguageManager.tr("Sign in", "Ingia"),
                subtitle = AppLanguageManager.tr("Use your phone number and PIN", "Tumia namba ya simu na PIN yako")
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it; viewModel.clearError() },
                label = { Text(AppLanguageManager.tr("Phone number", "Namba ya simu")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                colors = authFieldColors()
            )
            Spacer(Modifier.height(12.dp))
            PinField(
                value = pin,
                onValueChange = { pin = it; viewModel.clearError() },
                label = AppLanguageManager.tr("PIN (${PinSecurity.PIN_LENGTH} digits)", "PIN (tarakimu ${PinSecurity.PIN_LENGTH})"),
                imeAction = ImeAction.Done,
                onDone = { submit() }
            )

            error?.let {
                Text(it, color = StatusRed, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { submit() },
                enabled = !loading && phone.isNotBlank() && pin.length == PinSecurity.PIN_LENGTH,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Sign in", "Ingia"), fontSize = 18.sp)
            }

            TextButton(onClick = onNavigateToSignup, modifier = Modifier.fillMaxWidth()) {
                Text(
                    AppLanguageManager.tr("New here? Create an account", "Mgeni? Fungua akaunti"),
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        }
    }
}
