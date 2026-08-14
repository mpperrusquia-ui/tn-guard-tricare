import Foundation

/// Mirrors content/content.schema.json. Keep field names in sync with content.json.
struct AppContent: Codable, Equatable {
    var contentVersion: Int
    var updatedAt: String
    var trs: TRSContent
    var tnReimbursement: ReimbursementContent
    var faq: [FAQItem]
    var disclaimer: Disclaimer

    struct TRSContent: Codable, Equatable {
        var premiums: Premiums
        var links: TRSLinks
        var phoneNumbers: PhoneNumbers
        var eligibility: [String]
        var steps: [Step]

        struct Premiums: Codable, Equatable {
            var memberOnlyMonthly: String
            var memberAndFamilyMonthly: String
            var asOfYear: Int
        }

        struct TRSLinks: Codable, Equatable {
            var milconnect: String
            var bwe: String
            var trsOverview: String
            var trsEnrollmentSteps: String
            var formsPage: String
            var whenCoverageBegins: String
            var costs: String
            var handbook: String
            var eastRegion: String
            var humanaMilitary: String
            var findDoctor: String

            subscript(key: String) -> String? {
                switch key {
                case "milconnect": return milconnect
                case "bwe": return bwe
                case "trsOverview": return trsOverview
                case "trsEnrollmentSteps": return trsEnrollmentSteps
                case "formsPage": return formsPage
                case "whenCoverageBegins": return whenCoverageBegins
                case "costs": return costs
                case "handbook": return handbook
                case "eastRegion": return eastRegion
                case "humanaMilitary": return humanaMilitary
                case "findDoctor": return findDoctor
                default: return nil
                }
            }
        }

        struct PhoneNumbers: Codable, Equatable {
            var humanaEastLabel: String
            var humanaEast: String
            var triWestLabel: String
            var triWest: String
        }
    }

    struct ReimbursementContent: Codable, Equatable {
        var email: String
        var monthlyDeadlineRule: String
        var links: ReimbursementLinks
        var eligibility: [String]
        var steps: [Step]
        var forms: [FormDefinition]

        struct ReimbursementLinks: Codable, Equatable {
            var programPage: String
            var policyPdf: String
            var enrollmentPacketPdf: String
            var w4Pdf: String
        }
    }

    struct Step: Codable, Equatable, Identifiable {
        var id: String
        var title: String
        var body: String
        var actionLabel: String?
        var actionLinkKey: String?
    }

    struct FormDefinition: Codable, Hashable, Identifiable {
        var id: String
        var title: String
        var description: String
        var pdfTemplate: String
        var isPlaceholderTemplate: Bool?
        /// "acroform" (real interactive PDF fields, matched by name) or "overlay" (a real but
        /// non-interactive/flat page — values are drawn as text on top of it). Defaults to
        /// "acroform" when absent so older content stays valid.
        var fillMode: String?
        /// 0-based page to extract into the final shared PDF. When nil, the whole bundled
        /// document is kept (e.g. the W-4, whose extra pages are legitimate IRS instructions).
        var outputPage: Int?
        var fields: [FormField]

        var resolvedFillMode: FillMode { fillMode == "overlay" ? .overlay : .acroform }

        enum FillMode { case acroform, overlay }
    }

    struct FormField: Codable, Hashable, Identifiable {
        var id: String
        var label: String
        var type: FieldType
        /// Required for acroform fields — the real PDF field name to set.
        var pdfFieldName: String?
        /// Drives a Picker in the UI. For acroform fields these must exactly match the PDF's
        /// export values (e.g. the real dropdown options on the enrollment form).
        var options: [String]?
        var sensitive: Bool?
        var placeholder: String?
        /// Overlay mode only: where to stamp this field's value (or the same value in more
        /// than one spot, e.g. a name repeated in a sentence and in a signature block).
        var overlays: [OverlayPosition]?
        /// Overlay mode only, for choice-type fields: where to draw a mark for each option.
        var overlayOptions: [OverlayChoice]?

        enum FieldType: String, Codable, Hashable {
            case text, number, date, ssn, bankRouting, bankAccount, checkbox, signatureName, choice
        }
    }

    struct OverlayPosition: Codable, Hashable {
        var page: Int
        var x: Double
        var y: Double
    }

    struct OverlayChoice: Codable, Hashable {
        var value: String
        var page: Int
        var x: Double
        var y: Double
    }

    struct FAQItem: Codable, Equatable, Identifiable {
        var id: String { question }
        var question: String
        var answer: String
    }

    struct Disclaimer: Codable, Equatable {
        var title: String
        var body: String
    }
}
