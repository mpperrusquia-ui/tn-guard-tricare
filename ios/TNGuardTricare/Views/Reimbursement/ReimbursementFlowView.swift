import SwiftUI

struct ReimbursementFlowView: View {
    @EnvironmentObject private var contentStore: ContentStore
    @EnvironmentObject private var progressStore: ProgressStore
    @AppStorage("monthlyReminderEnabled") private var reminderEnabled = false

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.Color.background.ignoresSafeArea()
                if let content = contentStore.content {
                    ScrollView {
                        VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                            VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                Text("TN Premium Reimbursement")
                                    .font(Theme.Font.largeTitle)
                                    .foregroundStyle(Theme.Color.textPrimary)
                                Text("Tennessee Medical Readiness Act — reimburses your individual TRS/dental premium, not family coverage.")
                                    .font(Theme.Font.body)
                                    .foregroundStyle(Theme.Color.textSecondary)
                            }

                            InfoCard(icon: "envelope.fill", title: "Send your package to", value: content.tnReimbursement.email)

                            SectionHeader(title: "Eligibility")
                            EligibilityChecklist(items: content.tnReimbursement.eligibility)

                            SectionHeader(title: "Steps")
                            ForEach(content.tnReimbursement.steps) { step in
                                StepRow(
                                    title: step.title,
                                    body: step.body,
                                    isComplete: progressStore.isStepComplete(step.id, track: .reimbursement),
                                    onToggle: { progressStore.toggleStep(step.id, track: .reimbursement) }
                                )
                            }

                            SectionHeader(title: "Fill out the forms")
                            ForEach(content.tnReimbursement.forms) { form in
                                NavigationLink(value: form) {
                                    HStack {
                                        VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                            Text(form.title)
                                                .font(Theme.Font.headline)
                                                .foregroundStyle(Theme.Color.textPrimary)
                                            Text(form.description)
                                                .font(Theme.Font.caption)
                                                .foregroundStyle(Theme.Color.textSecondary)
                                        }
                                        Spacer()
                                        if progressStore.isFormComplete(form.id) {
                                            Image(systemName: "checkmark.circle.fill")
                                                .foregroundStyle(Theme.Color.success)
                                        }
                                        Image(systemName: "chevron.right")
                                            .foregroundStyle(Theme.Color.textSecondary)
                                    }
                                    .padding(Theme.Spacing.md)
                                    .cardStyle()
                                }
                                .buttonStyle(.plain)
                            }

                            NavigationLink {
                                PaymentHistoryExampleView()
                            } label: {
                                Text("How do I get my TRICARE Payment History PDF?")
                            }
                            .secondaryButtonStyle()

                            SectionHeader(title: "Stay on track")
                            VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
                                Toggle(isOn: Binding(
                                    get: { reminderEnabled },
                                    set: { newValue in
                                        reminderEnabled = newValue
                                        NotificationManager.requestAuthorizationAndSchedule(enabled: newValue)
                                    }
                                )) {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Monthly submission reminder")
                                            .font(Theme.Font.headline)
                                            .foregroundStyle(Theme.Color.textPrimary)
                                        Text("Notifies you on the \(content.tnReimbursement.monthlyDeadlineRule)")
                                            .font(Theme.Font.caption)
                                            .foregroundStyle(Theme.Color.textSecondary)
                                    }
                                }
                                .tint(Theme.Color.primary)
                            }
                            .padding(Theme.Spacing.md)
                            .cardStyle()
                        }
                        .padding(Theme.Spacing.md)
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Reimbursement")
            .navigationBarTitleDisplayMode(.inline)
            .navigationDestination(for: AppContent.FormDefinition.self) { form in
                FormFillView(form: form)
            }
        }
    }
}
