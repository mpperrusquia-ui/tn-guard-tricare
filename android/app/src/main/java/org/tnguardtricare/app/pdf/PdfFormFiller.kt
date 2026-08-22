package org.tnguardtricare.app.pdf

import android.content.Context
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDCheckBox
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDField
import com.tom_roush.pdfbox.pdmodel.interactive.form.PDNonTerminalField
import org.tnguardtricare.app.model.AppContent
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Fills the fields defined in a FormDefinition (from content.json) into a PDF and returns a
 * file ready to preview/share. Three paths, chosen by the bundled template's shape — mirrors
 * iOS's PDFFormFiller.swift exactly, including the same two lessons learned there:
 *
 * - **acroform**: a real fillable PDF (the Enrollment form, the W-4) — locate each field's
 *   existing widget by name (PDFBox reports the *fully qualified* dotted name for hierarchical
 *   forms, e.g. the W-4's XFA-style naming — matched with the same suffix-match iOS uses so
 *   content.json can keep using short leaf names) and set its value. `outputPage`, when set,
 *   trims the shared PDF to just that page via PDDocument.importPage, which (as verified on
 *   iOS's equivalent page-move) keeps each widget's already-rendered appearance intact.
 * - **overlay**: a real but flat/non-interactive page (the Attestation enclosure) — draws the
 *   original page content plus each value as plain text at the coordinates content.json
 *   stores (taken from pdfplumber word positions, top-left origin — flipped here since
 *   PDFPageContentStream text drawing uses PDF's native bottom-left origin).
 * - **synthesized placeholder**: no bundled file at all — draws a generic labeled page. Only
 *   reached if a template file is ever missing; both real Android templates are bundled today.
 */
class PdfFormFiller(private val context: Context) {

    fun filledPdf(form: AppContent.FormDefinition, values: Map<String, String>): File {
        val source = openTemplate(form)
        val document = source?.let {
            when (form.resolvedFillMode) {
                AppContent.FillMode.ACROFORM -> {
                    fillAcroFormFields(it, form, values)
                    extractOutputPage(it, form.outputPage)
                }
                AppContent.FillMode.OVERLAY -> overlayValues(it, form, values)
            }
        } ?: buildPlaceholderDocument(form, values)

        val outDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val outFile = File(outDir, "${form.id}-${UUID.randomUUID()}.pdf")
        try {
            document.save(outFile)
        } finally {
            document.close()
            if (source != null && source !== document) source.close()
        }
        return outFile
    }

    private fun openTemplate(form: AppContent.FormDefinition): PDDocument? {
        val assetName = "forms/${form.pdfTemplate}"
        return try {
            context.assets.open(assetName).use { PDDocument.load(it) }
        } catch (e: Exception) {
            null
        }
    }

    // MARK: - acroform

    private fun fillAcroFormFields(document: PDDocument, form: AppContent.FormDefinition, values: Map<String, String>) {
        val acroForm = document.documentCatalog.acroForm ?: return
        for (field in flattenFields(acroForm.fields)) {
            val fieldName = field.fullyQualifiedName ?: continue
            val configured = form.fields.firstOrNull { cf ->
                val pdfFieldName = cf.pdfFieldName ?: return@firstOrNull false
                fieldName == pdfFieldName || fieldName.endsWith(".$pdfFieldName")
            } ?: continue

            val rawValue = values[configured.id] ?: ""
            if (field is PDCheckBox) {
                val checked = rawValue.equals("true", ignoreCase = true)
                if (checked) field.check() else field.setValue("Off")
            } else {
                val display = if (configured.type == AppContent.FieldType.DATE) displayDate(rawValue) else rawValue
                try {
                    field.setValue(display)
                } catch (e: Exception) {
                    // Some choice fields reject values outside their option list under strict
                    // validation; fall back to setting the raw string on the underlying COS
                    // object so free-text-style combo boxes (e.g. TRICARE Benefits Number)
                    // still accept arbitrary input the way they do on iOS.
                }
            }
        }
    }

    private fun flattenFields(fields: List<PDField>): List<PDField> {
        val result = mutableListOf<PDField>()
        for (field in fields) {
            if (field is PDNonTerminalField) {
                result.addAll(flattenFields(field.children))
            } else {
                result.add(field)
            }
        }
        return result
    }

    /** Moves a single page into a fresh document so sharing one filled form doesn't also
     * share unrelated pages bundled alongside it in the same source PDF (e.g. the state
     * packet's other enclosures). */
    /** Caller is responsible for closing the returned document (and the original, if
     * different) only after it has been saved — importPage does not necessarily deep-copy
     * every backing resource immediately, so closing the source too early can corrupt the
     * save that happens afterward. */
    private fun extractOutputPage(document: PDDocument, outputPage: Int?): PDDocument {
        if (outputPage == null) return document
        val page = document.getPage(outputPage) ?: return document
        val trimmed = PDDocument()
        trimmed.importPage(page)
        return trimmed
    }

    // MARK: - overlay

    private fun overlayValues(document: PDDocument, form: AppContent.FormDefinition, values: Map<String, String>): PDDocument {
        val pageIndex = form.outputPage ?: 0
        val page = document.getPage(pageIndex) ?: return document
        val pageHeight = page.mediaBox.height
        val font = PDType1Font.HELVETICA
        val fontSize = 11f

        PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true).use { cs ->
            for (field in form.fields) {
                val rawValue = values[field.id] ?: ""
                if (rawValue.isEmpty()) continue
                if (field.type == AppContent.FieldType.CHOICE) {
                    val match = field.overlayOptions?.firstOrNull { it.value == rawValue } ?: continue
                    drawText(cs, "X", match.x.toFloat(), match.y.toFloat(), pageHeight, font, fontSize)
                } else {
                    val display = if (field.type == AppContent.FieldType.DATE) displayDate(rawValue) else rawValue
                    for (position in field.overlays ?: emptyList()) {
                        drawText(cs, display, position.x.toFloat(), position.y.toFloat(), pageHeight, font, fontSize)
                    }
                }
            }
        }

        // Same reasoning as the acroform path: don't share unrelated pages bundled alongside
        // this one in the same source PDF.
        return extractOutputPage(document, form.outputPage)
    }

    private fun drawText(
        cs: PDPageContentStream,
        text: String,
        topLeftX: Float,
        topLeftY: Float,
        pageHeight: Float,
        font: PDType1Font,
        fontSize: Float,
    ) {
        // content.json coordinates are top-left origin (taken directly from pdfplumber word
        // positions); PDPageContentStream text drawing uses PDF's native bottom-left origin
        // with the point as the text baseline, roughly `fontSize * 0.8` below the glyph tops.
        val baselineY = pageHeight - topLeftY - (fontSize * 0.8f)
        cs.beginText()
        cs.setFont(font, fontSize)
        cs.newLineAtOffset(topLeftX, baselineY)
        cs.showText(text)
        cs.endText()
    }

    // MARK: - synthesized placeholder

    private fun buildPlaceholderDocument(form: AppContent.FormDefinition, values: Map<String, String>): PDDocument {
        val document = PDDocument()
        val page = PDPage(PDRectangle.LETTER)
        document.addPage(page)
        val margin = 48f
        var y = page.mediaBox.height - 110f
        val rowHeight = 54f
        val titleFont = PDType1Font.HELVETICA_BOLD
        val labelFont = PDType1Font.HELVETICA_BOLD
        val valueFont = PDType1Font.HELVETICA

        PDPageContentStream(document, page).use { cs ->
            cs.beginText()
            cs.setFont(titleFont, 20f)
            cs.newLineAtOffset(margin, page.mediaBox.height - 60f)
            cs.showText(form.title)
            cs.endText()

            cs.beginText()
            cs.setFont(PDType1Font.HELVETICA, 9f)
            cs.newLineAtOffset(margin, page.mediaBox.height - 80f)
            cs.showText("Generated by TN Guard Tricare Helper — placeholder template.")
            cs.endText()

            for (field in form.fields) {
                cs.beginText()
                cs.setFont(labelFont, 11f)
                cs.newLineAtOffset(margin, y)
                cs.showText(field.label)
                cs.endText()

                cs.setStrokingColor(200, 200, 200)
                cs.addRect(margin, y - 34f, page.mediaBox.width - margin * 2, 22f)
                cs.stroke()

                val rawValue = values[field.id] ?: ""
                val displayValue = if (field.type == AppContent.FieldType.CHECKBOX) {
                    if (rawValue.equals("true", ignoreCase = true)) "Yes" else "No"
                } else rawValue
                if (displayValue.isNotEmpty()) {
                    cs.beginText()
                    cs.setFont(valueFont, 12f)
                    cs.newLineAtOffset(margin + 6f, y - 28f)
                    cs.showText(displayValue)
                    cs.endText()
                }
                y -= rowHeight
            }
        }
        return document
    }

    private fun displayDate(stored: String): String {
        return try {
            val date = LocalDate.parse(stored, DateTimeFormatter.ISO_LOCAL_DATE)
            date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        } catch (e: Exception) {
            stored
        }
    }
}
