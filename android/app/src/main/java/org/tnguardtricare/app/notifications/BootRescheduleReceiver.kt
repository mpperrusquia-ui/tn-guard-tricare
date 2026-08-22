package org.tnguardtricare.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

private const val PREFS_NAME = "org.tnguardtricare.app.prefs"
private const val KEY_REMINDER_ENABLED = "monthly_reminder_enabled"

/** AlarmManager alarms don't survive a reboot, so re-arm the reminder on boot if the user had
 * it turned on — mirrors iOS's UNCalendarNotificationTrigger surviving reboots automatically,
 * which Android's AlarmManager doesn't do for us. */
class BootRescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_REMINDER_ENABLED, false)) {
            ReminderScheduler.scheduleMonthlyReminder(context)
        }
    }
}
