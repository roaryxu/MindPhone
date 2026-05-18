package com.domain.mindphone.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.os.BatteryManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.domain.mindphone.domain.models.AppInfo
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    favoriteApps: List<AppInfo>,
    onNavigateToDrawer: () -> Unit,
    onNavigateToChat: () -> Unit = {},
    onAppListHold: () -> Unit = {},
    onAppClick: (AppInfo) -> Unit = {}
) {
    val context = LocalContext.current
    
    var timeText by remember { mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())) }
    var batteryPct by remember { mutableIntStateOf(100) }

    LaunchedEffect(Unit) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        while (true) {
            timeText = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 48.dp, bottom = 48.dp, start = 24.dp, end = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val interactionSourceHeader = remember { MutableInteractionSource() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 24.dp)
                    .clickable(
                        interactionSource = interactionSourceHeader, 
                        indication = null,
                        onClick = {
                            val intent = Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                            safelyStartIntent(context, intent)
                        }
                    )
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                BatteryIconWithText(percentage = batteryPct)
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(favoriteApps) { app ->
                        AppListItem(
                            appInfo = app,
                            onClick = {
                                onAppClick(app)
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            BottomIconDock()
        }

        // Persona Tester Chat Bot
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 180.dp) // Placed between App List and top
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onNavigateToChat() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SmartToy,
                contentDescription = "Test Persona",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
        
        var isHoldingAppList by remember { mutableStateOf(false) }
        var holdProgressAppList by remember { mutableFloatStateOf(0f) }

        LaunchedEffect(isHoldingAppList) {
            if (isHoldingAppList) {
                while (holdProgressAppList < 1f) {
                    delay(16)
                    holdProgressAppList += 16f / 3000f
                }
                if (holdProgressAppList >= 1f) {
                    isHoldingAppList = false
                    holdProgressAppList = 0f
                    onAppListHold()
                }
            } else {
                holdProgressAppList = 0f
            }
        }

        val scaleAppList by animateFloatAsState(targetValue = if (isHoldingAppList) 0.85f else 1f)

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp)
                .size(48.dp)
                .scale(scaleAppList)
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isHoldingAppList = true
                            tryAwaitRelease()
                            isHoldingAppList = false
                        },
                        onTap = {
                            onNavigateToDrawer()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Apps,
                contentDescription = "All Apps",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun AppListItem(
    appInfo: AppInfo,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Fix #1: Micro-animation decoupled from detectTapGestures, allowing normal scroll
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = onClick
            )
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = appInfo.label,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// Fix #6: Refined minimalist Bottom Icon Dock
@Composable
fun BottomIconDock() {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(36.dp))
            .background(Color.White.copy(alpha = 0.05f)) // Glassmorphism
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DockIcon(
            icon = Icons.Rounded.Phone,
            onClick = {
                val intent = Intent(Intent.ACTION_DIAL)
                context.startActivity(intent)
            }
        )
        DockIcon(
            icon = Icons.Rounded.Textsms,
            onClick = {
                val intent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_MESSAGING)
                }
                safelyStartIntent(context, intent)
            }
        )
        DockIcon(
            icon = Icons.Rounded.Language,
            onClick = {
                // Specific user validation: default web browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
                safelyStartIntent(context, intent)
            }
        )
        DockIcon(
            icon = Icons.Rounded.CameraAlt,
            onClick = {
                val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                safelyStartIntent(context, intent)
            }
        )
        HoldToExitDockIcon(
            icon = Icons.Rounded.ExitToApp,
            onExit = {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                safelyStartIntent(context, intent)
            }
        )
    }
}

// Fallback mechanism to prevent crashes if standard activity missing
private fun safelyStartIntent(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun DockIcon(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.85f else 1f)

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun HoldToExitDockIcon(
    icon: ImageVector,
    onExit: () -> Unit
) {
    var isHolding by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isHolding) {
        if (isHolding) {
            while (holdProgress < 1f) {
                delay(16)
                holdProgress += 0.03f 
            }
            if (holdProgress >= 1f) {
                isHolding = false
                holdProgress = 0f
                onExit()
            }
        } else {
            while (holdProgress > 0f) {
                delay(8)
                holdProgress -= 0.05f
            }
            holdProgress = 0f
        }
    }

    val scale by animateFloatAsState(targetValue = if (isHolding) 0.85f else 1f)

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isHolding = true
                        tryAwaitRelease()
                        isHolding = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Visual indicator for holding to exit.
        if (holdProgress > 0f) {
            CircularProgressIndicator(
                progress = holdProgress,
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                trackColor = Color.Transparent
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = "Hold to Exit",
            tint = MaterialTheme.colorScheme.primary, // Distinct color to warn user
            modifier = Modifier.size(28.dp)
        )
    }
}
@Composable
fun BatteryIconWithText(percentage: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .border(
                    width = 1.5.dp, 
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$percentage",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
            )
        }
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), 
                    shape = RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp)
                )
        )
    }
}
