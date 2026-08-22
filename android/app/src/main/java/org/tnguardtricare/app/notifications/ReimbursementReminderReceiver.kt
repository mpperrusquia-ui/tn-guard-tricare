package org.tnguardtricare.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import org.tnguardtricare.app.MainActivity
import java.time.LocalDateTime

private const val CHANNEL_ID = "reimbursement_reminder"
private const val NOTIFICATION_ID = 2001

/** Fires the monthly reminder notification, then reschedules itself for next month — Android
 * has no built-in recurring "2nd Friday of every month" trigger, so each firing chains to the
 * next one via ReminderScheduler.nextSecondFriday. */
class ReimbursementReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
        ReminderScheduler.scheduleAt(context, ReminderScheduler.nextSecondFriday(LocalDateTime.now()))
    }

    private fun showNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reimbursement reminder",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            manager.createNotificationChannel(channel)
        }

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentIntent = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("TN Tricare reimbursement due")
            .setContentText("Submit last month's TRICARE payment proof to tntricare@tn.gov — due by the 2nd Friday.")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Submit last month's TRICARE payment proof to tntricare@tn.gov — due by the 2nd Friday."
            ))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
