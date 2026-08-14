import SwiftUI

struct SectionHeader: View {
    let title: String
    var body: some View {
        Text(title)
            .font(Theme.Font.title)
            .foregroundStyle(Theme.Color.textPrimary)
            .frame(maxWidth: .infinity, alignment: .leading)
    }
}

struct InfoCard: View {
    let icon: String
    let title: String
    let value: String
    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
            HStack(spacing: Theme.Spacing.xs) {
                Image(systemName: icon)
                    .foregroundStyle(Theme.Color.accent)
                Text(title)
                    .font(Theme.Font.caption)
                    .foregroundStyle(Theme.Color.textSecondary)
            }
            Text(value)
                .font(Theme.Font.headline)
                .foregroundStyle(Theme.Color.textPrimary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(Theme.Spacing.md)
        .background(Theme.Color.surfaceAlt)
        .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.sm, style: .continuous))
    }
}

struct StepRow: View {
    let title: String
    let body_: String
    let isComplete: Bool
    let actionLabel: String?
    let onToggle: () -> Void
    let onAction: (() -> Void)?

    init(title: String, body: String, isComplete: Bool, actionLabel: String? = nil, onToggle: @escaping () -> Void, onAction: (() -> Void)? = nil) {
        self.title = title
        self.body_ = body
        self.isComplete = isComplete
        self.actionLabel = actionLabel
        self.onToggle = onToggle
        self.onAction = onAction
    }

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
            HStack(alignment: .top, spacing: Theme.Spacing.sm) {
                Button(action: onToggle) {
                    Image(systemName: isComplete ? "checkmark.circle.fill" : "circle")
                        .font(.system(size: 22))
                        .foregroundStyle(isComplete ? Theme.Color.success : Theme.Color.textSecondary)
                }
                .buttonStyle(.plain)
                VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
                    Text(title)
                        .font(Theme.Font.headline)
                        .foregroundStyle(Theme.Color.textPrimary)
                        .strikethrough(isComplete, color: Theme.Color.textSecondary)
                    Text(body_)
                        .font(Theme.Font.callout)
                        .foregroundStyle(Theme.Color.textSecondary)
                }
            }
            if let actionLabel, let onAction {
                Button(action: onAction) {
                    Text(actionLabel)
                }
                .secondaryButtonStyle()
                .padding(.leading, 30)
            }
        }
        .padding(Theme.Spacing.md)
        .cardStyle()
    }
}

struct EligibilityChecklist: View {
    let items: [String]
    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
            ForEach(items, id: \.self) { item in
                HStack(alignment: .top, spacing: Theme.Spacing.sm) {
                    Image(systemName: "checkmark.seal.fill")
                        .foregroundStyle(Theme.Color.accent)
                        .padding(.top, 2)
                    Text(item)
                        .font(Theme.Font.callout)
                        .foregroundStyle(Theme.Color.textPrimary)
                }
            }
        }
        .padding(Theme.Spacing.md)
        .cardStyle()
    }
}

struct ProgressCard: View {
    let icon: String
    let title: String
    let subtitle: String
    let fraction: Double

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.sm) {
            HStack {
                Image(systemName: icon)
                    .font(.system(size: 20))
                    .foregroundStyle(Theme.Color.primary)
                Text(title)
                    .font(Theme.Font.headline)
                    .foregroundStyle(Theme.Color.textPrimary)
                Spacer()
                Image(systemName: "chevron.right")
                    .foregroundStyle(Theme.Color.textSecondary)
            }
            Text(subtitle)
                .font(Theme.Font.caption)
                .foregroundStyle(Theme.Color.textSecondary)
            ProgressView(value: fraction)
                .tint(Theme.Color.primary)
            Text("\(Int(fraction * 100))% complete")
                .font(Theme.Font.caption)
                .foregroundStyle(Theme.Color.textSecondary)
        }
        .padding(Theme.Spacing.md)
        .cardStyle()
    }
}

/// Shown after the user returns from the in-app browser, since gov sites can't
/// redirect completion back into the app themselves.
struct StepCompletionConfirmation: ViewModifier {
    @Binding var isPresented: Bool
    let stepTitle: String
    let onConfirm: () -> Void

    func body(content: Content) -> some View {
        content.confirmationDialog(
            "Did you finish: \(stepTitle)?",
            isPresented: $isPresented,
            titleVisibility: .visible
        ) {
            Button("Yes, mark complete") { onConfirm() }
            Button("Not yet", role: .cancel) {}
        }
    }
}

extension View {
    func stepCompletionConfirmation(isPresented: Binding<Bool>, stepTitle: String, onConfirm: @escaping () -> Void) -> some View {
        modifier(StepCompletionConfirmation(isPresented: isPresented, stepTitle: stepTitle, onConfirm: onConfirm))
    }
}
