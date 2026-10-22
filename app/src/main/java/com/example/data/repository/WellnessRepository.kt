package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DailyMetricEntity
import com.example.data.local.entities.FavoriteQuoteEntity
import com.example.data.local.entities.JournalEntryEntity
import com.example.data.local.entities.PaymentRecordEntity
import com.example.data.local.entities.WaterEntryEntity
import com.example.data.local.entities.WellnessSessionEntity
import com.example.domain.model.AmbientSound
import com.example.domain.model.MeditationTrack
import com.example.domain.model.Quote
import com.example.domain.model.Therapist
import com.example.domain.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WellnessRepository(private val dao: WellnessDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    companion object {
        private const val QUOTE_PREFS_NAME = "still_quote_rotation_prefs"
        private const val KEY_LAST_QUOTE_INDEX = "last_quote_index"
    }

    fun getTodayDate(): String = dateFormat.format(Date())

    fun getDailyMetricFlow(date: String = getTodayDate()): Flow<DailyMetricEntity?> {
        return dao.getDailyMetricFlow(date)
    }

    fun getRecentMetricsFlow(days: Int = 14): Flow<List<DailyMetricEntity>> {
        return dao.getRecentMetricsFlow(days)
    }

    suspend fun getOrCreateTodayMetric(): DailyMetricEntity {
        val today = getTodayDate()
        val existing = dao.getDailyMetric(today)
        if (existing != null) {
            return existing
        }
        val newMetric = DailyMetricEntity(
            date = today,
            activeCalories = 0,
            totalCalories = 0,
            steps = 0,
            stepGoal = 10000,
            waterMl = 0,
            waterGoalMl = 2500,
            exerciseMinutes = 0
        )
        dao.insertOrUpdateMetric(newMetric)
        return newMetric
    }

    suspend fun seedInitialDataIfEmpty() {
        // Ensure today's metric entry exists with clean zero-based tracking
        getOrCreateTodayMetric()

        // Clean up any legacy hardcoded sample data
        dao.deleteHardcodedAppointments()
        dao.deleteHardcodedPayments()
    }

    suspend fun addWater(amountMl: Int, vessel: String = "Glass") {
        val today = getTodayDate()
        val current = dao.getDailyMetric(today) ?: getOrCreateTodayMetric()
        val updated = current.copy(waterMl = (current.waterMl + amountMl).coerceAtLeast(0))
        dao.insertOrUpdateMetric(updated)
        dao.insertWaterEntry(WaterEntryEntity(date = today, amountMl = amountMl, vessel = vessel))
    }

    suspend fun undoLatestWater() {
        val today = getTodayDate()
        val current = dao.getDailyMetric(today) ?: return
        dao.deleteLatestWaterEntry(today)
        val newWater = (current.waterMl - 250).coerceAtLeast(0)
        dao.insertOrUpdateMetric(current.copy(waterMl = newWater))
    }

    suspend fun updateSteps(deltaSteps: Int) {
        val today = getTodayDate()
        val current = dao.getDailyMetric(today) ?: getOrCreateTodayMetric()
        val newSteps = (current.steps + deltaSteps).coerceAtLeast(0)
        val newActiveBurn = (newSteps * 0.045f).toInt()
        val updated = current.copy(
            steps = newSteps,
            activeCalories = newActiveBurn,
            totalCalories = newActiveBurn
        )
        dao.insertOrUpdateMetric(updated)
    }

    suspend fun recordSession(type: String, title: String, durationSeconds: Int) {
        val today = getTodayDate()
        dao.insertSession(
            WellnessSessionEntity(
                date = today,
                type = type,
                title = title,
                durationSeconds = durationSeconds,
                completed = true
            )
        )
    }

    fun getAllSessionsFlow(): Flow<List<WellnessSessionEntity>> = dao.getAllSessionsFlow()
    fun getWaterEntriesFlow(): Flow<List<WaterEntryEntity>> = dao.getWaterEntriesForDate(getTodayDate())
    fun getAllAppointmentsFlow(): Flow<List<AppointmentEntity>> = dao.getAllAppointmentsFlow()
    fun getAllPaymentsFlow(): Flow<List<PaymentRecordEntity>> = dao.getAllPaymentsFlow()
    fun getFavoriteQuotesFlow(): Flow<List<FavoriteQuoteEntity>> = dao.getAllFavoriteQuotesFlow()
    fun getJournalEntriesFlow(): Flow<List<JournalEntryEntity>> = dao.getAllJournalEntriesFlow()

    suspend fun addJournalEntry(
        title: String,
        content: String,
        mood: String,
        steps: Int = 0,
        waterMl: Int = 0
    ) {
        val today = getTodayDate()
        dao.insertJournalEntry(
            JournalEntryEntity(
                date = today,
                timestamp = System.currentTimeMillis(),
                title = title,
                content = content,
                mood = mood,
                stepCountAtEntry = steps,
                waterMlAtEntry = waterMl
            )
        )
    }

    suspend fun deleteJournalEntry(id: Long) {
        dao.deleteJournalEntry(id)
    }

    suspend fun bookAppointment(appointment: AppointmentEntity, paymentMethod: String) {
        dao.insertAppointment(appointment)
        dao.insertPayment(
            PaymentRecordEntity(
                id = "pay_" + System.currentTimeMillis(),
                title = "Therapy Appointment - ${appointment.therapistName}",
                amount = appointment.fee,
                status = "CONFIRMED",
                paymentMethod = paymentMethod,
                appointmentId = appointment.id
            )
        )
        try {
            firestore.collection("appointments").document(appointment.id).set(appointment)
        } catch (e: Exception) {
            android.util.Log.w("WellnessRepository", "Failed to sync appointment to Firestore: ${e.message}")
        }
    }

    suspend fun updateAppointmentCallStatus(appointmentId: String, status: String) {
        dao.updateAppointmentStatus(appointmentId, status)
        try {
            firestore.collection("appointments").document(appointmentId)
                .update("status", status)
        } catch (e: Exception) {
            android.util.Log.w("WellnessRepository", "Failed to update appointment call status in Firestore: ${e.message}")
        }
    }

    suspend fun cancelAppointment(appointmentId: String) {
        dao.updateAppointmentStatus(appointmentId, "CANCELLED")
        try {
            firestore.collection("appointments").document(appointmentId)
                .update("status", "CANCELLED")
        } catch (e: Exception) {
            android.util.Log.w("WellnessRepository", "Failed to cancel appointment in Firestore: ${e.message}")
        }
    }

    suspend fun toggleFavoriteQuote(quote: Quote) {
        if (quote.isFavorite) {
            dao.removeFavoriteQuote(quote.id)
        } else {
            dao.insertFavoriteQuote(
                FavoriteQuoteEntity(
                    id = quote.id,
                    text = quote.text,
                    author = quote.author
                )
            )
        }
    }

    fun getCuratedTherapists(): List<Therapist> = emptyList()

    /**
     * Live Therapist Directory - Streamed in real-time from Firebase Firestore DB.
     */
    fun getTherapistsFlow(): Flow<List<Therapist>> = callbackFlow {
        val listener = firestore.collection("therapists")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val therapists = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Therapist::class.java)?.copy(id = doc.id)
                        } catch (e: Exception) {
                            Therapist(
                                id = doc.id,
                                name = doc.getString("name") ?: "Therapist",
                                title = doc.getString("title") ?: "Practitioner",
                                specialty = doc.getString("specialty") ?: "Mental Health",
                                credentials = doc.getString("credentials") ?: "Licensed Specialist",
                                yearsExperience = doc.getLong("yearsExperience")?.toInt() ?: 5,
                                languages = (doc.get("languages") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("English"),
                                bio = doc.getString("bio") ?: "",
                                rating = (doc.getDouble("rating") ?: 5.0).toFloat(),
                                reviewCount = doc.getLong("reviewCount")?.toInt() ?: 0,
                                hourlyRate = doc.getDouble("hourlyRate") ?: 75.0,
                                isVerified = doc.getBoolean("isVerified") ?: true,
                                isOnline = doc.getBoolean("isOnline") ?: true,
                                availableSlots = (doc.get("availableSlots") as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Today 3:00 PM", "Tomorrow 10:00 AM"),
                                email = doc.getString("email") ?: "",
                                photoUrl = doc.getString("photoUrl") ?: ""
                            )
                        }
                    }
                    trySend(therapists)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveTherapistProfile(therapist: Therapist) {
        suspendCancellableCoroutine<Unit> { cont ->
            firestore.collection("therapists").document(therapist.id).set(therapist)
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(Unit)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    }

    suspend fun saveUserRole(uid: String, role: UserRole) {
        suspendCancellableCoroutine<Unit> { cont ->
            firestore.collection("users").document(uid).set(mapOf("role" to role.name))
                .addOnSuccessListener {
                    if (cont.isActive) cont.resume(Unit)
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    }

    suspend fun getUserRole(uid: String): UserRole {
        return suspendCancellableCoroutine { cont ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val r = doc.getString("role")
                    val role = if (r == "THERAPIST") UserRole.THERAPIST else UserRole.CLIENT
                    if (cont.isActive) cont.resume(role)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(UserRole.CLIENT)
                }
        }
    }

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    /**
     * Guided Meditation Library - Streamed live from Firebase Firestore DB.
     */
    fun getMeditationTracksFlow(): Flow<List<MeditationTrack>> = callbackFlow {
        val listener = firestore.collection("meditation_tracks")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tracks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MeditationTrack::class.java)?.copy(id = doc.id)
                    }
                    trySend(tracks)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Relaxing Music Library - Streamed live from Firebase Firestore DB.
     */
    fun getRelaxingMusicFlow(): Flow<List<MeditationTrack>> = callbackFlow {
        val listener = firestore.collection("relaxing_music")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tracks = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MeditationTrack::class.java)?.copy(id = doc.id)
                    }
                    trySend(tracks)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Ambient Sound Library - Streamed live from Firebase Firestore DB.
     */
    fun getAmbientSoundsFlow(): Flow<List<AmbientSound>> = callbackFlow {
        val listener = firestore.collection("ambient_sounds")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val sounds = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(AmbientSound::class.java)?.copy(id = doc.id)
                    }
                    trySend(sounds)
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Inspirational Stillness Quotes Pool.
     * Note for Next Phase DB Integration:
     * Currently backed by an in-memory curated collection + SharedPreferences rotation pointer
     * so the top card quote changes every time the user opens the app. When the dedicated
     * Quotes DB table/remote sync is added in the next phase, replace `getCuratedQuotes()` inside
     * `getNextQuoteOnAppOpen()` with a DAO query (`dao.getNextQuote()`).
     */
    fun getCuratedQuotes(): List<Quote> {
        return listOf(
            Quote(
                id = "q_1",
                text = "Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor.",
                author = "Thich Nhat Hanh"
            ),
            Quote(
                id = "q_2",
                text = "Within you, there is a stillness and a sanctuary to which you can retreat at any time and be yourself.",
                author = "Hermann Hesse"
            ),
            Quote(
                id = "q_3",
                text = "You do not have to control your thoughts. You just have to stop letting them control you.",
                author = "Dan Millman"
            ),
            Quote(
                id = "q_4",
                text = "Calm mind brings inner strength and self-confidence, so that is very important for good health.",
                author = "Dalai Lama"
            ),
            Quote(
                id = "q_5",
                text = "Almost everything will work again if you unplug it for a few minutes, including you.",
                author = "Anne Lamott"
            ),
            Quote(
                id = "q_6",
                text = "In the midst of movement and chaos, keep stillness inside of you.",
                author = "Deepak Chopra"
            ),
            Quote(
                id = "q_7",
                text = "Quiet the mind, and the soul will speak.",
                author = "Ma Jaya Sati Bhagavati"
            ),
            Quote(
                id = "q_8",
                text = "The present moment is filled with joy and happiness. If you are attentive, you will see it.",
                author = "Thich Nhat Hanh"
            ),
            Quote(
                id = "q_9",
                text = "Do not let the behavior of others destroy your inner peace.",
                author = "Dalai Lama"
            ),
            Quote(
                id = "q_10",
                text = "Nothing can bring you peace but yourself.",
                author = "Ralph Waldo Emerson"
            )
        )
    }

    /**
     * Advances and returns the next quote every time the user opens the app.
     * Persists the rotation index in SharedPreferences so every launch displays a fresh quote,
     * and provides a single integration point for the next-phase database query.
     */
    fun getNextQuoteOnAppOpen(context: Context): Quote {
        val quotes = getCuratedQuotes()
        if (quotes.isEmpty()) {
            return Quote("q_default", "Breathe in calm, breathe out tension.", "STILL")
        }
        val prefs = context.getSharedPreferences(QUOTE_PREFS_NAME, Context.MODE_PRIVATE)
        val lastIndex = prefs.getInt(KEY_LAST_QUOTE_INDEX, -1)
        val nextIndex = (lastIndex + 1) % quotes.size
        prefs.edit().putInt(KEY_LAST_QUOTE_INDEX, nextIndex).apply()
        return quotes[nextIndex]
    }
}
