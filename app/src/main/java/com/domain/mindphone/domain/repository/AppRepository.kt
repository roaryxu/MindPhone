package com.domain.mindphone.domain.repository

import com.domain.mindphone.domain.models.AppInfo
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun getInstalledApps(): Flow<List<AppInfo>>
    suspend fun setAppHidden(packageName: String, hidden: Boolean)
    suspend fun setAppFavorite(packageName: String, favorite: Boolean)
    suspend fun setAppGatekept(packageName: String, gatekept: Boolean)
}
