package com.domain.mindphone.domain.repository

import com.domain.mindphone.domain.models.AppInfo
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for fetching and managing installed applications.
 * WHY: We use an interface so the UI layer is decoupled from the Android PackageManager,
 * making the app testable and easier to migrate if underlying mechanisms change.
 */
interface AppRepository {
    /**
     * Retrives a steady stream of installed application data.
     */
    fun getInstalledApps(): Flow<List<AppInfo>>

    /**
     * Set a specific application as "hidden" in the launcher drawer.
     */
    suspend fun setAppHidden(packageName: String, hidden: Boolean)
    
    /**
     * Add or remove a specific application from the favourite list (Home screen).
     */
    suspend fun setAppFavorite(packageName: String, favorite: Boolean)
}
