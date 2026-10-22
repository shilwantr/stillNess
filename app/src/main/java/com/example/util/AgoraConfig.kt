package com.example.util

object AgoraConfig {
    /**
     * Agora App ID from https://console.agora.io
     * Testing Mode (recommended for dev): Create an Agora project with "Testing mode: App ID without token".
     * Paste your App ID below or provide via .env.
     */
    var AGORA_APP_ID: String = "YOUR_AGORA_APP_ID"

    val isConfigured: Boolean
        get() = AGORA_APP_ID.isNotBlank() &&
                AGORA_APP_ID != "YOUR_AGORA_APP_ID" &&
                !AGORA_APP_ID.contains("YOUR_")

    /**
     * Generates a deterministic, unique Agora channel name for a therapy appointment session.
     * Both Client and Therapist join this exact channel.
     */
    fun getChannelName(appointmentId: String): String {
        val sanitized = appointmentId.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return "still_therapy_$sanitized"
    }
}
