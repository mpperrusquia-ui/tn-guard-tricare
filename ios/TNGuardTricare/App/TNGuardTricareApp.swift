import SwiftUI

@main
struct TNGuardTricareApp: App {
    @StateObject private var contentStore = ContentStore()
    @StateObject private var progressStore = ProgressStore()
    @StateObject private var draftStore = FormDraftStore()
    @AppStorage("hasSeenDisclaimer") private var hasSeenDisclaimer = false

    var body: some Scene {
        WindowGroup {
            Group {
                if let content = contentStore.content {
                    if hasSeenDisclaimer {
                        RootTabView()
                    } else {
                        DisclaimerView(disclaimer: content.disclaimer) {
                            hasSeenDisclaimer = true
                        }
                    }
                } else {
                    ProgressView("Loading…")
                        .tint(Theme.Color.primary)
                }
            }
            .environmentObject(contentStore)
            .environmentObject(progressStore)
            .environmentObject(draftStore)
            .task { await contentStore.load() }
        }
    }
}
