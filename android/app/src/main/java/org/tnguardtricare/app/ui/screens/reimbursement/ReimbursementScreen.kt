package org.tnguardtricare.app.ui.screens.reimbursement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.tnguardtricare.app.Routes
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.data.Track
import org.tnguardtricare.app.notifications.ReminderScheduler
import org.tnguardtricare.app.ui.components.CardSurface
import org.tnguardtricare.app.ui.components.EligibilityChecklist
import org.tnguardtricare.app.ui.components.InfoCard
import org.tnguardtricare.app.ui.components.SectionHeader
import org.tnguardtricare.app.ui.components.StepRow
import org.tnguardtricare.app.ui.theme.AppRadius
import org.tnguardtricare.app.ui.theme.AppSpacing
import org.tnguardtricare.app.ui.theme.LocalExtendedColors

private const val PREFS_NAME = "org.tnguardtricare.app.prefs"
private const val KEY_REMINDER_ENABLED = "monthly_reminder_enabled"

@Composable
fun ReimbursementScreen(app: TNGuardTricareApplication, navController: NavHostController) {
    val content by app.contentRepository.content.collectAsState()
    val completedSteps by app.progressStore.completedReimbursementSteps.collectAsState()
    val completedForms by app.progressStore.completedForms.collectAsState()
    val c = content ?: return
    val context = LocalContext.current

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_ENABLED, false)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) ReminderScheduler.scheduleMonthlyReminder(context)
    }

    fun enableReminder() {
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            ReminderScheduler.scheduleMonthlyReminder(context)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Column {
                Text(
                    "TN Premium Reimbursement",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Tennessee Medical Readiness Act — reimburses your individual TRS/dental premium, not family coverage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            InfoCard(Icons.Filled.Email, "Send your package to", c.tnReimbursement.email)
        }
        item { SectionHeader("Eligibility") }
        item { EligibilityChecklist(c.tnReimbursement.eligibility) }
        item { SectionHeader("Steps") }
        items(c.tnReimbursement.steps) { step ->
            StepRow(
                title = step.title,
                body = step.body,
                isComplete = completedSteps.contains(step.id),
                onToggle = { app.progressStore.toggleStep(step.id, Track.REIMBURSEMENT) },
            )
        }
        item { SectionHeader("Fill out the forms") }
        items(c.tnReimbursement.forms) { form ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate(Routes.formFill(form.id)) },
                shape = RoundedCornerShape(AppRadius.md),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, LocalExtendedColors.current.border),
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(form.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(form.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (completedForms.contains(form.id)) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = "Complete",
                            tint = LocalExtendedColors.current.success,
                            modifier = Modifier.padding(end = AppSpacing.xs),
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { navController.navigate(Routes.PAYMENT_HISTORY) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("How do I get my TRICARE Payment History PDF?")
            }
        }
        item { SectionHeader("Stay on track") }
        item {
            CardSurface {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Monthly submission reminder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Notifies you on the ${c.tnReimbursement.monthlyDeadlineRule}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { enabled ->
                            reminderEnabled = enabled
                            prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
                            if (enabled) {
                                enableReminder()
                            } else {
                                ReminderScheduler.cancel(context)
                            }
                        },
                    )
                }
            }
        }
    }
}
