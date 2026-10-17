package com.example.domain.model

enum class UserRole {
    CLIENT,
    THERAPIST
}

enum class BreathingMode(
    val title: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdAfterInhaleSeconds: Int,
    val exhaleSeconds: Int,
    val holdAfterExhaleSeconds: Int
) {
    BOX(
        title = "Box Breathing",
        description = "Equal 4-phase rhythm used by Navy SEALs to regain focus and calm.",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 4,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 4
    ),
    RELAX_478(
        title = "4-7-8 Relaxation",
        description = "Natural tranquilizer for the nervous system, ideal before sleep.",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 7,
        exhaleSeconds = 8,
        holdAfterExhaleSeconds = 0
    ),
    SLOW_46(
        title = "Slow 4-6 Balance",
        description = "Gentle parasympathetic activation to lower heart rate and stress.",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 0,
        exhaleSeconds = 6,
        holdAfterExhaleSeconds = 0
    ),
    EQUAL_44(
        title = "Equal Calm",
        description = "Balanced steady flow to restore mental presence and clarity.",
        inhaleSeconds = 4,
        holdAfterInhaleSeconds = 0,
        exhaleSeconds = 4,
        holdAfterExhaleSeconds = 0
    )
}

data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val isFavorite: Boolean = false
)

data class Therapist(
    val id: String,
    val name: String,
    val title: String,
    val specialty: String,
    val credentials: String,
    val yearsExperience: Int,
    val languages: List<String>,
    val bio: String,
    val rating: Float,
    val reviewCount: Int,
    val hourlyRate: Double,
    val isVerified: Boolean = true,
    val isOnline: Boolean = true,
    val availableSlots: List<String>
)

data class MeditationTrack(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val durationMinutes: Int = 0,
    val description: String = "",
    val isNarrated: Boolean = true,
    val audioUrl: String = "",
    val iconName: String = "self_improvement",
    val accentColor: String = "#38BDF8",
    val baseFrequency: Float = 432f,
    val secondaryFrequency: Float = 648f
)

data class AmbientSound(
    val id: String = "",
    val name: String = "",
    val iconName: String = "",
    val frequency: Float = 0f,
    val secondaryFrequency: Float = 0f,
    val description: String = "",
    val audioUrl: String = ""
)
