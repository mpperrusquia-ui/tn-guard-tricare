package org.tnguardtricare.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Schedules the local "submit your payment proof" reminder — purely on-device, no push
 * server. Fires on the 2nd Friday of every month, matching tnReimbursement.monthlyDeadlineRule
 * and mirroring iOS's NotificationManager.swift (which uses UNCalendarNotificationTrigger's
 * native weekday+weekOfMonth support; Android has no equivalent, so this computes the next
 * occurrence manually and the receiver reschedules itself each time it fires).
 *
 * Uses inexact alarms (`setAndAllowWhileIdle`) rather than exact ones — a monthly paperwork
 * reminder doesn't need to-the-minute precision, and this avoids requiring the user to grant
 * the special SCHEDULE_EXACT_ALARM permission for something this low-stakes.
 */
object ReminderScheduler {
    private const val REQUEST_CODE = 1001

    fun scheduleMonthlyReminder(context: Context) {
        val next = nextSecondFriday(LocalDateTime.now())
        scheduleAt(context, next)
    }

    fun scheduleAt(context: Context, dateTime: LocalDateTime) {
        val triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = reminderPendingIntent(context)
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }

    fun cancel(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(reminderPendingIntent(context))
    }

    private fun reminderPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReimbursementReminderReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    /** Next 2nd-Friday-of-month at 9am, strictly after [from]. */
    fun nextSecondFriday(from: LocalDateTime): LocalDateTime {
        var candidate = secondFridayOf(from.toLocalDate().withDayOfMonth(1)).atTime(9, 0)
        if (!candidate.isAfter(from)) {
            candidate = secondFridayOf(from.toLocalDate().plusMonths(1).withDayOfMonth(1)).atTime(9, 0)
        }
        return candidate
    }

    private fun secondFridayOf(firstOfMonth: LocalDate): LocalDate {
        val firstFriday = firstOfMonth.with(TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY))
        return firstFriday.plusWeeks(1)
    }
}
