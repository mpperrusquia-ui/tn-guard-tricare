import Foundation
import PDFKit
import UIKit

/// Fills the fields defined in a FormDefinition (from content.json) into a PDF and returns
/// a file URL ready to preview/share. Three paths, chosen by the bundled template's shape:
///
/// - **acroform**: a real, genuinely fillable PDF (e.g. the IRS W-4, or the TN enrollment
///   form) — locates each field's pre-existing widget annotation by name, across every page
///   since multi-page/multi-enclosure documents spread fields around, and sets its value.
///   `outputPage`, when set, trims the shared PDF down to just that one page afterward (moving
///   an already-filled PDFPage into a fresh PDFDocument has been verified to keep rendering
///   correctly — PDFKit regenerates each widget's own appearance stream, it doesn't depend on
///   the surrounding document).
/// - **overlay**: a real but non-interactive/flat page (e.g. the TN attestation enclosure,
///   which is just printed text with blank lines) — draws the original page as a background
///   via `CGContext.drawPDFPage`, then draws each field's value as plain text at the
///   coordinates configured in content.json.
/// - **synthesized placeholder** (no bundled file at all): draws a generic labeled page with
///   each value printed under its label. Used only until a real template is bundled.
///
/// Note on a dead end already ruled out: an earlier version of the placeholder path built its
/// own PDFAnnotation widgets from scratch and set `widgetStringValue` on them. PDFKit only
/// regenerates a widget's on-screen appearance for fields that were part of a real `/AcroForm`
/// to begin with — freshly-constructed annotations kept the value programmatically but
/// rendered blank in Preview/Files, which is why the placeholder path draws plain text instead.
enum PDFFormFiller {
    enum FillError: Error {
        case templateUnavailable
        case writeFailed
    }

    static func filledPDF(form: AppContent.FormDefinition, values: [String: String]) throws -> URL {
        let document: PDFDocument
        if let bundledURL = bundledTemplateURL(for: form) {
            switch form.resolvedFillMode {
            case .acroform:
                guard let sourceDocument = PDFDocument(url: bundledURL) else { throw FillError.templateUnavailable }
                fillAcroFormFields(sourceDocument, form: form, values: values)
                document = extractOutputPage(sourceDocument, outputPage: form.outputPage)
            case .overlay:
                document = try buildOverlayDocument(sourceURL: bundledURL, form: form, values: values)
            }
        } else {
            document = buildPlaceholderDocument(for: form, values: values)
        }

        let outputURL = FileManager.default.temporaryDirectory
            .appendingPathComponent("\(form.id)-\(UUID().uuidString).pdf")
        guard document.write(to: outputURL) else { throw FillError.writeFailed }
        return outputURL
    }

    private static func bundledTemplateURL(for form: AppContent.FormDefinition) -> URL? {
        Bundle.main.url(forResource: form.pdfTemplate.replacingOccurrences(of: ".pdf", with: ""), withExtension: "pdf")
    }

    // MARK: - acroform

    private static func fillAcroFormFields(_ document: PDFDocument, form: AppContent.FormDefinition, values: [String: String]) {
        for pageIndex in 0..<document.pageCount {
            guard let page = document.page(at: pageIndex) else { continue }
            for annotation in page.annotations {
                // PDFKit reports the *fully qualified* dotted field name for hierarchical
                // AcroForms (e.g. "topmostSubform[0].Page1[0].Step1a[0].f1_01[0]"), not just
                // the leaf name — match either an exact hit or the configured name as the
                // final path component, so content.json can keep using short leaf names.
                guard let fieldName = annotation.fieldName,
                      let field = form.fields.first(where: {
                          guard let pdfFieldName = $0.pdfFieldName else { return false }
                          return fieldName == pdfFieldName || fieldName.hasSuffix("." + pdfFieldName)
                      }) else { continue }
                switch annotation.widgetFieldType {
                case .text, .choice:
                    let rawValue = values[field.id] ?? ""
                    annotation.widgetStringValue = field.type == .date ? displayDate(from: rawValue) : rawValue
                case .button:
                    let checked = (values[field.id] ?? "").lowercased() == "true"
                    annotation.buttonWidgetState = checked ? .onState : .offState
                default:
                    break
                }
            }
        }
    }

    /// Moves a single page out of a (possibly multi-enclosure) source document into a fresh
    /// document, so sharing one filled form doesn't also share unrelated pages bundled
    /// alongside it in the same source PDF.
    private static func extractOutputPage(_ document: PDFDocument, outputPage: Int?) -> PDFDocument {
        guard let outputPage, let page = document.page(at: outputPage) else { return document }
        let output = PDFDocument()
        output.insert(page, at: 0)
        return output
    }

    // MARK: - overlay

    private static func buildOverlayDocument(sourceURL: URL, form: AppContent.FormDefinition, values: [String: String]) throws -> PDFDocument {
        guard let cgDocument = CGPDFDocument(sourceURL as CFURL) else { throw FillError.templateUnavailable }
        let pageNumber = (form.outputPage ?? 0) + 1 // CGPDFDocument pages are 1-indexed
        guard let cgPage = cgDocument.page(at: pageNumber) else { throw FillError.templateUnavailable }
        let pageRect = cgPage.getBoxRect(.mediaBox)

        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)
        let pdfData = renderer.pdfData { context in
            context.beginPage()
            let cg = context.cgContext
            cg.saveGState()
            // drawPDFPage always draws in the PDF's native bottom-left-origin space; flip into
            // it just for this call, then draw overlay text in the renderer's normal top-left,
            // y-down space (matching the coordinates content.json stores, taken directly from
            // pdfplumber's word positions, which use the same top-left convention).
            cg.translateBy(x: 0, y: pageRect.height)
            cg.scaleBy(x: 1, y: -1)
            cg.drawPDFPage(cgPage)
            cg.restoreGState()

            let textAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11),
                .foregroundColor: UIColor.black
            ]

            for field in form.fields {
                let rawValue = values[field.id] ?? ""
                guard !rawValue.isEmpty else { continue }
                if field.type == .choice {
                    guard let match = field.overlayOptions?.first(where: { $0.value == rawValue }) else { continue }
                    ("X" as NSString).draw(at: CGPoint(x: match.x, y: match.y), withAttributes: textAttrs)
                } else {
                    let displayValue = field.type == .date ? displayDate(from: rawValue) : rawValue
                    for position in field.overlays ?? [] {
                        (displayValue as NSString).draw(at: CGPoint(x: position.x, y: position.y), withAttributes: textAttrs)
                    }
                }
            }
        }
        return PDFDocument(data: pdfData) ?? PDFDocument()
    }

    /// Stored date values are "yyyy-MM-dd" (see FormFillView); printed forms read more
    /// naturally as "MM/dd/yyyy", matching the convention used elsewhere on these PDFs.
    private static func displayDate(from stored: String) -> String {
        let storageFormatter = DateFormatter()
        storageFormatter.dateFormat = "yyyy-MM-dd"
        guard let date = storageFormatter.date(from: stored) else { return stored }
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "MM/dd/yyyy"
        return displayFormatter.string(from: date)
    }

    // MARK: - synthesized placeholder

    /// Draws a clean labeled page with each field's entered value printed as static text
    /// under its label — used only until a real template is bundled for this form.
    private static func buildPlaceholderDocument(for form: AppContent.FormDefinition, values: [String: String]) -> PDFDocument {
        let pageWidth: CGFloat = 612
        let pageHeight: CGFloat = 792
        let margin: CGFloat = 48
        let rowHeight: CGFloat = 54

        let renderer = UIGraphicsPDFRenderer(bounds: CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight))
        let pdfData = renderer.pdfData { context in
            context.beginPage()
            let titleAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.boldSystemFont(ofSize: 20),
                .foregroundColor: UIColor.black
            ]
            let subtitleAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11),
                .foregroundColor: UIColor.darkGray
            ]
            let labelAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 11, weight: .semibold),
                .foregroundColor: UIColor.black
            ]
            let valueAttrs: [NSAttributedString.Key: Any] = [
                .font: UIFont.systemFont(ofSize: 13),
                .foregroundColor: UIColor.black
            ]

            (form.title as NSString).draw(at: CGPoint(x: margin, y: 40), withAttributes: titleAttrs)
            ("Generated by TN Guard Tricare Helper — placeholder template, verify against the official form at tn.gov before sending." as NSString)
                .draw(in: CGRect(x: margin, y: 66, width: pageWidth - margin * 2, height: 28), withAttributes: subtitleAttrs)

            var y: CGFloat = 110
            for field in form.fields {
                (field.label as NSString).draw(at: CGPoint(x: margin, y: y), withAttributes: labelAttrs)
                let boxRect = CGRect(x: margin, y: y + 16, width: pageWidth - margin * 2, height: 22)
                UIColor.lightGray.setStroke()
                context.cgContext.stroke(boxRect)

                let rawValue = values[field.id] ?? ""
                let displayValue = field.type == .checkbox ? (rawValue.lowercased() == "true" ? "Yes" : "No") : rawValue
                if !displayValue.isEmpty {
                    (displayValue as NSString).draw(
                        at: CGPoint(x: margin + 6, y: y + 19),
                        withAttributes: valueAttrs
                    )
                }
                y += rowHeight
            }
        }

        return PDFDocument(data: pdfData) ?? PDFDocument()
    }
}
