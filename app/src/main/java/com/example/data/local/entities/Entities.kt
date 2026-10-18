package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_metrics")
data class DailyMetricEntity(
    @PrimaryKey val date: String, // format "yyyy-MM-dd"
    val activeCalories: Int,
    val totalCalories: Int,
    val steps: Int,
    val stepGoal: Int = 10000,
    val waterMl: Int,
    val waterGoalMl: Int = 2500,
    val exerciseMinutes: Int = 30
)

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val amountMl: Int,
    val vessel: String = "Glass"
)

@Entity(tableName = "wellness_sessions")
data class WellnessSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "BREATHING" or "MEDITATION"
    val title: String,
    val durationSeconds: Int,
    val completed: Boolean = true
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val therapistId: String,
    val therapistName: String,
    val therapistTitle: String,
    val clientName: String,
    val dateSlot: String,
    val timeSlot: String,
    val status: String, // "UPCOMING", "COMPLETED", "CANCELLED"
    val fee: Double,
    val callEligible: Boolean = true
)

@Entity(tableName = "payment_records")
data class PaymentRecordEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val amount: Double,
    val status: String, // "CONFIRMED", "PENDING", "REFUNDED"
    val paymentMethod: String,
    val appointmentId: String? = null
)

@Entity(tableName = "favorite_quotes")
data class FavoriteQuoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val author: String,
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val mood: String = "CALM",
    val stepCountAtEntry: Int = 0,
    val waterMlAtEntry: Int = 0
)

