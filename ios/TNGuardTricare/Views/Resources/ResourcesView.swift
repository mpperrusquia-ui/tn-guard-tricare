import SwiftUI

struct ResourcesView: View {
    @EnvironmentObject private var contentStore: ContentStore
    @State private var safariURL: IdentifiableURL?

    var body: some View {
        NavigationStack {
            ZStack {
                Theme.Color.background.ignoresSafeArea()
                if let content = contentStore.content {
                    ScrollView {
                        VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                            SectionHeader(title: "Official links")
                            VStack(spacing: Theme.Spacing.sm) {
                                linkRow("TRS Overview", content.trs.links.trsOverview)
                                linkRow("TRS Enrollment Steps", content.trs.links.trsEnrollmentSteps)
                                linkRow("milConnect", content.trs.links.milconnect)
                                linkRow("Beneficiary Web Enrollment", content.trs.links.bwe)
                                linkRow("TRS Forms Page", content.trs.links.formsPage)
                                linkRow("When Coverage Begins", content.trs.links.whenCoverageBegins)
                                linkRow("Compare Costs", content.trs.links.costs)
                                linkRow("National Guard & Reserve Handbook", content.trs.links.handbook)
                                linkRow("Find a Doctor", content.trs.links.findDoctor)
                                linkRow("TN Medical Readiness Act Program Page", content.tnReimbursement.links.programPage)
                                linkRow("TN Program Policy (PDF)", content.tnReimbursement.links.policyPdf)
                                linkRow("Enrollment Packet (PDF)", content.tnReimbursement.links.enrollmentPacketPdf)
                                linkRow("2026 Form W-4 (PDF)", content.tnReimbursement.links.w4Pdf)
                            }
                            .padding(Theme.Spacing.md)
                            .cardStyle()

                            SectionHeader(title: "FAQ")
                            VStack(spacing: Theme.Spacing.sm) {
                                ForEach(content.faq) { item in
                                    DisclosureGroup(item.question) {
                                        Text(item.answer)
                                            .font(Theme.Font.callout)
                                            .foregroundStyle(Theme.Color.textSecondary)
                                            .padding(.top, Theme.Spacing.xs)
                                    }
                                    .font(Theme.Font.headline)
                                    .foregroundStyle(Theme.Color.textPrimary)
                                    .tint(Theme.Color.accent)
                                }
                            }
                            .padding(Theme.Spacing.md)
                            .cardStyle()

                            SectionHeader(title: "Disclaimer")
                            Text(content.disclaimer.body)
                                .font(Theme.Font.caption)
                                .foregroundStyle(Theme.Color.textSecondary)
                                .padding(Theme.Spacing.md)
                                .cardStyle()
                        }
                        .padding(Theme.Spacing.md)
                    }
                } else {
                    ProgressView()
                }
            }
            .navigationTitle("Resources")
            .navigationBarTitleDisplayMode(.inline)
            .sheet(item: $safariURL) { identifiableURL in
                SafariView(url: identifiableURL.url).ignoresSafeArea()
            }
        }
    }

    private func linkRow(_ title: String, _ urlString: String) -> some View {
        Button {
            if let url = URL(string: urlString) {
                safariURL = IdentifiableURL(url: url)
            }
        } label: {
            HStack {
                Text(title)
                    .font(Theme.Font.callout)
                    .foregroundStyle(Theme.Color.textPrimary)
                Spacer()
                Image(systemName: "arrow.up.right")
                    .foregroundStyle(Theme.Color.accent)
            }
        }
        .buttonStyle(.plain)
    }
}
