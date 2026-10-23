package com.example.util

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AgoraCallState(
    val isJoined: Boolean = false,
    val channelName: String = "",
    val isVideoMode: Boolean = true,
    val localUid: Int = 0,
    val remoteUid: Int? = null,
    val isRemoteVideoMuted: Boolean = false,
    val isLocalVideoMuted: Boolean = false,
    val isLocalAudioMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val connectionQuality: String = "Connecting to session...",
    val errorMessage: String? = null
)

class AgoraRtcManager(private val context: Context) {

    companion object {
        private const val TAG = "AgoraRtcManager"
    }

    private var rtcEngine: RtcEngine? = null

    private val _callState = MutableStateFlow(AgoraCallState())
    val callState: StateFlow<AgoraCallState> = _callState.asStateFlow()

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.d(TAG, "onJoinChannelSuccess: channel=$channel, uid=$uid, elapsed=$elapsed")
            _callState.value = _callState.value.copy(
                isJoined = true,
                channelName = channel ?: "",
                localUid = uid,
                connectionQuality = "Waiting for practitioner..."
            )
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "onUserJoined: remoteUid=$uid, elapsed=$elapsed")
            _callState.value = _callState.value.copy(
                remoteUid = uid,
                isRemoteVideoMuted = false,
                connectionQuality = "Connected • Encrypted HD"
            )
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "onUserOffline: uid=$uid, reason=$reason")
            if (_callState.value.remoteUid == uid) {
                _callState.value = _callState.value.copy(
                    remoteUid = null,
                    connectionQuality = "Participant disconnected"
                )
            }
        }

        override fun onUserMuteVideo(uid: Int, muted: Boolean) {
            Log.d(TAG, "onUserMuteVideo: uid=$uid, muted=$muted")
            if (_callState.value.remoteUid == uid) {
                _callState.value = _callState.value.copy(isRemoteVideoMuted = muted)
            }
        }

        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            val quality = when {
                txQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_EXCELLENT -> "Excellent (48kHz HD)"
                txQuality == Constants.QUALITY_GOOD || rxQuality == Constants.QUALITY_GOOD -> "Good (Encrypted)"
                txQuality == Constants.QUALITY_POOR || rxQuality == Constants.QUALITY_POOR -> "Weak Network"
                else -> "Connected • Encrypted"
            }
            if (_callState.value.remoteUid != null) {
                _callState.value = _callState.value.copy(connectionQuality = quality)
            }
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora RTC Error: code=$err")
            if (err == Constants.ERR_INVALID_APP_ID) {
                _callState.value = _callState.value.copy(
                    errorMessage = "Invalid Agora App ID. Please configure your App ID in AgoraConfig."
                )
            }
        }
    }

    /**
     * Initializes the Agora RTC Engine if not already created.
     */
    fun initEngine(): Boolean {
        if (rtcEngine != null) return true

        val appId = AgoraConfig.AGORA_APP_ID
        if (!AgoraConfig.isConfigured) {
            Log.w(TAG, "Agora App ID is not configured yet. Prompting user configuration.")
            _callState.value = _callState.value.copy(
                errorMessage = "Agora App ID not configured. Set your App ID in AgoraConfig.kt"
            )
            return false
        }

        return try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = appId
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            }
            rtcEngine = RtcEngine.create(config).apply {
                enableAudio()
                setEnableSpeakerphone(true)
            }
            Log.d(TAG, "Agora RtcEngine successfully initialized.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RtcEngine: ${e.message}", e)
            _callState.value = _callState.value.copy(
                errorMessage = "Failed to initialize Agora RTC: ${e.localizedMessage}"
            )
            false
        }
    }

    /**
     * Joins an Agora therapy call channel.
     * @param channelName The shared deterministic room name (e.g. "still_therapy_<appointmentId>")
     * @param isVideo Whether video is enabled for this session
     * @param token Optional Agora security token (empty string for Testing Mode: App ID without token)
     */
    fun joinSession(channelName: String, isVideo: Boolean, token: String = ""): Boolean {
        if (rtcEngine == null && !initEngine()) {
            return false
        }

        try {
            val engine = rtcEngine ?: return false

            _callState.value = AgoraCallState(
                isJoined = false,
                channelName = channelName,
                isVideoMode = isVideo,
                isSpeakerOn = true,
                connectionQuality = "Connecting to session..."
            )

            if (isVideo) {
                engine.enableVideo()
                engine.startPreview()
            } else {
                engine.disableVideo()
            }

            val options = ChannelMediaOptions().apply {
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                autoSubscribeAudio = true
                autoSubscribeVideo = isVideo
                publishMicrophoneTrack = true
                publishCameraTrack = isVideo
            }

            val result = engine.joinChannel(token, channelName, 0, options)
            Log.d(TAG, "joinChannel result code=$result for channel=$channelName (isVideo=$isVideo)")
            return result == Constants.ERR_OK
        } catch (e: Exception) {
            Log.e(TAG, "Error joining session: ${e.message}", e)
            _callState.value = _callState.value.copy(errorMessage = e.localizedMessage)
            return false
        }
    }

    /**
     * Binds local camera preview to a SurfaceView inside Jetpack Compose.
     */
    fun setupLocalVideo(surfaceView: SurfaceView) {
        val engine = rtcEngine ?: return
        try {
            surfaceView.setZOrderMediaOverlay(true)
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
            engine.setupLocalVideo(canvas)
            Log.d(TAG, "setupLocalVideo configured.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setupLocalVideo: ${e.message}")
        }
    }

    /**
     * Binds remote participant camera stream to a SurfaceView inside Jetpack Compose.
     */
    fun setupRemoteVideo(surfaceView: SurfaceView, uid: Int) {
        val engine = rtcEngine ?: return
        try {
            val canvas = VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
            engine.setupRemoteVideo(canvas)
            Log.d(TAG, "setupRemoteVideo configured for uid=$uid.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setupRemoteVideo: ${e.message}")
        }
    }

    /**
     * Toggles microphone mute.
     */
    fun toggleMicrophone(isMuted: Boolean) {
        rtcEngine?.muteLocalAudioStream(isMuted)
        _callState.value = _callState.value.copy(isLocalAudioMuted = isMuted)
    }

    /**
     * Toggles local video stream on/off.
     */
    fun toggleLocalVideo(isVideoDisabled: Boolean) {
        val engine = rtcEngine ?: return
        engine.muteLocalVideoStream(isVideoDisabled)
        if (isVideoDisabled) {
            engine.stopPreview()
        } else {
            engine.startPreview()
        }
        _callState.value = _callState.value.copy(isLocalVideoMuted = isVideoDisabled)
    }

    /**
     * Switches between front and back camera.
     */
    fun switchCamera() {
        rtcEngine?.switchCamera()
    }

    /**
     * Toggles audio routing between loudspeaker and earpiece.
     */
    fun toggleSpeakerphone(isSpeakerOn: Boolean) {
        rtcEngine?.setEnableSpeakerphone(isSpeakerOn)
        _callState.value = _callState.value.copy(isSpeakerOn = isSpeakerOn)
    }

    /**
     * Leaves the active Agora session and resets media channels.
     */
    fun leaveSession() {
        try {
            rtcEngine?.stopPreview()
            rtcEngine?.leaveChannel()
            Log.d(TAG, "Left Agora session cleanly.")
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving session: ${e.message}")
        } finally {
            _callState.value = AgoraCallState(isJoined = false)
        }
    }

    /**
     * Destroys the engine upon app termination.
     */
    fun destroy() {
        leaveSession()
        RtcEngine.destroy()
        rtcEngine = null
    }
}
