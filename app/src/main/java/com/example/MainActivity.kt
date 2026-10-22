package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.glass.LocalLiquidState
import io.github.fletchmckee.liquid.liquefiable
import io.github.fletchmckee.liquid.rememberLiquidState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.StillBottomNavBar
import com.example.ui.components.StillTab
import com.example.ui.screens.BreatheScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TherapistScreen
import com.example.ui.screens.TherapyCallScreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WellnessViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WellnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("OPEN_EMO", false) || intent.hasExtra("EMO_MOOD")) {
            viewModel.setEmoScreenOpen(true)
        }
        intent.getStringExtra("OPEN_TAB")?.let { tabName ->
            try {
                viewModel.setTab(com.example.ui.components.StillTab.valueOf(tabName))
            } catch (_: Exception) {}
        }
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                RequestEssentialPermissions()

                // Rotate the top quote card every time the user opens/resumes the app
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START) {
                            viewModel.rotateQuoteOnAppOpen(isFromActivityStart = true)
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                DisposableEffect(Unit) {
                    val receiver = object : android.content.BroadcastReceiver() {
                        override fun onReceive(c: android.content.Context?, intent: android.content.Intent?) {
                            val open = intent?.getBooleanExtra("open", true) ?: true
                            viewModel.setEmoScreenOpen(open)
                        }
                    }
                    val filter = android.content.IntentFilter("com.example.TOGGLE_EMO_SCREEN")
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_EXPORTED)
                        } else {
                            context.registerReceiver(receiver, filter)
                        }
                    } catch (_: Exception) {}
                    onDispose {
                        try {
                            context.unregisterReceiver(receiver)
                        } catch (_: Exception) {}
                    }
                }

                val currentTab by viewModel.currentTab.collectAsState()
                val callState by viewModel.callState.collectAsState()
                val isEmoScreenOpen by viewModel.isEmoScreenOpen.collectAsState()
                val ambientTheme by viewModel.ambientTheme.collectAsState()
                val liquidState = rememberLiquidState()

                BackHandler(enabled = callState.isActive || isEmoScreenOpen || currentTab != StillTab.TODAY) {
                    if (callState.isActive) {
                        viewModel.endCall()
                    } else if (isEmoScreenOpen) {
                        viewModel.setEmoScreenOpen(false)
                    } else {
                        viewModel.setTab(StillTab.TODAY)
                    }
                }

                CompositionLocalProvider(LocalLiquidState provides liquidState) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MidnightDark)
                    ) {
                        com.example.ui.glass.CosmicHorizonCanvas(
                            theme = ambientTheme,
                            modifier = Modifier
                                .fillMaxSize()
                                .liquefiable(liquidState)
                        )

                        if (callState.isActive) {
                            TherapyCallScreen(
                                viewModel = viewModel,
                                onCallEnded = { viewModel.endCall() }
                            )
                        } else if (isEmoScreenOpen) {
                            com.example.ui.screens.EmoRobotScreen(
                                onClose = { viewModel.setEmoScreenOpen(false) }
                            )
                        } else {
                            Scaffold(
                                containerColor = Color.Transparent,
                                bottomBar = {
                                    StillBottomNavBar(
                                        currentTab = currentTab,
                                        onTabSelected = { viewModel.setTab(it) }
                                    )
                                }
                            ) { innerPadding ->
                                Crossfade(
                                    targetState = currentTab,
                                    label = "ScreenCrossfade",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) { tab ->
                                    when (tab) {
                                        StillTab.TODAY -> DashboardScreen(viewModel = viewModel)
                                        StillTab.TRENDS -> JournalScreen(viewModel = viewModel)
                                        StillTab.BREATHE -> BreatheScreen(viewModel = viewModel)
                                        StillTab.THERAPISTS -> TherapistScreen(
                                            viewModel = viewModel,
                                            onNavigateToCall = {}
                                        )
                                        StillTab.PROFILE -> ProfileScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("OPEN_EMO", false) || intent.hasExtra("EMO_MOOD")) {
            viewModel.setEmoScreenOpen(true)
        }
        intent.getStringExtra("OPEN_TAB")?.let { tabName ->
            try {
                viewModel.setTab(com.example.ui.components.StillTab.valueOf(tabName))
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun RequestEssentialPermissions() {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }
}
