package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DailyMetricEntity
import com.example.data.local.entities.FavoriteQuoteEntity
import com.example.data.local.entities.PaymentRecordEntity
import com.example.data.local.entities.WaterEntryEntity
import com.example.data.local.entities.WellnessSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    // Daily Metrics
    @Query("SELECT * FROM daily_metrics WHERE date = :date LIMIT 1")
    fun getDailyMetricFlow(date: String): Flow<DailyMetricEntity?>

    @Query("SELECT * FROM daily_metrics WHERE date = :date LIMIT 1")
    suspend fun getDailyMetric(date: String): DailyMetricEntity?

    @Query("SELECT * FROM daily_metrics ORDER BY date DESC LIMIT :days")
    fun getRecentMetricsFlow(days: Int): Flow<List<DailyMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetric(metric: DailyMetricEntity)

    // Water entries
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getWaterEntriesForDate(date: String): Flow<List<WaterEntryEntity>>

    @Insert
    suspend fun insertWaterEntry(entry: WaterEntryEntity)

    @Query("DELETE FROM water_entries WHERE id = (SELECT id FROM water_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLatestWaterEntry(date: String)

    // Wellness Sessions
    @Query("SELECT * FROM wellness_sessions ORDER BY timestamp DESC")
    fun getAllSessionsFlow(): Flow<List<WellnessSessionEntity>>

    @Insert
    suspend fun insertSession(session: WellnessSessionEntity)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY dateSlot ASC, timeSlot ASC")
    fun getAllAppointmentsFlow(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: String, status: String)

    @Query("DELETE FROM appointments WHERE id IN ('apt_1', 'apt_2')")
    suspend fun deleteHardcodedAppointments()

    // Payment Records
    @Query("SELECT * FROM payment_records ORDER BY timestamp DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecordEntity)

    @Query("DELETE FROM payment_records WHERE id = 'pay_001'")
    suspend fun deleteHardcodedPayments()

    // Favorite Quotes
    @Query("SELECT * FROM favorite_quotes ORDER BY savedTimestamp DESC")
    fun getAllFavoriteQuotesFlow(): Flow<List<FavoriteQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteQuote(quote: FavoriteQuoteEntity)

    @Query("DELETE FROM favorite_quotes WHERE id = :id")
    suspend fun removeFavoriteQuote(id: String)
}
