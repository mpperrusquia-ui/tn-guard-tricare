package org.tnguardtricare.app.ui.screens.trs

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.data.Track
import org.tnguardtricare.app.model.AppContent
import org.tnguardtricare.app.ui.components.EligibilityChecklist
import org.tnguardtricare.app.ui.components.InfoCard
import org.tnguardtricare.app.ui.components.SectionHeader
import org.tnguardtricare.app.ui.components.StepRow
import org.tnguardtricare.app.ui.theme.AppSpacing

@Composable
fun TrsScreen(app: TNGuardTricareApplication) {
    val content by app.contentRepository.content.collectAsState()
    val completedSteps by app.progressStore.completedTrsSteps.collectAsState()
    val c = content ?: return
    val context = LocalContext.current

    var pendingStep by remember { mutableStateOf<AppContent.Step?>(null) }
    var showConfirmation by remember { mutableStateOf(false) }

    // Gov sites can't redirect completion back into the app, so we treat "the user returned
    // to our Activity after we launched a Custom Tab" as the moment to ask if they finished —
    // mirrors iOS's SafariView-dismiss-triggers-confirmation pattern.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pendingStep != null) {
                showConfirmation = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun openLink(step: AppContent.Step) {
        val key = step.actionLinkKey ?: return
        val url = c.trs.links.byKey(key) ?: return
        pendingStep = step
        CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Column {
                Text(
                    "TRICARE Reserve Select",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Available year-round — not tied to Open Season. Purchase any time you qualify.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    InfoCard(Icons.Filled.Person, "Member only / mo", "$${c.trs.premiums.memberOnlyMonthly}")
                }
                Column(modifier = Modifier.weight(1f)) {
                    InfoCard(Icons.Filled.Group, "Member + family / mo", "$${c.trs.premiums.memberAndFamilyMonthly}")
                }
            }
        }
        item { SectionHeader("Eligibility") }
        item { EligibilityChecklist(c.trs.eligibility) }
        item { SectionHeader("Steps") }
        items(c.trs.steps) { step ->
            StepRow(
                title = step.title,
                body = step.body,
                isComplete = completedSteps.contains(step.id),
                onToggle = { app.progressStore.toggleStep(step.id, Track.TRS) },
                actionLabel = step.actionLabel,
                onAction = if (step.actionLinkKey != null) {
                    { openLink(step) }
                } else null,
            )
        }
        item { SectionHeader("Talk to your regional contractor") }
        item {
            InfoCard(Icons.Filled.Phone, c.trs.phoneNumbers.humanaEastLabel, formatPhone(c.trs.phoneNumbers.humanaEast))
        }
        item {
            InfoCard(Icons.Filled.Phone, c.trs.phoneNumbers.triWestLabel, formatPhone(c.trs.phoneNumbers.triWest))
        }
    }

    if (showConfirmation && pendingStep != null) {
        val step = pendingStep!!
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
                pendingStep = null
            },
            title = { Text("Did you finish: ${step.title}?") },
            confirmButton = {
                TextButton(onClick = {
                    app.progressStore.setStepComplete(step.id, Track.TRS, true)
                    showConfirmation = false
                    pendingStep = null
                }) { Text("Yes, mark complete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    pendingStep = null
                }) { Text("Not yet") }
            },
        )
    }
}

private fun formatPhone(digits: String): String {
    if (digits.length != 10) return digits
    return "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
}
