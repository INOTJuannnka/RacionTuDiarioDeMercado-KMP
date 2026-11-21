package com.racion.app.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.racion.app.MainActivity
import com.racion.app.R
import com.racion.app.shared.di.androidStorage
import java.util.Calendar

/**
 * Recordatorio diario para completar el diario.
 *
 * Usa una alarma inexacta de `AlarmManager` (no requiere el permiso de alarmas exactas):
 * Android puede correrla unos minutos para ahorrar batería, lo cual basta para un aviso.
 * Las alarmas se borran al reiniciar el teléfono; [ReminderReceiver] las vuelve a programar.
 */
object DailyReminder {
    private const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 1001
    const val HOUR = 20

    fun schedule(context: Context) {
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HOUR)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        context.getSystemService(AlarmManager::class.java).setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            next.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            alarmIntent(context)
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java).cancel(alarmIntent(context))
    }

    fun canNotify(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    internal fun show(context: Context) {
        if (!canNotify(context)) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recordatorio diario", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.ic_launcher_background))
            .setContentTitle("¿Ya registraste tus comidas?")
            .setContentText("Tomate un minuto para completar el diario de hoy.")
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // El permiso se revocó entre el chequeo y el aviso: no hay nada que mostrar.
        }
    }

    private fun alarmIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, 0,
        Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_SHOW),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
}

/** Muestra el aviso cuando suena la alarma y la reprograma tras reiniciar el teléfono. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val enabled = androidStorage(context).loadSettings()?.remindersEnabled ?: true
        when (intent.action) {
            ACTION_SHOW -> if (enabled) DailyReminder.show(context)
            Intent.ACTION_BOOT_COMPLETED -> if (enabled) DailyReminder.schedule(context)
        }
    }

    companion object {
        const val ACTION_SHOW = "com.racion.app.SHOW_DAILY_REMINDER"
    }
}
