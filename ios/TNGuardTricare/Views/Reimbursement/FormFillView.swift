import SwiftUI
import UIKit

struct FormFillView: View {
    let form: AppContent.FormDefinition
    @EnvironmentObject private var draftStore: FormDraftStore
    @EnvironmentObject private var progressStore: ProgressStore
    @EnvironmentObject private var contentStore: ContentStore

    @State private var values: [String: String] = [:]
    @State private var generatedPDFURL: IdentifiableURL?
    @State private var errorMessage: String?

    var body: some View {
        ZStack {
            Theme.Color.background.ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: Theme.Spacing.lg) {
                    if form.isPlaceholderTemplate == true {
                        HStack(alignment: .top, spacing: Theme.Spacing.sm) {
                            Image(systemName: "exclamationmark.triangle.fill")
                                .foregroundStyle(Theme.Color.warning)
                            Text("This app doesn't yet bundle the official tn.gov PDF for this form, so it generates a labeled summary with the same fields. Cross-check with the official form before sending.")
                                .font(Theme.Font.caption)
                                .foregroundStyle(Theme.Color.textSecondary)
                        }
                        .padding(Theme.Spacing.md)
                        .background(Theme.Color.warning.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: Theme.Radius.sm, style: .continuous))
                    }

                    Text(form.description)
                        .font(Theme.Font.body)
                        .foregroundStyle(Theme.Color.textSecondary)

                    VStack(spacing: Theme.Spacing.md) {
                        ForEach(form.fields) { field in
                            FormFieldInput(field: field, value: binding(for: field))
                        }
                    }
                    .padding(Theme.Spacing.md)
                    .cardStyle()

                    if let errorMessage {
                        Text(errorMessage)
                            .font(Theme.Font.caption)
                            .foregroundStyle(Theme.Color.danger)
                    }

                    Button(action: generateAndShare) {
                        Text("Preview & Share PDF")
                    }
                    .primaryButtonStyle()

                    if let email = contentStore.content?.tnReimbursement.email {
                        InfoCard(icon: "envelope.fill", title: "Send the completed package to", value: email)
                    }
                }
                .padding(Theme.Spacing.md)
            }
        }
        .navigationTitle(form.title)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear(perform: loadValues)
        .sheet(item: $generatedPDFURL) { identifiableURL in
            ShareSheet(items: [identifiableURL.url])
                .onDisappear {
                    progressStore.setFormComplete(form.id, complete: true)
                }
        }
    }

    private func binding(for field: AppContent.FormField) -> Binding<String> {
        Binding(
            get: { values[field.id] ?? "" },
            set: { newValue in
                values[field.id] = newValue
                draftStore.setValue(newValue, formId: form.id, field: field)
            }
        )
    }

    private func loadValues() {
        for field in form.fields {
            var value = draftStore.value(formId: form.id, field: field)
            // DatePicker displays a "today" fallback for an unset value but only writes back
            // to storage once the user actually changes it — without this, a date field left
            // untouched shows "today" on screen but silently saves as empty.
            if field.type == .date && value.isEmpty {
                value = Self.defaultDateFormatter.string(from: Date())
                draftStore.setValue(value, formId: form.id, field: field)
            }
            values[field.id] = value
        }
    }

    private static let defaultDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()

    private func generateAndShare() {
        errorMessage = nil
        do {
            let url = try PDFFormFiller.filledPDF(form: form, values: values)
            generatedPDFURL = IdentifiableURL(url: url)
        } catch {
            errorMessage = "Couldn't generate the PDF. Try again."
        }
    }
}

private struct FormFieldInput: View {
    let field: AppContent.FormField
    @Binding var value: String

    var body: some View {
        VStack(alignment: .leading, spacing: Theme.Spacing.xs) {
            HStack(spacing: Theme.Spacing.xs) {
                Text(field.label)
                    .font(Theme.Font.callout)
                    .foregroundStyle(Theme.Color.textPrimary)
                if field.sensitive == true {
                    Image(systemName: "lock.fill")
                        .font(.system(size: 11))
                        .foregroundStyle(Theme.Color.textSecondary)
                }
            }
            switch field.type {
            case .checkbox:
                Toggle(isOn: Binding(
                    get: { value == "true" },
                    set: { value = $0 ? "true" : "false" }
                )) {
                    EmptyView()
                }
                .labelsHidden()
            case .choice:
                Picker(field.label, selection: $value) {
                    Text(field.placeholder ?? "Select…").tag("")
                    ForEach(field.options ?? [], id: \.self) { option in
                        Text(option).tag(option)
                    }
                }
                .pickerStyle(.menu)
                .tint(Theme.Color.accent)
                .frame(maxWidth: .infinity, alignment: .leading)
            case .date:
                DatePicker(
                    "",
                    selection: Binding(
                        get: { Self.dateFormatter.date(from: value) ?? Date() },
                        set: { value = Self.dateFormatter.string(from: $0) }
                    ),
                    displayedComponents: .date
                )
                .labelsHidden()
                .datePickerStyle(.compact)
            default:
                TextField(field.placeholder ?? field.label, text: $value)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(keyboardType)
                    .italic(field.type == .signatureName)
            }
        }
    }

    private var keyboardType: UIKeyboardType {
        switch field.type {
        case .number, .ssn, .bankRouting, .bankAccount: return .numberPad
        default: return .default
        }
    }

    private static let dateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter
    }()
}
