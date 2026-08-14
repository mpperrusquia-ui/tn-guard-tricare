import SwiftUI

/// Mirrors content/design-tokens.json. Keep in sync by hand when that file changes.
enum Theme {
    enum Color {
        static let primary = SwiftUI.Color("ThemePrimary")
        static let primaryDark = SwiftUI.Color("ThemePrimaryDark")
        static let accent = SwiftUI.Color("ThemeAccent")
        static let background = SwiftUI.Color("ThemeBackground")
        static let surface = SwiftUI.Color("ThemeSurface")
        static let surfaceAlt = SwiftUI.Color("ThemeSurfaceAlt")
        static let textPrimary = SwiftUI.Color("ThemeTextPrimary")
        static let textSecondary = SwiftUI.Color("ThemeTextSecondary")
        static let success = SwiftUI.Color("ThemeSuccess")
        static let warning = SwiftUI.Color("ThemeWarning")
        static let danger = SwiftUI.Color("ThemeDanger")
        static let border = SwiftUI.Color("ThemeBorder")
    }

    enum Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
        static let xxl: CGFloat = 48
    }

    enum Radius {
        static let sm: CGFloat = 8
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let pill: CGFloat = 999
    }

    enum Font {
        static let largeTitle = SwiftUI.Font.system(size: 30, weight: .bold)
        static let title = SwiftUI.Font.system(size: 22, weight: .semibold)
        static let headline = SwiftUI.Font.system(size: 17, weight: .semibold)
        static let body = SwiftUI.Font.system(size: 16, weight: .regular)
        static let callout = SwiftUI.Font.system(size: 15, weight: .regular)
        static let caption = SwiftUI.Font.system(size: 13, weight: .regular)
    }
}

extension View {
    func cardStyle() -> some View {
        self
            .padding(Theme.Spacing.md)
            .background(Theme.Color.surface)
            .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.md, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: Theme.Radius.md, style: .continuous)
                    .strokeBorder(Theme.Color.border, lineWidth: 1)
            )
            .shadow(color: SwiftUI.Color.black.opacity(0.06), radius: 12, x: 0, y: 4)
    }

    func primaryButtonStyle() -> some View {
        self
            .font(Theme.Font.headline)
            .foregroundStyle(.white)
            .padding(.vertical, Theme.Spacing.sm + 2)
            .frame(maxWidth: .infinity)
            .background(Theme.Color.primary)
            .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.md, style: .continuous))
    }

    func secondaryButtonStyle() -> some View {
        self
            .font(Theme.Font.headline)
            .foregroundStyle(Theme.Color.accent)
            .padding(.vertical, Theme.Spacing.sm + 2)
            .frame(maxWidth: .infinity)
            .background(Theme.Color.accent.opacity(0.12))
            .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.md, style: .continuous))
    }
}
