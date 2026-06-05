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

    private fun getGatekeptApps(): MutableSet<String> {
        return prefs.getStringSet("gatekept_apps", emptySet())?.toMutableSet() ?: mutableSetOf()
    }

    private val defaultProblematicApps = setOf("youtube", "tiktok", "facebook", "instagram")

    init {
        loadApps()
    }

    private fun loadApps() {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val currentHidden = getHiddenApps()
        val currentFavorites = getFavoriteApps()
        val currentGatekept = getGatekeptApps()

        val appList = resolveInfos.mapNotNull { resolveInfo ->
            val pkgName = resolveInfo.activityInfo.packageName
            if (pkgName == context.packageName) return@mapNotNull null
            
            val label = resolveInfo.loadLabel(pm).toString()
            val isDefaultGatekept = defaultProblematicApps.any { label.contains(it, ignoreCase = true) }
            val isGatekept = currentGatekept.contains(pkgName) || isDefaultGatekept
            
            AppInfo(
                packageName = pkgName,
                label = label,
                icon = resolveInfo.loadIcon(pm),
                launchIntent = pm.getLaunchIntentForPackage(pkgName),
                isHidden = currentHidden.contains(pkgName),
                isFavorite = currentFavorites.any { pkgName.contains(it) },
                isGatekept = isGatekept
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
            
            appsFlow.value = appsFlow.value.map {
                if (it.packageName == packageName) it.copy(isHidden = hidden) else it
            }
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
            
            // Fast optimistic update
            appsFlow.value = appsFlow.value.map {
                if (it.packageName == packageName) it.copy(isFavorite = favorite) else it
            }
        }
    }
    
    override suspend fun setAppGatekept(packageName: String, gatekept: Boolean) {
        withContext(Dispatchers.IO) {
            val current = getGatekeptApps()
            if (gatekept) current.add(packageName) else current.remove(packageName)
            prefs.edit().putStringSet("gatekept_apps", current).apply()
            
            appsFlow.value = appsFlow.value.map {
                if (it.packageName == packageName) it.copy(isGatekept = gatekept) else it
            }
        }
    }
}
