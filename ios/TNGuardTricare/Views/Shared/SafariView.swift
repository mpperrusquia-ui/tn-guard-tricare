import SwiftUI
import SafariServices

/// Wraps SFSafariViewController so links like milConnect/BWE open in an in-app browser.
/// Government sites can't redirect back into this app on completion, so callers present
/// this, and when the user dismisses it (taps Done), show a "did you finish?" confirmation
/// to mark the step complete — see StepCompletionConfirmation.
struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        let config = SFSafariViewController.Configuration()
        config.entersReaderIfAvailable = false
        return SFSafariViewController(url: url, configuration: config)
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {}
}
