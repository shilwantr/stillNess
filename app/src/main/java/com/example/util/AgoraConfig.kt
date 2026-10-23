package com.example.util

import android.util.Log
import io.agora.media.RtcTokenBuilder2

object AgoraConfig {
    private const val TAG = "AgoraConfig"

    /**
     * Agora App ID from https://console.agora.io
     */
    var AGORA_APP_ID: String = "472c1d584d584e8e9a33daf9d5723615"

    /**
     * Agora App Certificate from https://console.agora.io (Project Management -> Security)
     * If your project has Primary Certificate enabled (secured mode), paste your 32-character
     * App Certificate here. The app will automatically generate real Agora RTC tokens on-the-fly!
     */
    var AGORA_APP_CERTIFICATE: String = "de77e716b80f4d17a98daff6fc4052f4"

    /**
     * Optional static temporary token generated from Agora Console.
     */
    var AGORA_TEMP_TOKEN: String = ""

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

    /**
     * Obtains an Agora RTC token for joining the specified channel.
     * If AGORA_APP_CERTIFICATE is configured, dynamically builds a signed token.
     * Otherwise returns AGORA_TEMP_TOKEN or an empty string (for Testing Mode: App ID only).
     */
    fun getToken(channelName: String, uid: Int = 0): String {
        if (AGORA_TEMP_TOKEN.isNotBlank()) {
            return AGORA_TEMP_TOKEN.trim()
        }
        val cert = AGORA_APP_CERTIFICATE.trim()
        val appId = AGORA_APP_ID.trim()
        if (cert.isNotBlank() && appId.isNotBlank()) {
            return try {
                val tokenBuilder = RtcTokenBuilder2()
                val expireTimeSeconds = 86400 // 24 hours validity
                val token = tokenBuilder.buildTokenWithUid(
                    appId,
                    cert,
                    channelName,
                    uid,
                    RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    expireTimeSeconds,
                    expireTimeSeconds
                )
                Log.d(TAG, "Successfully built dynamic token for channel=$channelName")
                token
            } catch (e: Exception) {
                Log.e(TAG, "Failed to build dynamic token: ${e.message}", e)
                ""
            }
        }
        return ""
    }
}
