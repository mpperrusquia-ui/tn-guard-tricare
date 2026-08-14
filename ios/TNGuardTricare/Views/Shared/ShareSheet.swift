import SwiftUI
import UIKit

/// Wraps a URL for use with `.sheet(item:)` without assuming whether the SDK already
/// conforms `URL` to `Identifiable` (varies by SDK version — safer not to guess).
struct IdentifiableURL: Identifiable {
    let id = UUID()
    let url: URL
}

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
