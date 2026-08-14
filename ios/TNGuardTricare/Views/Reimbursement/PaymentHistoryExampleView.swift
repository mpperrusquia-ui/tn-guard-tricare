import SwiftUI

struct PaymentHistoryExampleView: View {
    var body: some View {
        ZStack {
            Theme.Color.background.ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                    Text("Getting your TRICARE Payment History")
                        .font(Theme.Font.largeTitle)
                        .foregroundStyle(Theme.Color.textPrimary)

                    VStack(alignment: .leading, spacing: Theme.Spacing.md) {
                        instructionRow(number: 1, text: "Log into the TRICARE / Humana Military beneficiary portal.")
                        instructionRow(number: 2, text: "Go to Billing → Payment History.")
                        instructionRow(number: 3, text: "Save or print the page as a PDF — it should show your plan, paid-through dates, and amounts.")
                        instructionRow(number: 4, text: "Attach that PDF, along with your filled forms, in your email to tntricare@tn.gov.")
                    }

                    SectionHeader(title: "What it should look like")
                    exampleMock
                        .cardStyle()

                    Text("This is a mock layout for reference only — not a real bill or real personal data.")
                        .font(Theme.Font.caption)
                        .foregroundStyle(Theme.Color.textSecondary)
                }
                .padding(Theme.Spacing.md)
            }
        }
        .navigationTitle("Payment History")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func instructionRow(number: Int, text: String) -> some View {
        HStack(alignment: .top, spacing: Theme.Spacing.sm) {
            Text("\(number)")
                .font(Theme.Font.headline)
                .foregroundStyle(.white)
                .frame(width: 24, height: 24)
                .background(Theme.Color.primary)
                .clipShape(Circle())
            Text(text)
                .font(Theme.Font.body)
                .foregroundStyle(Theme.Color.textPrimary)
        }
    }

    private var exampleMock: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
            HStack {
                Text("Payment History").font(Theme.Font.headline)
                Spacer()
                Text("EXAMPLE").font(Theme.Font.caption).foregroundStyle(Theme.Color.warning)
            }
            Divider()
            exampleRow(plan: "TRICARE Reserve Select", period: "Jan 1 – Jan 31, 2026", amount: "$57.88", status: "Paid")
            exampleRow(plan: "TRICARE Reserve Select", period: "Feb 1 – Feb 28, 2026", amount: "$57.88", status: "Paid")
            exampleRow(plan: "TRICARE Reserve Select", period: "Mar 1 – Mar 31, 2026", amount: "$57.88", status: "Paid")
        }
    }

    private func exampleRow(plan: String, period: String, amount: String, status: String) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(plan).font(Theme.Font.callout).foregroundStyle(Theme.Color.textPrimary)
                Text(period).font(Theme.Font.caption).foregroundStyle(Theme.Color.textSecondary)
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 2) {
                Text(amount).font(Theme.Font.callout).foregroundStyle(Theme.Color.textPrimary)
                Text(status).font(Theme.Font.caption).foregroundStyle(Theme.Color.success)
            }
        }
    }
}
