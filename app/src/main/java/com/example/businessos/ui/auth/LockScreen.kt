package com.example.businessos.ui.auth

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.businessos.data.security.PinSecurity
import com.example.businessos.ui.theme.AmberAccent
import com.example.businessos.ui.theme.DarkBackground
import com.example.businessos.ui.theme.StatusRed
import com.example.businessos.ui.theme.TextOnAccent
import com.example.businessos.ui.theme.TextSecondary
import com.example.businessos.ui.util.AppLanguageManager

/**
 * Shown over the app when it is locked. The signed-in person is selected, and anyone else in the
 * business can tap their own name and enter their own PIN to take over this phone.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    viewModel: AuthViewModel,
    onUnlocked: () -> Unit,
    onSignOut: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val candidates by viewModel.candidates.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    val loading by viewModel.isLoading.collectAsState()

    var selectedUserId by remember { mutableStateOf(session?.userId) }
    var pin by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.clearError()
        viewModel.loadCandidates()
    }

    fun submit() {
        val id = selectedUserId ?: return
        viewModel.unlock(id, pin) {
            pin = ""
            onUnlocked()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DarkBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AuthHeader(
                title = session?.activeBusinessName ?: AppLanguageManager.tr("Locked", "Imefungwa"),
                subtitle = AppLanguageManager.tr("Choose your name and enter your PIN", "Chagua jina lako na uweke PIN")
            )

            if (candidates.size > 1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    candidates.forEach { person ->
                        FilterChip(
                            selected = selectedUserId == person.userId,
                            onClick = {
                                selectedUserId = person.userId
                                pin = ""
                                viewModel.clearError()
                            },
                            label = { Text(person.name, fontSize = 16.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Text(session?.name.orEmpty(), fontSize = 18.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
            }

            PinField(
                value = pin,
                onValueChange = { pin = it; viewModel.clearError() },
                label = AppLanguageManager.tr("PIN", "PIN"),
                imeAction = ImeAction.Done,
                onDone = { submit() }
            )

            error?.let {
                Text(it, color = StatusRed, fontSize = 14.sp, modifier = Modifier.padding(top = 12.dp))
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { submit() },
                enabled = !loading && selectedUserId != null && pin.length == PinSecurity.PIN_LENGTH,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = TextOnAccent)
            ) {
                Text(AppLanguageManager.tr("Unlock", "Fungua"), fontSize = 18.sp)
            }

            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text(AppLanguageManager.tr("Sign out", "Toka"), color = TextSecondary, fontSize = 16.sp)
            }
        }
    }
}
