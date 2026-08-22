package org.tnguardtricare.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Mirrors content/content.schema.json — keep field names in sync with content.json and with
 * the iOS ContentModel.swift, since both apps read the same file.
 */
@Serializable
data class AppContent(
    val contentVersion: Int,
    val updatedAt: String,
    val trs: TrsContent,
    val tnReimbursement: ReimbursementContent,
    val faq: List<FaqItem>,
    val disclaimer: Disclaimer,
) {
    @Serializable
    data class TrsContent(
        val premiums: Premiums,
        val links: TrsLinks,
        val phoneNumbers: PhoneNumbers,
        val eligibility: List<String>,
        val steps: List<Step>,
    ) {
        @Serializable
        data class Premiums(
            val memberOnlyMonthly: String,
            val memberAndFamilyMonthly: String,
            val asOfYear: Int,
        )

        @Serializable
        data class TrsLinks(
            val milconnect: String,
            val bwe: String,
            val trsOverview: String,
            val trsEnrollmentSteps: String,
            val formsPage: String,
            val whenCoverageBegins: String,
            val costs: String,
            val handbook: String,
            val eastRegion: String,
            val humanaMilitary: String,
            val findDoctor: String,
        ) {
            fun byKey(key: String): String? = when (key) {
                "milconnect" -> milconnect
                "bwe" -> bwe
                "trsOverview" -> trsOverview
                "trsEnrollmentSteps" -> trsEnrollmentSteps
                "formsPage" -> formsPage
                "whenCoverageBegins" -> whenCoverageBegins
                "costs" -> costs
                "handbook" -> handbook
                "eastRegion" -> eastRegion
                "humanaMilitary" -> humanaMilitary
                "findDoctor" -> findDoctor
                else -> null
            }
        }

        @Serializable
        data class PhoneNumbers(
            val humanaEastLabel: String,
            val humanaEast: String,
            val triWestLabel: String,
            val triWest: String,
        )
    }

    @Serializable
    data class ReimbursementContent(
        val email: String,
        val monthlyDeadlineRule: String,
        val links: ReimbursementLinks,
        val eligibility: List<String>,
        val steps: List<Step>,
        val forms: List<FormDefinition>,
    ) {
        @Serializable
        data class ReimbursementLinks(
            val programPage: String,
            val policyPdf: String,
            val enrollmentPacketPdf: String,
            val w4Pdf: String,
        )
    }

    @Serializable
    data class Step(
        val id: String,
        val title: String,
        val body: String,
        val actionLabel: String? = null,
        val actionLinkKey: String? = null,
    )

    @Serializable
    data class FormDefinition(
        val id: String,
        val title: String,
        val description: String,
        val pdfTemplate: String,
        val isPlaceholderTemplate: Boolean? = null,
        /** "acroform" (default) or "overlay" — see FillMode. */
        val fillMode: String? = null,
        /** 0-based page to extract into the final shared PDF; null keeps the whole document. */
        val outputPage: Int? = null,
        val fields: List<FormField>,
    ) {
        val resolvedFillMode: FillMode
            get() = if (fillMode == "overlay") FillMode.OVERLAY else FillMode.ACROFORM
    }

    enum class FillMode { ACROFORM, OVERLAY }

    @Serializable
    data class FormField(
        val id: String,
        val label: String,
        val type: FieldType,
        /** Required for acroform fields — the real PDF field name to set. */
        val pdfFieldName: String? = null,
        /** Drives a dropdown in the UI; for acroform fields must match the PDF's export values. */
        val options: List<String>? = null,
        val sensitive: Boolean? = null,
        val placeholder: String? = null,
        /** Overlay mode only: where to stamp this field's value (possibly in multiple spots). */
        val overlays: List<OverlayPosition>? = null,
        /** Overlay mode only, for choice fields: where to draw a mark for each option. */
        val overlayOptions: List<OverlayChoice>? = null,
    )

    @Serializable
    enum class FieldType {
        @SerialName("text") TEXT,
        @SerialName("number") NUMBER,
        @SerialName("date") DATE,
        @SerialName("ssn") SSN,
        @SerialName("bankRouting") BANK_ROUTING,
        @SerialName("bankAccount") BANK_ACCOUNT,
        @SerialName("checkbox") CHECKBOX,
        @SerialName("signatureName") SIGNATURE_NAME,
        @SerialName("choice") CHOICE,
    }

    @Serializable
    data class OverlayPosition(
        val page: Int,
        val x: Double,
        val y: Double,
    )

    @Serializable
    data class OverlayChoice(
        val value: String,
        val page: Int,
        val x: Double,
        val y: Double,
    )

    @Serializable
    data class FaqItem(
        val question: String,
        val answer: String,
    )

    @Serializable
    data class Disclaimer(
        val title: String,
        val body: String,
    )
}
