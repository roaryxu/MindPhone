package com.domain.mindphone
import androidx.compose.ui.graphics.Color
import com.domain.mindphone.ui.theme.WarmSienna

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.domain.mindphone.data.repository.PackageManagerAppRepository
import com.domain.mindphone.ui.apps.AppDrawerScreen
import com.domain.mindphone.ui.home.HomeScreen
import com.domain.mindphone.ui.theme.MindPhoneTheme

class HomeActivity : ComponentActivity() {

    // Simple manual dependency resolution for phase 1.
    private lateinit var appRepository: PackageManagerAppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge drawing for premium immersive aesthetic
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Hide notification bar by default (Fix #4)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        appRepository = PackageManagerAppRepository(applicationContext)

        setContent {
            val colors = listOf(WarmSienna, Color(0xFFA7B5C8), Color(0xFFFFFFFF))
            var colorIndex by remember { mutableStateOf(0) }
            val primaryColor = colors[colorIndex]

            MindPhoneTheme(primaryColor = primaryColor) {
                val allApps by appRepository.getInstalledApps().collectAsState(initial = emptyList())
                val favoriteApps = allApps.filter { it.isFavorite }
                
                var showDrawer by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showDrawer) {
                            AppDrawerScreen(
                                allApps = allApps,
                                appRepository = appRepository,
                                onBack = { showDrawer = false }
                            )
                        } else {
                            HomeScreen(
                                favoriteApps = favoriteApps,
                                onNavigateToDrawer = { showDrawer = true },
                                onAppListHold = { colorIndex = (colorIndex + 1) % colors.size }
                            )
                        }
                    }
                }
            }
        }
    }

}
