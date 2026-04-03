package com.domain.mindphone.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.domain.mindphone.domain.models.AppInfo
import com.domain.mindphone.domain.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

/**
 * Implementation of [AppRepository] that reads directly from the device's PackageManager.
 * WHY: As an Android launcher, we need to query the OS for all resolvable MAIN/LAUNCHER intents.
 */
class PackageManagerAppRepository(
    private val context: Context
) : AppRepository {

    private val appsFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    private val prefs = context.getSharedPreferences("mindphone_prefs", Context.MODE_PRIVATE)

    private fun getHiddenApps(): MutableSet<String> {
        return prefs.getStringSet("hidden_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private fun getFavoriteApps(): MutableSet<String> {
        if (!prefs.contains("favorite_apps")) {
            return mutableSetOf("com.android.phone", "com.android.messaging", "com.google.android.calendar")
        }
        return prefs.getStringSet("favorite_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    init {
        loadApps()
    }

    private fun loadApps() {
        // Query PackageManager for all apps in the launcher category
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val currentHidden = getHiddenApps()
        val currentFavorites = getFavoriteApps()

        val appList = resolveInfos.mapNotNull { resolveInfo ->
            val pkgName = resolveInfo.activityInfo.packageName
            // Filter out our own launcher to prevent recursive loops
            if (pkgName == context.packageName) return@mapNotNull null
            
            AppInfo(
                packageName = pkgName,
                label = resolveInfo.loadLabel(pm).toString(),
                icon = resolveInfo.loadIcon(pm),
                launchIntent = pm.getLaunchIntentForPackage(pkgName),
                isHidden = currentHidden.contains(pkgName),
                isFavorite = currentFavorites.any { pkgName.contains(it) } 
            )
        }.sortedBy { it.label.lowercase() }
        
        appsFlow.value = appList
    }

    override fun getInstalledApps(): Flow<List<AppInfo>> = appsFlow

    override suspend fun setAppHidden(packageName: String, hidden: Boolean) {
        withContext(Dispatchers.IO) {
            val current = getHiddenApps()
            if (hidden) current.add(packageName) else current.remove(packageName)
            prefs.edit().putStringSet("hidden_apps", current).apply()
            loadApps()
        }
    }

    override suspend fun setAppFavorite(packageName: String, favorite: Boolean) {
        withContext(Dispatchers.IO) {
            val current = getFavoriteApps()
            if (favorite) {
                current.add(packageName)
            } else {
                current.remove(packageName)
                val toRemove = current.filter { packageName.contains(it) }
                current.removeAll(toRemove.toSet())
            }
            prefs.edit().putStringSet("favorite_apps", current).apply()
            loadApps()
        }
    }
}
