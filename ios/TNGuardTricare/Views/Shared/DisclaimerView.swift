import SwiftUI

struct DisclaimerView: View {
    let disclaimer: AppContent.Disclaimer
    let onContinue: () -> Void

    var body: some View {
        ZStack {
            Theme.Color.background.ignoresSafeArea()
            VStack(spacing: Theme.Spacing.lg) {
                Spacer()
                Image(systemName: "shield.lefthalf.filled")
                    .font(.system(size: 56))
                    .foregroundStyle(Theme.Color.primary)
                Text(disclaimer.title)
                    .font(Theme.Font.largeTitle)
                    .foregroundStyle(Theme.Color.textPrimary)
                    .multilineTextAlignment(.center)
                Text(disclaimer.body)
                    .font(Theme.Font.body)
                    .foregroundStyle(Theme.Color.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, Theme.Spacing.md)
                Spacer()
                Button(action: onContinue) {
                    Text("I understand, continue")
                }
                .primaryButtonStyle()
                .padding(.horizontal, Theme.Spacing.lg)
            }
            .padding(Theme.Spacing.lg)
        }
    }
}
