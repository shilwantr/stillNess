package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_WELLNESS = "channel_still_wellness"
        const val CHANNEL_APPOINTMENTS = "channel_still_appointments"
        const val NOTIF_ID_HYDRATION = 101
        const val NOTIF_ID_WELLNESS = 102
        const val NOTIF_ID_APPOINTMENT = 103
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val wellnessChannel = NotificationChannel(
                CHANNEL_WELLNESS,
                "Hydration & Wellness Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle mindfulness, breathing, and water intake reminders"
                enableLights(true)
                lightColor = 0xFF38BDF8.toInt()
            }

            val appointmentChannel = NotificationChannel(
                CHANNEL_APPOINTMENTS,
                "Therapy Sessions & Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming appointment check-ins and session alerts"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(wellnessChannel)
            notificationManager.createNotificationChannel(appointmentChannel)
        }
    }

    fun sendHydrationReminder(intakeCurrentMl: Int, goalMl: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val remainingMl = (goalMl - intakeCurrentMl).coerceAtLeast(0)
        val text = if (remainingMl > 0) {
            "You've consumed ${(intakeCurrentMl / 1000f).formatOneDecimal()} L today. Drink a fresh glass to reach your ${(goalMl / 1000f).formatOneDecimal()} L balance."
        } else {
            "Hydration goal completed! Continue sipping mindfully throughout your day."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_WELLNESS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("STILL • Hydration Sanctuary")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_HYDRATION, notification)
        } catch (_: SecurityException) {}
    }

    fun sendAppointmentReminder(therapistName: String, timeSlot: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permission != PackageManager.PERMISSION_GRANTED) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_APPOINTMENTS)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Upcoming Session with $therapistName")
            .setContentText("Your voice therapy session is scheduled for $timeSlot. Tap to prepare.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_APPOINTMENT, notification)
        } catch (_: SecurityException) {}
    }

    private fun Float.formatOneDecimal(): String = String.format("%.1f", this)
}
