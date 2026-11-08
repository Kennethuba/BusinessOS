package com.example.businessos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.businessos.ui.auth.PinSecurityManager
import com.example.businessos.ui.navigation.NavGraph
import com.example.businessos.ui.theme.BusinessOSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.example.businessos.ui.theme.ThemeManager.init(this)
        com.example.businessos.ui.util.AppLanguageManager.init(this)

        val app = application as BusinessOsApplication

        setContent {
            BusinessOSTheme {
                val context = LocalContext.current
                var isAppUnlocked by remember {
                    mutableStateOf(!PinSecurityManager.isPinEnabled(context))
                }

                if (!isAppUnlocked) {
                    com.example.businessos.ui.auth.PinLockScreen(
                        isSetupMode = false,
                        onSuccess = { isAppUnlocked = true }
                    )
                } else {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        viewModelFactory = app.viewModelFactory,
                        authRepository = app.authRepository,
                        syncManager = app.syncManager
                    )
                }
            }
        }
    }
}