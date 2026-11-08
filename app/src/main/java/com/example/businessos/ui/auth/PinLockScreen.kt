package com.example.businessos.ui.auth

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import com.example.businessos.ui.theme.*

object PinSecurityManager {
    private const val PREFS_NAME = "business_os_prefs"
    private const val KEY_PIN = "owner_pin_code"
    private const val KEY_PIN_ENABLED = "owner_pin_enabled"

    fun isPinEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PIN_ENABLED, false) && prefs.getString(KEY_PIN, null)?.length == 4
    }

    fun verifyPin(context: Context, enteredPin: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_PIN, "") ?: ""
        return saved == enteredPin
    }

    fun savePin(context: Context, pin: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PIN, pin)
            .putBoolean(KEY_PIN_ENABLED, true)
            .apply()
    }

    fun disablePin(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_PIN)
            .putBoolean(KEY_PIN_ENABLED, false)
            .apply()
    }
}

@Composable
fun PinLockScreen(
    isSetupMode: Boolean = false,
    onSuccess: () -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var enteredPin by remember { mutableStateOf("") }
    var confirmPinStep by remember { mutableStateOf(false) }
    var firstEnteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            errorMessage = null

            if (newPin.length == 4) {
                if (isSetupMode) {
                    if (!confirmPinStep) {
                        firstEnteredPin = newPin
                        confirmPinStep = true
                        enteredPin = ""
                    } else {
                        if (newPin == firstEnteredPin) {
                            PinSecurityManager.savePin(context, newPin)
                            onSuccess()
                        } else {
                            errorMessage = "PIN hazilingani. Tafadhali anza upya."
                            confirmPinStep = false
                            firstEnteredPin = ""
                            enteredPin = ""
                        }
                    }
                } else {
                    if (PinSecurityManager.verifyPin(context, newPin)) {
                        onSuccess()
                    } else {
                        errorMessage = "PIN siyo sahihi! Tafadhali jaribu tena."
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Scaffold(containerColor = DarkBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(AmberAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = AmberAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = if (isSetupMode) {
                    if (!confirmPinStep) "Weka PIN Mpya (Set PIN)" else "Thibitisha PIN Yako (Confirm PIN)"
                } else {
                    "Fungua BusinessOS (Unlock App)"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = if (isSetupMode) "Weka tarakimu 4 za kulinda daftari lako"
                else "Weka tarakimu 4 kufungua rekodi za biashara",
                fontSize = 13.sp,
                color = TextSecondary
            )

            Spacer(Modifier.height(28.dp))

            // 4 Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (isFilled) AmberAccent else DarkSurfaceElevated)
                            .then(
                                if (!isFilled) Modifier.background(DarkSurfaceElevated)
                                else Modifier
                            )
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = errorMessage!!,
                    color = StatusRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(36.dp))

            // Keypad (3x4 grid)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                keypad.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(Modifier.size(68.dp))
                            } else if (key == "DEL") {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurfaceElevated)
                                        .clickable { onBackspace() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = TextPrimary)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface)
                                        .clickable { onDigitPress(key) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = key,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (onCancel != null) {
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = onCancel) {
                    Text("Ghairi (Cancel)", color = TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}
