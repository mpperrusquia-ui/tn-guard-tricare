import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var contentStore: ContentStore
    @EnvironmentObject private var progressStore: ProgressStore
    @EnvironmentObject private var draftStore: FormDraftStore
    @AppStorage("monthlyReminderEnabled") private var reminderEnabled = false
    @State private var showClearConfirmation = false
    @State private var showClearedToast = false

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.Color.background.ignoresSafeArea()
                ScrollView {
                    VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                        SectionHeader(title: "Content")
                        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
                            if let content = contentStore.content {
                                HStack {
                                    Text("Content version")
                                        .font(Theme.Font.body)
                                        .foregroundStyle(Theme.Color.textPrimary)
                                    Spacer()
                                    Text("v\(content.contentVersion) · \(content.updatedAt)")
                                        .font(Theme.Font.callout)
                                        .foregroundStyle(Theme.Color.textSecondary)
                                }
                            }
                            Button {
                                Task { await contentStore.refreshFromRemote() }
                            } label: {
                                HStack {
                                    if contentStore.isRefreshing {
                                        ProgressView().padding(.trailing, Theme.Spacing.xs)
                                    }
                                    Text("Check for content updates")
                                }
                            }
                            .secondaryButtonStyle()
                            if let error = contentStore.lastRefreshError {
                                Text(error)
                                    .font(Theme.Font.caption)
                                    .foregroundStyle(Theme.Color.textSecondary)
                            }
                        }
                        .padding(Theme.Spacing.md)
                        .cardStyle()

                        SectionHeader(title: "Notifications")
                        Toggle(isOn: Binding(
                            get: { reminderEnabled },
                            set: { newValue in
                                reminderEnabled = newValue
                                NotificationManager.requestAuthorizationAndSchedule(enabled: newValue)
                            }
                        )) {
                            Text("Monthly reimbursement reminder")
                                .font(Theme.Font.body)
                                .foregroundStyle(Theme.Color.textPrimary)
                        }
                        .tint(Theme.Color.primary)
                        .padding(Theme.Spacing.md)
                        .cardStyle()

                        SectionHeader(title: "Your data")
                        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
                            Text("Everything you enter — checklist progress and form fields — stays on this device. Sensitive fields (SSN, bank info) are stored in the iOS Keychain. Nothing is sent anywhere unless you share a generated PDF yourself.")
                                .font(Theme.Font.caption)
                                .foregroundStyle(Theme.Color.textSecondary)
                            Button(role: .destructive) {
                                showClearConfirmation = true
                            } label: {
                                Text("Clear All Saved Data")
                            }
                            .foregroundStyle(Theme.Color.danger)
                        }
                        .padding(Theme.Spacing.md)
                        .cardStyle()

                        if showClearedToast {
                            Text("All saved data cleared.")
                                .font(Theme.Font.callout)
                                .foregroundStyle(Theme.Color.success)
                        }
                    }
                    .padding(Theme.Spacing.md)
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .confirmationDialog(
                "Clear all saved progress, form drafts, and Keychain data?",
                isPresented: $showClearConfirmation,
                titleVisibility: .visible
            ) {
                Button("Clear everything", role: .destructive) {
                    clearAllData()
                }
                Button("Cancel", role: .cancel) {}
            }
        }
    }

    private func clearAllData() {
        progressStore.resetAll()
        let formIds = contentStore.content?.tnReimbursement.forms.map(\.id) ?? []
        var allFields: [String: [AppContent.FormField]] = [:]
        for form in contentStore.content?.tnReimbursement.forms ?? [] {
            allFields[form.id] = form.fields
        }
        draftStore.clearAll(formIds: formIds, allFields: allFields)
        reminderEnabled = false
        NotificationManager.requestAuthorizationAndSchedule(enabled: false)
        showClearedToast = true
    }
}
