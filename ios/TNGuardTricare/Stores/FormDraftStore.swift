import Foundation

/// Single entry point for reading/writing form field drafts (Attestation, W-4). Routes
/// sensitive fields (SSN, bank routing/account, DoD ID) to KeychainStore and everything
/// else to a plain JSON file in Documents, based on each field's `sensitive` flag in
/// content.json — callers don't need to know which backing store a field uses.
@MainActor
final class FormDraftStore: ObservableObject {
    @Published private var nonSensitiveValues: [String: String]

    private var fileURL: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("form-drafts.json")
    }

    init() {
        nonSensitiveValues = Self.load(from: FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("form-drafts.json"))
    }

    func value(formId: String, field: AppContent.FormField) -> String {
        let key = draftKey(formId: formId, fieldId: field.id)
        if field.sensitive == true {
            return KeychainStore.get(forKey: key) ?? ""
        }
        return nonSensitiveValues[key] ?? ""
    }

    func setValue(_ value: String, formId: String, field: AppContent.FormField) {
        let key = draftKey(formId: formId, fieldId: field.id)
        if field.sensitive == true {
            if value.isEmpty {
                KeychainStore.delete(forKey: key)
            } else {
                KeychainStore.set(value, forKey: key)
            }
        } else {
            nonSensitiveValues[key] = value.isEmpty ? nil : value
            persist()
        }
    }

    func clearAll(formIds: [String], allFields: [String: [AppContent.FormField]]) {
        for formId in formIds {
            for field in allFields[formId] ?? [] where field.sensitive == true {
                KeychainStore.delete(forKey: draftKey(formId: formId, fieldId: field.id))
            }
        }
        nonSensitiveValues = [:]
        persist()
    }

    private func draftKey(formId: String, fieldId: String) -> String { "\(formId).\(fieldId)" }

    private func persist() {
        guard let data = try? JSONEncoder().encode(nonSensitiveValues) else { return }
        try? data.write(to: fileURL, options: .atomic)
    }

    private static func load(from url: URL) -> [String: String] {
        guard let data = try? Data(contentsOf: url),
              let dict = try? JSONDecoder().decode([String: String].self, from: data) else {
            return [:]
        }
        return dict
    }
}
