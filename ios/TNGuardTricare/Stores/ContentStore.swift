import Foundation

/// Loads app content with a "bundled first, remote refresh in background" strategy so the
/// app always renders instantly and never blocks on network. Point `remoteURL` at a hosted
/// copy of content/content.json (see docs/CONTENT_UPDATE_GUIDE.md) to enable over-the-air
/// content updates without an App Store release.
@MainActor
final class ContentStore: ObservableObject {
    @Published private(set) var content: AppContent?
    @Published private(set) var isRefreshing = false
    @Published private(set) var lastRefreshError: String?

    /// Hosted on GitHub — edit content/content.json in the tn-guard-tricare repo (bump
    /// contentVersion) and installed apps pick it up on next launch/refresh, no App Store
    /// release needed. Falls back to the bundled copy if this is ever unreachable.
    private let remoteURL: URL? = URL(string: "https://raw.githubusercontent.com/mpperrusquia-ui/tn-guard-tricare/main/content/content.json")

    private var cacheURL: URL {
        FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("content-cache.json")
    }

    func load() async {
        if content == nil {
            content = loadCached() ?? loadBundled()
        }
        await refreshFromRemote()
    }

    func refreshFromRemote() async {
        guard let remoteURL else { return }
        isRefreshing = true
        defer { isRefreshing = false }
        do {
            let (data, _) = try await URLSession.shared.data(from: remoteURL)
            let decoded = try JSONDecoder().decode(AppContent.self, from: data)
            if let current = content, decoded.contentVersion < current.contentVersion {
                return
            }
            content = decoded
            try? data.write(to: cacheURL, options: .atomic)
            lastRefreshError = nil
        } catch {
            lastRefreshError = error.localizedDescription
        }
    }

    private func loadCached() -> AppContent? {
        guard let data = try? Data(contentsOf: cacheURL) else { return nil }
        return try? JSONDecoder().decode(AppContent.self, from: data)
    }

    private func loadBundled() -> AppContent? {
        guard let url = Bundle.main.url(forResource: "content", withExtension: "json") else {
            assertionFailure("Bundled content.json is missing from the app target.")
            return nil
        }
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(AppContent.self, from: data)
    }
}
