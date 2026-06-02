package com.domain.mindphone.domain.models

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Explicit interface for data structures representing an installed application.
 * Using a data class provides compile-time safety and clear expected structure.
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val launchIntent: Intent?,
    val isHidden: Boolean = false,
    val isFavorite: Boolean = false,
    val isGatekept: Boolean = false
)
