import Foundation
import UserNotifications

/// Schedules the local "submit your payment proof" reminder. Purely on-device — no push
/// server. Fires on the 2nd Friday of every month, matching tnReimbursement.monthlyDeadlineRule.
enum NotificationManager {
    private static let reminderIdentifier = "monthly-reimbursement-reminder"

    static func requestAuthorizationAndSchedule(enabled: Bool) {
        let center = UNUserNotificationCenter.current()
        guard enabled else {
            center.removePendingNotificationRequests(withIdentifiers: [reminderIdentifier])
            return
        }
        center.requestAuthorization(options: [.alert, .sound, .badge]) { granted, _ in
            guard granted else { return }
            scheduleMonthlyReminder()
        }
    }

    private static func scheduleMonthlyReminder() {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: [reminderIdentifier])

        let content = UNMutableNotificationContent()
        content.title = "TN Tricare reimbursement due"
        content.body = "Submit last month's TRICARE payment proof to tntricare@tn.gov — due by the 2nd Friday."
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.weekday = 6 // Friday (Sunday = 1)
        dateComponents.weekOfMonth = 2
        dateComponents.hour = 9
        dateComponents.minute = 0

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: reminderIdentifier, content: content, trigger: trigger)
        center.add(request)
    }
}
