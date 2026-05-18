package com.domain.mindphone.domain.config

object MindfulAppConfig {
    // Set of package names for apps that should trigger the Mindful Gatekeeper
    private val problematicApps = setOf(
        "com.google.android.youtube", // YouTube
        "com.instagram.android",      // Instagram
        "com.facebook.katana",        // Facebook
        "com.linkedin.android"        // LinkedIn
    )

    /**
     * Checks if the given app package name is on the mindful gatekeeper list.
     */
    fun isProblematicApp(packageName: String): Boolean {
        return problematicApps.contains(packageName)
    }
}