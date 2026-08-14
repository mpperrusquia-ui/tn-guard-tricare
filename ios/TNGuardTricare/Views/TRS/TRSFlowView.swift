import SwiftUI

struct TRSFlowView: View {
    @EnvironmentObject private var contentStore: ContentStore
    @EnvironmentObject private var progressStore: ProgressStore
    @State private var safariURL: IdentifiableURL?
    @State private var pendingConfirmationStep: AppContent.Step?
    @State private var showConfirmation = false

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.Color.background.ignoresSafeArea()
                if let content = contentStore.content {
                    ScrollView {
                        VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                            VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                Text("TRICARE Reserve Select")
                                    .font(Theme.Font.largeTitle)
                                    .foregroundStyle(Theme.Color.textPrimary)
                                Text("Available year-round — not tied to Open Season. Purchase any time you qualify.")
                                    .font(Theme.Font.body)
                                    .foregroundStyle(Theme.Color.textSecondary)
                            }

                            HStack(spacing: Theme.Spacing.sm) {
                                InfoCard(icon: "person.fill", title: "Member only / mo", value: "$\(content.trs.premiums.memberOnlyMonthly)")
                                InfoCard(icon: "person.3.fill", title: "Member + family / mo", value: "$\(content.trs.premiums.memberAndFamilyMonthly)")
                            }

                            SectionHeader(title: "Eligibility")
                            EligibilityChecklist(items: content.trs.eligibility)

                            SectionHeader(title: "Steps")
                            ForEach(content.trs.steps) { step in
                                StepRow(
                                    title: step.title,
                                    body: step.body,
                                    isComplete: progressStore.isStepComplete(step.id, track: .trs),
                                    actionLabel: step.actionLabel,
                                    onToggle: { progressStore.toggleStep(step.id, track: .trs) },
                                    onAction: step.actionLinkKey != nil ? { openLink(for: step, content: content) } : nil
                                )
                            }

                            SectionHeader(title: "Talk to your regional contractor")
                            InfoCard(icon: "phone.fill", title: content.trs.phoneNumbers.humanaEastLabel, value: formatPhone(content.trs.phoneNumbers.humanaEast))
                            InfoCard(icon: "phone.fill", title: content.trs.phoneNumbers.triWestLabel, value: formatPhone(content.trs.phoneNumbers.triWest))
                        }
                        .padding(Theme.Spacing.md)
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("TRS Enrollment")
            .navigationBarTitleDisplayMode(.inline)
            .sheet(item: $safariURL) { identifiableURL in
                SafariView(url: identifiableURL.url)
                    .ignoresSafeArea()
                    .onDisappear {
                        showConfirmation = true
                    }
            }
            .stepCompletionConfirmation(
                isPresented: $showConfirmation,
                stepTitle: pendingConfirmationStep?.title ?? "this step"
            ) {
                if let step = pendingConfirmationStep {
                    progressStore.setStepComplete(step.id, track: .trs, complete: true)
                }
            }
        }
    }

    private func openLink(for step: AppContent.Step, content: AppContent) {
        guard let key = step.actionLinkKey, let urlString = content.trs.links[key], let url = URL(string: urlString) else { return }
        pendingConfirmationStep = step
        safariURL = IdentifiableURL(url: url)
    }

    private func formatPhone(_ digits: String) -> String {
        guard digits.count == 10 else { return digits }
        let a = digits.prefix(3)
        let b = digits.dropFirst(3).prefix(3)
        let c = digits.suffix(4)
        return "\(a)-\(b)-\(c)"
    }
}
