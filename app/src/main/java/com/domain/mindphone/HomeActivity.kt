package com.domain.mindphone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.domain.mindphone.data.repository.PackageManagerAppRepository
import com.domain.mindphone.domain.models.AppInfo
import com.domain.mindphone.ui.apps.AppDrawerScreen
import com.domain.mindphone.ui.settings.SettingsScreen
import com.domain.mindphone.ui.gatekeeper.GatekeeperScreen
import com.domain.mindphone.ui.home.HomeScreen
import com.domain.mindphone.ui.theme.MindPhoneTheme
import com.domain.mindphone.ui.theme.WarmSienna

class HomeActivity : ComponentActivity() {

    private lateinit var appRepository: PackageManagerAppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
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
                var showSettings by remember { mutableStateOf(false) }
                var gatekeeperTargetApp by remember { mutableStateOf<AppInfo?>(null) }

                val onAppClick: (AppInfo) -> Unit = { app ->
                    if (app.isGatekept) {
                        gatekeeperTargetApp = app
                    } else {
                        app.launchIntent?.let { startActivity(it) }
                        showDrawer = false // close drawer if an app is opened
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (showSettings) {
                            SettingsScreen(onNavigateBack = { showSettings = false })
                        } else if (gatekeeperTargetApp != null) {
                            BackHandler { gatekeeperTargetApp = null }
                            GatekeeperScreen(
                                appName = gatekeeperTargetApp!!.label,
                                onAllow = {
                                    gatekeeperTargetApp!!.launchIntent?.let { startActivity(it) }
                                    gatekeeperTargetApp = null
                                    showDrawer = false
                                },
                                onDenyClose = {
                                    gatekeeperTargetApp = null
                                }
                            )
                        } else if (showDrawer) {
                            AppDrawerScreen(
                                allApps = allApps,
                                appRepository = appRepository,
                                onBack = { showDrawer = false },
                                onAppClick = onAppClick
                            )
                        } else {
                            HomeScreen(
                                favoriteApps = favoriteApps,
                                onNavigateToDrawer = { showDrawer = true },
                                onNavigateToSettings = { showSettings = true },
                                onAppListHold = { colorIndex = (colorIndex + 1) % colors.size },
                                onAppClick = onAppClick
                            )
                        }
                    }
                }
            }
        }
    }
}
