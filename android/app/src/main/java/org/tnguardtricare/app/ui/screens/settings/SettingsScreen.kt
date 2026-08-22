package org.tnguardtricare.app.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.notifications.ReminderScheduler
import org.tnguardtricare.app.ui.components.CardSurface
import org.tnguardtricare.app.ui.components.SectionHeader
import org.tnguardtricare.app.ui.theme.AppSpacing
import org.tnguardtricare.app.ui.theme.LocalExtendedColors

private const val PREFS_NAME = "org.tnguardtricare.app.prefs"
private const val KEY_REMINDER_ENABLED = "monthly_reminder_enabled"

@Composable
fun SettingsScreen(app: TNGuardTricareApplication) {
    val content by app.contentRepository.content.collectAsState()
    val isRefreshing by app.contentRepository.isRefreshing.collectAsState()
    val lastRefreshError by app.contentRepository.lastRefreshError.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var reminderEnabled by remember { mutableStateOf(prefs.getBoolean(KEY_REMINDER_ENABLED, false)) }
    var showClearConfirmation by remember { mutableStateOf(false) }
    var showClearedToast by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) ReminderScheduler.scheduleMonthlyReminder(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.md)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        SectionHeader("Content")
        CardSurface {
            content?.let {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Content version", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "v${it.contentVersion} · ${it.updatedAt}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OutlinedButton(
                onClick = { scope.launch { app.contentRepository.refreshFromRemote() } },
                modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.sm),
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = AppSpacing.sm))
                }
                Text("Check for content updates")
            }
            lastRefreshError?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        SectionHeader("Notifications")
        CardSurface {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Monthly reimbursement reminder",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { enabled ->
                        reminderEnabled = enabled
                        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
                        if (enabled) {
                            val needsPermission = Build.VERSION.SDK_INT >= 33 &&
                                androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            if (needsPermission) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                ReminderScheduler.scheduleMonthlyReminder(context)
                            }
                        } else {
                            ReminderScheduler.cancel(context)
                        }
                    },
                )
            }
        }

        SectionHeader("Your data")
        CardSurface {
            Text(
                "Everything you enter — checklist progress and form fields — stays on this device. Sensitive fields (SSN, bank info) are stored encrypted via the Android Keystore. Nothing is sent anywhere unless you share a generated PDF yourself.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { showClearConfirmation = true },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LocalExtendedColors.current.danger),
                modifier = Modifier.padding(top = AppSpacing.sm),
            ) {
                Text("Clear All Saved Data")
            }
        }

        if (showClearedToast) {
            Text("All saved data cleared.", color = LocalExtendedColors.current.success)
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear all saved progress, form drafts, and secure data?") },
            confirmButton = {
                TextButton(onClick = {
                    app.progressStore.resetAll()
                    content?.let { app.formDraftStore.clearAll(it.tnReimbursement.forms) }
                    reminderEnabled = false
                    prefs.edit().putBoolean(KEY_REMINDER_ENABLED, false).apply()
                    ReminderScheduler.cancel(context)
                    showClearConfirmation = false
                    showClearedToast = true
                }) { Text("Clear everything", color = LocalExtendedColors.current.danger) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) { Text("Cancel") }
            },
        )
    }
}
