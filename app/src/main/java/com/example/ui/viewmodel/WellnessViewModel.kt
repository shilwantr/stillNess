package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DailyMetricEntity
import com.example.data.local.entities.FavoriteQuoteEntity
import com.example.data.local.entities.JournalEntryEntity
import com.example.data.local.entities.PaymentRecordEntity
import com.example.data.local.entities.WaterEntryEntity
import com.example.data.local.entities.WellnessSessionEntity
import com.example.data.repository.WellnessRepository
import com.example.domain.model.AmbientSound
import com.example.domain.model.BreathingMode
import com.example.domain.model.MeditationTrack
import com.example.domain.model.Quote
import com.example.domain.model.Therapist
import com.example.domain.model.UserRole
import com.example.ui.components.ChartPoint
import com.example.ui.components.StillTab
import com.example.ui.glass.MoodState
import com.example.util.AudioSoundManager
import com.example.util.NotificationHelper
import com.example.util.StepSensorManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

enum class BreathingPhase(val label: String, val cueText: String) {
    INHALE("Inhale", "Fill your lungs with cool, calming air..."),
    HOLD_INHALE("Hold", "Pause and rest in the stillness..."),
    EXHALE("Exhale", "Slowly release all tension from your body..."),
    HOLD_EXHALE("Hold", "Rest peacefully before the next breath...")
}

enum class MetricViewType(val label: String, val unit: String) {
    CALORIES("Active Energy", "kcal"),
    STEPS("Steps", "steps"),
    WATER("Water", "ml")
}

enum class TimeframeView(val label: String) {
    WEEK("Week"),
    MONTH("Month"),
    YEAR("Year")
}

data class ActiveCallState(
    val isActive: Boolean = false,
    val isIncoming: Boolean = false,
    val appointment: AppointmentEntity? = null,
    val therapistName: String = "",
    val therapistTitle: String = "",
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val connectionQuality: String = "48kHz HD Audio • Encrypted"
)

class WellnessViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val db = AppDatabase.getDatabase(application)
    val repository = WellnessRepository(db.wellnessDao())
    val audioSoundManager = AudioSoundManager(application)
    val notificationHelper = NotificationHelper(application)

    private val stepSensorManager: StepSensorManager

    // Navigation Tab
    private val _currentTab = MutableStateFlow(StillTab.TODAY)
    val currentTab: StateFlow<StillTab> = _currentTab.asStateFlow()

    // Auth & User State
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _therapistSaveSuccess = MutableStateFlow<String?>(null)
    val therapistSaveSuccess: StateFlow<String?> = _therapistSaveSuccess.asStateFlow()

    // Dynamic Live Therapists list streamed from Firestore
    val therapists: StateFlow<List<Therapist>> = repository.getTherapistsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Role
    private val _userRole = MutableStateFlow(UserRole.CLIENT)
    val userRole: StateFlow<UserRole> = _userRole.asStateFlow()

    // Daily Mood State
    private val _selectedMood = MutableStateFlow(MoodState.CALM)
    val selectedMood: StateFlow<MoodState> = _selectedMood.asStateFlow()

    // Ambient Background Theme (NT / AURORA_SILK as initial theme)
    private val _ambientTheme = MutableStateFlow(com.example.ui.glass.AmbientTheme.AURORA_SILK)
    val ambientTheme: StateFlow<com.example.ui.glass.AmbientTheme> = _ambientTheme.asStateFlow()

    fun setAmbientTheme(theme: com.example.ui.glass.AmbientTheme) {
        _ambientTheme.value = theme
    }

    fun cycleAmbientTheme() {
        val themes = com.example.ui.glass.AmbientTheme.values()
        val nextIdx = (themes.indexOf(_ambientTheme.value) + 1) % themes.size
        _ambientTheme.value = themes[nextIdx]
    }

    // Daily Metrics Flow
    val todayMetric: StateFlow<DailyMetricEntity?> = repository.getDailyMetricFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val recentMetrics: StateFlow<List<DailyMetricEntity>> = repository.getRecentMetricsFlow(14)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val waterEntries: StateFlow<List<WaterEntryEntity>> = repository.getWaterEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = repository.getAllAppointmentsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val payments: StateFlow<List<PaymentRecordEntity>> = repository.getAllPaymentsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favoriteQuotes: StateFlow<List<FavoriteQuoteEntity>> = repository.getFavoriteQuotesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val completedSessions: StateFlow<List<WellnessSessionEntity>> = repository.getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.getJournalEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dashboard Card Visibility State
    private val _isQuoteCardVisible = MutableStateFlow(true)
    val isQuoteCardVisible: StateFlow<Boolean> = _isQuoteCardVisible.asStateFlow()

    private val _isMoodCardVisible = MutableStateFlow(true)
    val isMoodCardVisible: StateFlow<Boolean> = _isMoodCardVisible.asStateFlow()

    private val _isTelemetryCardVisible = MutableStateFlow(false)
    val isTelemetryCardVisible: StateFlow<Boolean> = _isTelemetryCardVisible.asStateFlow()

    private val _isTilesCardVisible = MutableStateFlow(false)
    val isTilesCardVisible: StateFlow<Boolean> = _isTilesCardVisible.asStateFlow()

    private val _isBreathworkCardVisible = MutableStateFlow(true)
    val isBreathworkCardVisible: StateFlow<Boolean> = _isBreathworkCardVisible.asStateFlow()

    private val _isHydrationCardVisible = MutableStateFlow(true)
    val isHydrationCardVisible: StateFlow<Boolean> = _isHydrationCardVisible.asStateFlow()

    private val _isWalkingCardVisible = MutableStateFlow(true)
    val isWalkingCardVisible: StateFlow<Boolean> = _isWalkingCardVisible.asStateFlow()

    fun toggleQuoteCard() { _isQuoteCardVisible.value = !_isQuoteCardVisible.value }
    fun toggleMoodCard() { _isMoodCardVisible.value = !_isMoodCardVisible.value }
    fun toggleTelemetryCard() { _isTelemetryCardVisible.value = !_isTelemetryCardVisible.value }
    fun toggleTilesCard() { _isTilesCardVisible.value = !_isTilesCardVisible.value }
    fun toggleBreathworkCard() { _isBreathworkCardVisible.value = !_isBreathworkCardVisible.value }
    fun toggleHydrationCard() { _isHydrationCardVisible.value = !_isHydrationCardVisible.value }
    fun toggleWalkingCard() { _isWalkingCardVisible.value = !_isWalkingCardVisible.value }

    // Graph State
    private val _selectedMetricType = MutableStateFlow(MetricViewType.CALORIES)
    val selectedMetricType: StateFlow<MetricViewType> = _selectedMetricType.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(TimeframeView.WEEK)
    val selectedTimeframe: StateFlow<TimeframeView> = _selectedTimeframe.asStateFlow()

    private val _selectedGraphIndex = MutableStateFlow(2) // Defaults to Wednesday / Today
    val selectedGraphIndex: StateFlow<Int> = _selectedGraphIndex.asStateFlow()

    // Breathing State
    private val _breathingMode = MutableStateFlow(BreathingMode.BOX)
    val breathingMode: StateFlow<BreathingMode> = _breathingMode.asStateFlow()

    private val _breathingDurationMinutes = MutableStateFlow(5)
    val breathingDurationMinutes: StateFlow<Int> = _breathingDurationMinutes.asStateFlow()

    private val _isBreathingActive = MutableStateFlow(false)
    val isBreathingActive: StateFlow<Boolean> = _isBreathingActive.asStateFlow()

    private val _isBreathingPaused = MutableStateFlow(false)
    val isBreathingPaused: StateFlow<Boolean> = _isBreathingPaused.asStateFlow()

    private val _breathingPhase = MutableStateFlow(BreathingPhase.INHALE)
    val breathingPhase: StateFlow<BreathingPhase> = _breathingPhase.asStateFlow()

    private val _phaseRemainingSeconds = MutableStateFlow(4)
    val phaseRemainingSeconds: StateFlow<Int> = _phaseRemainingSeconds.asStateFlow()

    private val _totalBreathingSecondsRemaining = MutableStateFlow(300)
    val totalBreathingSecondsRemaining: StateFlow<Int> = _totalBreathingSecondsRemaining.asStateFlow()

    private val _orbExpansionFraction = MutableStateFlow(0.5f)
    val orbExpansionFraction: StateFlow<Float> = _orbExpansionFraction.asStateFlow()

    private var breathingJob: Job? = null

    // Track download progress for Meditations, Relaxing Music and Ambient Sounds
    val meditationDownloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val relaxingMusicDownloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val ambientDownloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())

    // Dynamic Music & Meditation tracks streamed directly from Firestore Database
    val meditationTracks: StateFlow<List<MeditationTrack>> = repository.getMeditationTracksFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dynamic Relaxing Music library streamed directly from Firestore Database
    val relaxingMusic: StateFlow<List<MeditationTrack>> = repository.getRelaxingMusicFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dynamic Ambient Soundtracks streamed directly from Firestore Database
    val ambientSounds: StateFlow<List<AmbientSound>> = repository.getAmbientSoundsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Meditation & Ambient Audio State
    private val _activeMeditationTrack = MutableStateFlow<MeditationTrack?>(null)
    val activeMeditationTrack: StateFlow<MeditationTrack?> = _activeMeditationTrack.asStateFlow()

    private val _isMeditationPlaying = MutableStateFlow(false)
    val isMeditationPlaying: StateFlow<Boolean> = _isMeditationPlaying.asStateFlow()

    private val _meditationRemainingSeconds = MutableStateFlow(600)
    val meditationRemainingSeconds: StateFlow<Int> = _meditationRemainingSeconds.asStateFlow()

    private var meditationJob: Job? = null

    private val _activeAmbientSound = MutableStateFlow<AmbientSound?>(null)
    val activeAmbientSound: StateFlow<AmbientSound?> = _activeAmbientSound.asStateFlow()

    private val _ambientVolume = MutableStateFlow(0.7f)
    val ambientVolume: StateFlow<Float> = _ambientVolume.asStateFlow()

    // Active Voice Call State
    private val _callState = MutableStateFlow(ActiveCallState())
    val callState: StateFlow<ActiveCallState> = _callState.asStateFlow()
    private var callTimerJob: Job? = null

    // Fullscreen Emo Robot Screen State
    private val _isEmoScreenOpen = MutableStateFlow(false)
    val isEmoScreenOpen: StateFlow<Boolean> = _isEmoScreenOpen.asStateFlow()

    fun setEmoScreenOpen(isOpen: Boolean) {
        _isEmoScreenOpen.value = isOpen
    }

    // Daily Rotating Quote (changes every time the user opens the app)
    private val _currentQuote = MutableStateFlow(repository.getCuratedQuotes().first())
    val currentQuote: StateFlow<Quote> = _currentQuote.asStateFlow()
    private var hasRotatedInitialLaunch = false

    init {
        // Observe Firebase Authentication State
        try {
            _currentUser.value = auth.currentUser
            auth.addAuthStateListener { fa ->
                val user = fa.currentUser
                _currentUser.value = user
                if (user != null) {
                    viewModelScope.launch {
                        try {
                            val role = repository.getUserRole(user.uid)
                            _userRole.value = role
                        } catch (_: Exception) {}
                    }
                }
            }
        } catch (_: Exception) {}

        // Rotate quote on initial ViewModel creation
        rotateQuoteOnAppOpen()

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Sync favorite state when favoriteQuotes emits
        viewModelScope.launch {
            favoriteQuotes.collect { favList ->
                val curr = _currentQuote.value
                val isFav = favList.any { it.id == curr.id }
                if (curr.isFavorite != isFav) {
                    _currentQuote.value = curr.copy(isFavorite = isFav)
                }
            }
        }

        stepSensorManager = StepSensorManager(application) { stepIncrement ->
            viewModelScope.launch {
                repository.updateSteps(stepIncrement)
            }
        }
        stepSensorManager.startListening()
    }

    /**
     * Called when the app is opened/started so the top quote card displays a fresh quote
     * every time the user opens the app (backed by WellnessRepository and ready for Phase 2 DB).
     */
    fun rotateQuoteOnAppOpen(isFromActivityStart: Boolean = false) {
        if (isFromActivityStart && !hasRotatedInitialLaunch) {
            hasRotatedInitialLaunch = true
            return
        }
        val next = repository.getNextQuoteOnAppOpen(appContext)
        val isFav = favoriteQuotes.value.any { it.id == next.id }
        _currentQuote.value = next.copy(isFavorite = isFav)
    }

    /**
     * Manually advances to the next quote when the user taps the refresh button on the top quote card.
     */
    fun nextQuote() {
        val next = repository.getNextQuoteOnAppOpen(appContext)
        val isFav = favoriteQuotes.value.any { it.id == next.id }
        _currentQuote.value = next.copy(isFavorite = isFav)
        audioSoundManager.triggerSubtleHaptic()
    }

    override fun onCleared() {
        super.onCleared()
        stepSensorManager.stopListening()
        audioSoundManager.release()
        breathingJob?.cancel()
        meditationJob?.cancel()
        callTimerJob?.cancel()
    }

    fun setTab(tab: StillTab) {
        _currentTab.value = tab
    }

    fun selectMood(mood: MoodState) {
        _selectedMood.value = mood
        audioSoundManager.triggerSubtleHaptic()
    }

    companion object {
        private const val WEB_CLIENT_ID = "1044318322389-8ojsn2r0delim29f701jjrocj502fd8b.apps.googleusercontent.com"
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { if (cont.isActive) cont.resume(it) }
            addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
            addOnCanceledListener { cont.cancel() }
        }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            try {
                val credentialManager = CredentialManager.create(activityContext)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = activityContext
                )
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    auth.signInWithCredential(authCredential).awaitTask()
                    _currentUser.value = auth.currentUser
                    auth.currentUser?.let { user ->
                        val role = repository.getUserRole(user.uid)
                        _userRole.value = role
                    }
                } else {
                    _authError.value = "Unrecognized credential type"
                }
            } catch (e: GetCredentialException) {
                _authError.value = "Google Sign-In was cancelled."
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Authentication failed"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _userRole.value = UserRole.CLIENT
        _authError.value = null
        _therapistSaveSuccess.value = null
    }

    fun setUserRole(role: UserRole) {
        _userRole.value = role
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                repository.saveUserRole(user.uid, role)
            } catch (_: Exception) {}
        }
    }

    fun registerTherapistProfile(
        title: String,
        specialty: String,
        hourlyRate: Double,
        bio: String
    ) {
        val user = _currentUser.value
        if (user == null) {
            _authError.value = "Please sign in before registering as a therapist."
            return
        }
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            _therapistSaveSuccess.value = null
            try {
                val therapist = Therapist(
                    id = user.uid,
                    name = user.displayName ?: "Therapist",
                    title = title.ifBlank { "Licensed Therapist" },
                    specialty = specialty.ifBlank { "Mindfulness & Wellbeing" },
                    credentials = "Verified Practitioner",
                    yearsExperience = 5,
                    languages = listOf("English"),
                    bio = bio.ifBlank { "Compassionate mental wellbeing support and guidance." },
                    rating = 5.0f,
                    reviewCount = 0,
                    hourlyRate = if (hourlyRate > 0) hourlyRate else 75.0,
                    isVerified = true,
                    isOnline = true,
                    availableSlots = listOf("Today 3:00 PM", "Tomorrow 10:00 AM", "Thursday 2:00 PM"),
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString() ?: ""
                )
                repository.saveTherapistProfile(therapist)
                repository.saveUserRole(user.uid, UserRole.THERAPIST)
                _userRole.value = UserRole.THERAPIST
                _therapistSaveSuccess.value = "Registered successfully! You are now listed in the Therapy section."
                audioSoundManager.triggerSubtleHaptic()
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Failed to save therapist profile"
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun clearAuthMessages() {
        _authError.value = null
        _therapistSaveSuccess.value = null
    }

    fun setMetricType(type: MetricViewType) {
        _selectedMetricType.value = type
    }

    fun setTimeframe(tf: TimeframeView) {
        _selectedTimeframe.value = tf
    }

    fun selectGraphIndex(index: Int) {
        _selectedGraphIndex.value = index
    }

    // Hydration actions
    fun addWater(amountMl: Int, vessel: String = "Glass") {
        viewModelScope.launch {
            repository.addWater(amountMl, vessel)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun undoWater() {
        viewModelScope.launch {
            repository.undoLatestWater()
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun simulateStepIncrement(steps: Int = 250) {
        viewModelScope.launch {
            repository.updateSteps(steps)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    // Breathing Session
    fun setBreathingMode(mode: BreathingMode) {
        if (!_isBreathingActive.value) {
            _breathingMode.value = mode
            _phaseRemainingSeconds.value = mode.inhaleSeconds
        }
    }

    fun setBreathingDurationMinutes(minutes: Int) {
        if (!_isBreathingActive.value) {
            _breathingDurationMinutes.value = minutes
            _totalBreathingSecondsRemaining.value = minutes * 60
        }
    }

    fun startBreathingSession() {
        if (_isBreathingActive.value && !_isBreathingPaused.value) return
        if (_isBreathingPaused.value) {
            _isBreathingPaused.value = false
            notificationHelper.showPlaybackNotification(_breathingMode.value.title, "Active Breathing Practice • Chimes On", isPlaying = true)
            runBreathingCycle()
            return
        }

        _isBreathingActive.value = true
        _isBreathingPaused.value = false
        _breathingPhase.value = BreathingPhase.INHALE
        _phaseRemainingSeconds.value = _breathingMode.value.inhaleSeconds
        _totalBreathingSecondsRemaining.value = _breathingDurationMinutes.value * 60
        _orbExpansionFraction.value = 0.28f
        audioSoundManager.resetBreathingChimeMelody()
        audioSoundManager.playBreathingPhaseChime()
        notificationHelper.showPlaybackNotification(_breathingMode.value.title, "Active Breathing Practice • Chimes On", isPlaying = true)
        runBreathingCycle()
    }

    fun pauseBreathingSession() {
        _isBreathingPaused.value = true
        breathingJob?.cancel()
        notificationHelper.showPlaybackNotification(_breathingMode.value.title, "Breathing Practice Paused", isPlaying = false)
    }

    fun stopBreathingSession() {
        _isBreathingActive.value = false
        _isBreathingPaused.value = false
        breathingJob?.cancel()
        _orbExpansionFraction.value = 0.5f
        audioSoundManager.resetBreathingChimeMelody()
        notificationHelper.dismissPlaybackNotification()

        val completedSecs = (_breathingDurationMinutes.value * 60) - _totalBreathingSecondsRemaining.value
        if (completedSecs > 15) {
            viewModelScope.launch {
                repository.recordSession(
                    type = "BREATHING",
                    title = _breathingMode.value.title,
                    durationSeconds = completedSecs
                )
            }
        }
    }

    /**
     * Smooth 50ms tick breathing engine with natural sine-ease-in-out curve so the
     * 3D Liquid Glass Bubble expands and contracts fluidly without 1-second jumps.
     */
    private fun runBreathingCycle() {
        breathingJob?.cancel()
        breathingJob = viewModelScope.launch {
            val mode = _breathingMode.value
            val tickMs = 50L

            suspend fun runSmoothPhase(
                phase: BreathingPhase,
                chimeHz: Float,
                durationSeconds: Int,
                startExpansion: Float,
                endExpansion: Float,
                isHold: Boolean = false
            ) {
                if (durationSeconds <= 0 || !_isBreathingActive.value || _isBreathingPaused.value) return
                _breathingPhase.value = phase
                audioSoundManager.playBreathingPhaseChime(chimeHz)

                val totalSteps = (durationSeconds * 1000L / tickMs).toInt().coerceAtLeast(1)
                var elapsedMs = 0L

                for (step in 1..totalSteps) {
                    if (!_isBreathingActive.value || _isBreathingPaused.value || _totalBreathingSecondsRemaining.value <= 0) break

                    val progress = step.toFloat() / totalSteps
                    // Smooth cosine ease-in-out interpolation
                    val eased = 0.5f * (1f - cos(PI.toFloat() * progress))
                    _orbExpansionFraction.value = if (isHold) {
                        // Gentle weightless micro-buoyancy during breath hold
                        startExpansion + sin(progress * 2f * PI.toFloat()) * 0.025f
                    } else {
                        startExpansion + (endExpansion - startExpansion) * eased
                    }

                    val remSec = ceil(durationSeconds * (1f - progress)).toInt().coerceIn(1, durationSeconds)
                    _phaseRemainingSeconds.value = remSec

                    delay(tickMs)
                    elapsedMs += tickMs
                    if (elapsedMs >= 1000L) {
                        elapsedMs -= 1000L
                        _totalBreathingSecondsRemaining.value =
                            (_totalBreathingSecondsRemaining.value - 1).coerceAtLeast(0)
                    }
                }
            }

            while (_isBreathingActive.value && !_isBreathingPaused.value && _totalBreathingSecondsRemaining.value > 0) {
                // 1. Inhale
                runSmoothPhase(
                    phase = BreathingPhase.INHALE,
                    chimeHz = 528f,
                    durationSeconds = mode.inhaleSeconds,
                    startExpansion = 0.28f,
                    endExpansion = 1.0f
                )

                // 2. Hold after Inhale
                if (mode.holdAfterInhaleSeconds > 0) {
                    runSmoothPhase(
                        phase = BreathingPhase.HOLD_INHALE,
                        chimeHz = 440f,
                        durationSeconds = mode.holdAfterInhaleSeconds,
                        startExpansion = 1.0f,
                        endExpansion = 1.0f,
                        isHold = true
                    )
                }

                // 3. Exhale
                runSmoothPhase(
                    phase = BreathingPhase.EXHALE,
                    chimeHz = 396f,
                    durationSeconds = mode.exhaleSeconds,
                    startExpansion = 1.0f,
                    endExpansion = 0.28f
                )

                // 4. Hold after Exhale
                if (mode.holdAfterExhaleSeconds > 0) {
                    runSmoothPhase(
                        phase = BreathingPhase.HOLD_EXHALE,
                        chimeHz = 330f,
                        durationSeconds = mode.holdAfterExhaleSeconds,
                        startExpansion = 0.28f,
                        endExpansion = 0.28f,
                        isHold = true
                    )
                }
            }

            if (_totalBreathingSecondsRemaining.value <= 0) {
                stopBreathingSession()
            }
        }
    }

    // Meditation & Ambient Audio
    
    private fun getLocalAudioCacheFile(id: String): java.io.File {
        return java.io.File(appContext.cacheDir, "audio_cache_$id.mp3")
    }

    private fun downloadAudioIfNeeded(
        id: String,
        url: String,
        progressMap: MutableStateFlow<Map<String, Float>>,
        onComplete: () -> Unit
    ) {
        val file = getLocalAudioCacheFile(id)
        if (file.exists() && file.length() > 0) {
            progressMap.value = progressMap.value + (id to 1f)
            onComplete()
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient()
                val request = okhttp3.Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                response.body?.let { body ->
                    val totalBytes = body.contentLength()
                    var bytesCopied = 0L
                    val buffer = ByteArray(8 * 1024)
                    var bytes = body.source().read(buffer)
                    file.outputStream().use { out ->
                        while (bytes >= 0) {
                            out.write(buffer, 0, bytes)
                            bytesCopied += bytes
                            val progress = if (totalBytes > 0) bytesCopied.toFloat() / totalBytes else 0f
                            progressMap.value = progressMap.value + (id to progress)
                            bytes = body.source().read(buffer)
                        }
                    }
                }
                launch(kotlinx.coroutines.Dispatchers.Main) { onComplete() }
            } catch (_: Exception) {
                file.delete()
            }
        }
    }

    fun startMeditation(track: MeditationTrack, isRelaxingCategory: Boolean = false) {
        _activeMeditationTrack.value = track
        _meditationRemainingSeconds.value = track.durationMinutes * 60
        _isMeditationPlaying.value = true

        if (track.audioUrl.isNotBlank()) {
            val localFile = getLocalAudioCacheFile(track.id)
            val progressStateMap = if (isRelaxingCategory) relaxingMusicDownloadProgress else meditationDownloadProgress
            
            audioSoundManager.playAudioUrl(track.audioUrl, localCachePath = localFile.absolutePath)
            
            downloadAudioIfNeeded(
                id = track.id,
                url = track.audioUrl,
                progressMap = progressStateMap,
                onComplete = {
                    progressStateMap.value = progressStateMap.value + (track.id to 1f)
                }
            )
        } else {
            val baseFreq = if (track.baseFrequency > 0f) track.baseFrequency else 432f
            val secFreq = if (track.secondaryFrequency > 0f) track.secondaryFrequency else 648f
            audioSoundManager.startAmbientSound(baseFreq, secFreq)
        }

        notificationHelper.showPlaybackNotification(
            title = track.title,
            subtitle = "Playing • ${track.category} (${track.durationMinutes} min)",
            isPlaying = true
        )

        meditationJob?.cancel()
        meditationJob = viewModelScope.launch {
            while (_isMeditationPlaying.value && _meditationRemainingSeconds.value > 0) {
                delay(1000)
                _meditationRemainingSeconds.value--
            }
            stopMeditation()
        }
    }

    fun pauseMeditation() {
        _isMeditationPlaying.value = false
        meditationJob?.cancel()
        audioSoundManager.stopAmbientSound()

        val trackTitle = _activeMeditationTrack.value?.title ?: "Meditation Track"
        notificationHelper.showPlaybackNotification(
            title = trackTitle,
            subtitle = "Paused",
            isPlaying = false
        )
    }

    fun stopMeditation() {
        val track = _activeMeditationTrack.value
        val remaining = _meditationRemainingSeconds.value
        _isMeditationPlaying.value = false
        meditationJob?.cancel()
        audioSoundManager.stopAmbientSound()
        notificationHelper.dismissPlaybackNotification()

        if (track != null) {
            val completed = (track.durationMinutes * 60) - remaining
            if (completed > 15) {
                viewModelScope.launch {
                    repository.recordSession("MEDITATION", track.title, completed)
                }
            }
        }
        _activeMeditationTrack.value = null
    }

    fun toggleAmbientSound(sound: AmbientSound) {
        if (_activeAmbientSound.value?.id == sound.id) {
            _activeAmbientSound.value = null
            audioSoundManager.stopAmbientSound()
            notificationHelper.dismissPlaybackNotification()
        } else {
            _activeAmbientSound.value = sound
            if (sound.audioUrl.isNotBlank()) {
                val localFile = getLocalAudioCacheFile(sound.id)
                audioSoundManager.playAudioUrl(sound.audioUrl, localCachePath = localFile.absolutePath)
                
                downloadAudioIfNeeded(
                    id = sound.id,
                    url = sound.audioUrl,
                    progressMap = ambientDownloadProgress,
                    onComplete = {
                        ambientDownloadProgress.value = ambientDownloadProgress.value + (sound.id to 1f)
                    }
                )
            } else {
                audioSoundManager.startAmbientSound(sound.frequency, sound.secondaryFrequency)
            }
            notificationHelper.showPlaybackNotification(
                title = sound.name,
                subtitle = "Playing • Calming Soundscape",
                isPlaying = true
            )
        }
    }

    fun setAmbientVolume(vol: Float) {
        _ambientVolume.value = vol
        audioSoundManager.setVolume(vol)
    }

    // Voice Calling between User & Therapist
    fun initiateCall(therapist: Therapist, appointment: AppointmentEntity? = null) {
        _callState.value = ActiveCallState(
            isActive = true,
            isIncoming = false,
            appointment = appointment,
            therapistName = therapist.name,
            therapistTitle = therapist.title,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = true
        )
        startCallTimer()
    }

    fun startCallWithAppointment(appointment: AppointmentEntity) {
        _callState.value = ActiveCallState(
            isActive = true,
            isIncoming = false,
            appointment = appointment,
            therapistName = appointment.therapistName,
            therapistTitle = appointment.therapistTitle,
            durationSeconds = 0,
            isMuted = false,
            isSpeakerOn = true
        )
        startCallTimer()
    }

    fun toggleCallMute() {
        val curr = _callState.value
        _callState.value = curr.copy(isMuted = !curr.isMuted)
        audioSoundManager.triggerSubtleHaptic()
    }

    fun toggleCallSpeaker() {
        val curr = _callState.value
        _callState.value = curr.copy(isSpeakerOn = !curr.isSpeakerOn)
        audioSoundManager.triggerSubtleHaptic()
    }

    fun endCall() {
        callTimerJob?.cancel()
        _callState.value = ActiveCallState(isActive = false)
        audioSoundManager.triggerSubtleHaptic()
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_callState.value.isActive) {
                delay(1000)
                _callState.value = _callState.value.copy(
                    durationSeconds = _callState.value.durationSeconds + 1
                )
            }
        }
    }

    // Appointment Booking & Payment
    fun bookAppointment(therapist: Therapist, slot: String, paymentMethod: String) {
        viewModelScope.launch {
            val apt = AppointmentEntity(
                id = "apt_" + System.currentTimeMillis(),
                therapistId = therapist.id,
                therapistName = therapist.name,
                therapistTitle = therapist.title,
                clientName = "You",
                dateSlot = slot.substringBefore(" "),
                timeSlot = slot.substringAfter(" "),
                status = "UPCOMING",
                fee = therapist.hourlyRate,
                callEligible = true
            )
            repository.bookAppointment(apt, paymentMethod)
            notificationHelper.sendAppointmentReminder(therapist.name, slot)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun cancelAppointment(id: String) {
        viewModelScope.launch {
            repository.cancelAppointment(id)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun toggleFavoriteQuote() {
        viewModelScope.launch {
            val q = _currentQuote.value
            repository.toggleFavoriteQuote(q)
            _currentQuote.value = q.copy(isFavorite = !q.isFavorite)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun sendTestHydrationReminder() {
        val metric = todayMetric.value
        val curr = metric?.waterMl ?: 0
        val goal = metric?.waterGoalMl ?: 2500
        notificationHelper.sendHydrationReminder(curr, goal)
        audioSoundManager.triggerSubtleHaptic()
    }

    // Journal Entry operations
    fun saveJournalEntry(
        title: String,
        content: String,
        mood: String
    ) {
        viewModelScope.launch {
            val currSteps = todayMetric.value?.steps ?: 0
            val currWater = todayMetric.value?.waterMl ?: 0
            repository.addJournalEntry(
                title = title.ifBlank { "Daily Reflection" },
                content = content,
                mood = mood,
                steps = currSteps,
                waterMl = currWater
            )
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    fun deleteJournalEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
            audioSoundManager.triggerSubtleHaptic()
        }
    }

    // Chart Data calculations based strictly on real database metrics
    fun getChartPoints(): List<ChartPoint> {
        val metrics = recentMetrics.value
        val metricType = _selectedMetricType.value
        val cal = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dayFormat = SimpleDateFormat("EEE", Locale.US)

        val days = (6 downTo 0).map { daysAgo ->
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val dateStr = simpleDateFormat.format(c.time)
            val label = dayFormat.format(c.time)
            val isToday = daysAgo == 0
            val metric = metrics.find { it.date == dateStr }
            val value = when (metricType) {
                MetricViewType.CALORIES -> metric?.activeCalories?.toFloat() ?: 0f
                MetricViewType.STEPS -> metric?.steps?.toFloat() ?: 0f
                MetricViewType.WATER -> metric?.waterMl?.toFloat() ?: 0f
            }
            ChartPoint(
                label = label,
                value = value,
                isToday = isToday
            )
        }

        return days
    }
}
