package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.WellnessDao
import com.example.data.local.entities.AppointmentEntity
import com.example.data.local.entities.DailyMetricEntity
import com.example.data.local.entities.FavoriteQuoteEntity
import com.example.data.local.entities.PaymentRecordEntity
import com.example.data.local.entities.WaterEntryEntity
import com.example.data.local.entities.WellnessSessionEntity

@Database(
    entities = [
        DailyMetricEntity::class,
        WaterEntryEntity::class,
        WellnessSessionEntity::class,
        AppointmentEntity::class,
        PaymentRecordEntity::class,
        FavoriteQuoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wellnessDao(): WellnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "still_wellness_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
