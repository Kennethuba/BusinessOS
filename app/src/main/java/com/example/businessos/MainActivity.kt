package com.example.businessos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.businessos.ui.auth.AuthViewModel
import com.example.businessos.ui.auth.LockScreen
import com.example.businessos.ui.navigation.NavGraph
import com.example.businessos.ui.theme.BusinessOSTheme

class MainActivity : ComponentActivity() {

    private val app get() = application as BusinessOsApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        com.example.businessos.ui.theme.ThemeManager.init(this)
        com.example.businessos.ui.util.AppLanguageManager.init(this)

        setContent {
            BusinessOSTheme {
                var restored by remember { mutableStateOf(false) }
                val session by app.authRepository.session.collectAsState()
                val locked by app.appLock.isLocked.collectAsState()
                val authViewModel: AuthViewModel = viewModel(factory = app.viewModelFactory)

                // Someone was signed in before the app closed: bring them back, but ask for the PIN.
                LaunchedEffect(Unit) {
                    if (app.authRepository.restoreSession()) app.appLock.lock()
                    restored = true
                }

                if (restored) {
                    val navController = rememberNavController()
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavGraph(
                            navController = navController,
                            viewModelFactory = app.viewModelFactory,
                            authRepository = app.authRepository,
                            appLock = app.appLock
                        )
                        if (session != null && locked) {
                            // Swallow touches and the back button so nothing underneath can be used.
                            BackHandler(enabled = true) { }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        awaitEachGesture { awaitFirstDown().consume() }
                                    }
                            ) {
                                LockScreen(
                                    viewModel = authViewModel,
                                    onUnlocked = { app.appLock.unlock() },
                                    onSignOut = {
                                        authViewModel.logout()
                                        app.appLock.unlock()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) app.appLock.onBackgrounded()
    }

    override fun onStart() {
        super.onStart()
        if (app.authRepository.session.value != null) app.appLock.onForegrounded()
    }
}
