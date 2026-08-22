package org.tnguardtricare.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import org.tnguardtricare.app.Routes
import org.tnguardtricare.app.TNGuardTricareApplication
import org.tnguardtricare.app.data.Track
import org.tnguardtricare.app.ui.components.InfoCard
import org.tnguardtricare.app.ui.components.ProgressCard
import org.tnguardtricare.app.ui.components.SectionHeader
import org.tnguardtricare.app.ui.theme.AppSpacing

@Composable
fun HomeScreen(app: TNGuardTricareApplication, navController: NavHostController) {
    val content by app.contentRepository.content.collectAsState()
    val completedTrs by app.progressStore.completedTrsSteps.collectAsState()
    val completedReimbursement by app.progressStore.completedReimbursementSteps.collectAsState()

    val c = content ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        item {
            Column {
                Text(
                    "Welcome, Guardsman",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    "Two things to get done: enroll in TRICARE Reserve Select, then get reimbursed for it by the state.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            ProgressCard(
                icon = Icons.Filled.HealthAndSafety,
                title = "1. Enroll in TRICARE Reserve Select",
                subtitle = "Federal enrollment via milConnect",
                fraction = app.progressStore.progressFraction(Track.TRS, c.trs.steps.size),
                onClick = { navController.navigate(Routes.TRS) },
            )
        }
        item {
            ProgressCard(
                icon = Icons.Filled.AttachMoney,
                title = "2. TN Premium Reimbursement",
                subtitle = "Get your individual premium paid back by the state",
                fraction = app.progressStore.progressFraction(Track.REIMBURSEMENT, c.tnReimbursement.steps.size),
                onClick = { navController.navigate(Routes.REIMBURSEMENT) },
            )
        }
        item { SectionHeader("Good to know") }
        item {
            InfoCard(
                icon = Icons.Filled.Email,
                title = "TN Tricare reimbursement email",
                value = c.tnReimbursement.email,
            )
        }
        item {
            InfoCard(
                icon = Icons.Filled.CalendarMonth,
                title = "Monthly submission deadline",
                value = c.tnReimbursement.monthlyDeadlineRule,
            )
        }
    }
}
