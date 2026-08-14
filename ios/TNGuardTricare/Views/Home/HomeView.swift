import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var contentStore: ContentStore
    @EnvironmentObject private var progressStore: ProgressStore
    @State private var selectedTrack: ProgressStore.Track?

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.Color.background.ignoresSafeArea()
                if let content = contentStore.content {
                    ScrollView {
                        VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                            VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                                Text("Welcome, Guardsman")
                                    .font(Theme.Font.largeTitle)
                                    .foregroundStyle(Theme.Color.textPrimary)
                                Text("Two things to get done: enroll in TRICARE Reserve Select, then get reimbursed for it by the state.")
                                    .font(Theme.Font.body)
                                    .foregroundStyle(Theme.Color.textSecondary)
                            }

                            NavigationLink(value: ProgressStore.Track.trs) {
                                ProgressCard(
                                    icon: "heart.text.square.fill",
                                    title: "1. Enroll in TRICARE Reserve Select",
                                    subtitle: "Federal enrollment via milConnect",
                                    fraction: progressStore.progressFraction(track: .trs, totalSteps: content.trs.steps.count)
                                )
                            }
                            .buttonStyle(.plain)

                            NavigationLink(value: ProgressStore.Track.reimbursement) {
                                ProgressCard(
                                    icon: "dollarsign.circle.fill",
                                    title: "2. TN Premium Reimbursement",
                                    subtitle: "Get your individual premium paid back by the state",
                                    fraction: progressStore.progressFraction(track: .reimbursement, totalSteps: content.tnReimbursement.steps.count)
                                )
                            }
                            .buttonStyle(.plain)

                            SectionHeader(title: "Good to know")
                            InfoCard(icon: "envelope.fill", title: "TN Tricare reimbursement email", value: content.tnReimbursement.email)
                            InfoCard(icon: "calendar", title: "Monthly submission deadline", value: content.tnReimbursement.monthlyDeadlineRule)
                        }
                        .padding(Theme.Spacing.md)
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("TN Guard Tricare")
            .navigationDestination(for: ProgressStore.Track.self) { track in
                switch track {
                case .trs: TRSFlowView()
                case .reimbursement: ReimbursementFlowView()
                }
            }
        }
    }
}
