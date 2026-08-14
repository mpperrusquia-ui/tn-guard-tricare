import Foundation

/// Tracks which checklist steps and forms the user has marked complete. Non-sensitive —
/// just step IDs — so it's stored as a plain JSON file in the app's Documents directory.
/// Sensitive form field values (SSN, bank info) live in KeychainStore instead.
@MainActor
final class ProgressStore: ObservableObject {
    @Published private(set) var completedTRSSteps: Set<String>
    @Published private(set) var completedReimbursementSteps: Set<String>
    @Published private(set) var completedForms: Set<String>

    private struct State: Codable {
        var completedTRSSteps: Set<String> = []
        var completedReimbursementSteps: Set<String> = []
        var completedForms: Set<String> = []
    }

    private var fileURL: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("progress.json")
    }

    init() {
        let loaded = Self.load(from: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("progress.json"))
        completedTRSSteps = loaded.completedTRSSteps
        completedReimbursementSteps = loaded.completedReimbursementSteps
        completedForms = loaded.completedForms
    }

    func isStepComplete(_ id: String, track: Track) -> Bool {
        switch track {
        case .trs: return completedTRSSteps.contains(id)
        case .reimbursement: return completedReimbursementSteps.contains(id)
        }
    }

    func toggleStep(_ id: String, track: Track) {
        switch track {
        case .trs:
            if completedTRSSteps.contains(id) { completedTRSSteps.remove(id) } else { completedTRSSteps.insert(id) }
        case .reimbursement:
            if completedReimbursementSteps.contains(id) { completedReimbursementSteps.remove(id) } else { completedReimbursementSteps.insert(id) }
        }
        persist()
    }

    func setStepComplete(_ id: String, track: Track, complete: Bool) {
        switch track {
        case .trs:
            if complete { completedTRSSteps.insert(id) } else { completedTRSSteps.remove(id) }
        case .reimbursement:
            if complete { completedReimbursementSteps.insert(id) } else { completedReimbursementSteps.remove(id) }
        }
        persist()
    }

    func isFormComplete(_ id: String) -> Bool { completedForms.contains(id) }

    func setFormComplete(_ id: String, complete: Bool) {
        if complete { completedForms.insert(id) } else { completedForms.remove(id) }
        persist()
    }

    func progressFraction(track: Track, totalSteps: Int) -> Double {
        guard totalSteps > 0 else { return 0 }
        let completedCount: Int
        switch track {
        case .trs: completedCount = completedTRSSteps.count
        case .reimbursement: completedCount = completedReimbursementSteps.count
        }
        return min(1.0, Double(completedCount) / Double(totalSteps))
    }

    func resetAll() {
        completedTRSSteps = []
        completedReimbursementSteps = []
        completedForms = []
        persist()
    }

    enum Track: Hashable {
        case trs
        case reimbursement
    }

    private func persist() {
        let state = State(
            completedTRSSteps: completedTRSSteps,
            completedReimbursementSteps: completedReimbursementSteps,
            completedForms: completedForms
        )
        guard let data = try? JSONEncoder().encode(state) else { return }
        try? data.write(to: fileURL, options: .atomic)
    }

    private static func load(from url: URL) -> State {
        guard let data = try? Data(contentsOf: url),
              let state = try? JSONDecoder().decode(State.self, from: data) else {
            return State()
        }
        return state
    }
}
