package org.tnguardtricare.app.ui.screens.resources

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.ui.components.CardSurface
import org.tnguardtricare.app.ui.components.SectionHeader
import org.tnguardtricare.app.ui.theme.AppSpacing

@Composable
fun ResourcesScreen(app: TNGuardTricareApplication) {
    val content by app.contentRepository.content.collectAsState()
    val c = content ?: return
    val context = LocalContext.current

    fun open(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Text(
                "Resources",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        item { SectionHeader("Official links") }
        item {
            CardSurface {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    LinkRow("TRS Overview", c.trs.links.trsOverview, ::open)
                    LinkRow("TRS Enrollment Steps", c.trs.links.trsEnrollmentSteps, ::open)
                    LinkRow("milConnect", c.trs.links.milconnect, ::open)
                    LinkRow("Beneficiary Web Enrollment", c.trs.links.bwe, ::open)
                    LinkRow("TRS Forms Page", c.trs.links.formsPage, ::open)
                    LinkRow("When Coverage Begins", c.trs.links.whenCoverageBegins, ::open)
                    LinkRow("Compare Costs", c.trs.links.costs, ::open)
                    LinkRow("National Guard & Reserve Handbook", c.trs.links.handbook, ::open)
                    LinkRow("Find a Doctor", c.trs.links.findDoctor, ::open)
                    LinkRow("TN Medical Readiness Act Program Page", c.tnReimbursement.links.programPage, ::open)
                    LinkRow("TN Program Policy (PDF)", c.tnReimbursement.links.policyPdf, ::open)
                    LinkRow("Enrollment Packet (PDF)", c.tnReimbursement.links.enrollmentPacketPdf, ::open)
                    LinkRow("2026 Form W-4 (PDF)", c.tnReimbursement.links.w4Pdf, ::open)
                }
            }
        }
        item { SectionHeader("FAQ") }
        items(c.faq) { item ->
            CardSurface {
                Text(item.question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    item.answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.xs),
                )
            }
        }
        item { SectionHeader("Disclaimer") }
        item {
            CardSurface {
                Text(c.disclaimer.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LinkRow(title: String, url: String, onOpen: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen(url) },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
    }
}
