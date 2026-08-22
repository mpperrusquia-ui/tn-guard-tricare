package org.tnguardtricare.app.ui.screens.reimbursement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavHostController
import org.tnguardtricare.app.ui.components.CardSurface
import org.tnguardtricare.app.ui.theme.AppSpacing
import org.tnguardtricare.app.ui.theme.LocalExtendedColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryExampleScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(AppSpacing.md)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Text(
                "Getting your TRICARE Payment History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            InstructionRow(1, "Log into the TRICARE / Humana Military beneficiary portal.")
            InstructionRow(2, "Go to Billing → Payment History.")
            InstructionRow(3, "Save or print the page as a PDF — it should show your plan, paid-through dates, and amounts.")
            InstructionRow(4, "Attach that PDF, along with your filled forms, in your email to tntricare@tn.gov.")

            Text("What it should look like", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            CardSurface {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("EXAMPLE", style = MaterialTheme.typography.labelSmall, color = LocalExtendedColors.current.warning)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.sm))
                ExampleRow("TRICARE Reserve Select", "Jan 1 – Jan 31, 2026", "$57.88")
                ExampleRow("TRICARE Reserve Select", "Feb 1 – Feb 28, 2026", "$57.88")
                ExampleRow("TRICARE Reserve Select", "Mar 1 – Mar 31, 2026", "$57.88")
            }

            Text(
                "This is a mock layout for reference only — not a real bill or real personal data.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstructionRow(number: Int, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(number.toString(), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelMedium)
        }
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = AppSpacing.sm),
        )
    }
}

@Composable
private fun ExampleRow(plan: String, period: String, amount: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(plan, style = MaterialTheme.typography.bodyMedium)
            Text(period, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, style = MaterialTheme.typography.bodyMedium)
            Text("Paid", style = MaterialTheme.typography.bodySmall, color = LocalExtendedColors.current.success)
        }
    }
}

