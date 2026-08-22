package org.tnguardtricare.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.tnguardtricare.app.ui.screens.home.HomeScreen
import org.tnguardtricare.app.ui.screens.reimbursement.FormFillScreen
import org.tnguardtricare.app.ui.screens.reimbursement.PaymentHistoryExampleScreen
import org.tnguardtricare.app.ui.screens.reimbursement.ReimbursementScreen
import org.tnguardtricare.app.ui.screens.resources.ResourcesScreen
import org.tnguardtricare.app.ui.screens.settings.SettingsScreen
import org.tnguardtricare.app.ui.screens.trs.TrsScreen

object Routes {
    const val HOME = "home"
    const val TRS = "trs"
    const val REIMBURSEMENT = "reimbursement"
    const val PAYMENT_HISTORY = "reimbursement/payment-history"
    const val FORM_FILL = "reimbursement/form/{formId}"
    const val RESOURCES = "resources"
    const val SETTINGS = "settings"

    fun formFill(formId: String) = "reimbursement/form/$formId"
}

private data class BottomNavItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.TRS, "TRS", Icons.Outlined.MenuBook),
    BottomNavItem(Routes.REIMBURSEMENT, "Reimbursement", Icons.Outlined.AttachMoney),
    BottomNavItem(Routes.RESOURCES, "Resources", Icons.Filled.Menu),
    BottomNavItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings),
)

@Composable
fun RootNavHost(app: TNGuardTricareApplication) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = backStackEntry?.destination

                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(app = app, navController = navController)
            }
            composable(Routes.TRS) {
                TrsScreen(app = app)
            }
            composable(Routes.REIMBURSEMENT) {
                ReimbursementScreen(app = app, navController = navController)
            }
            composable(Routes.PAYMENT_HISTORY) {
                PaymentHistoryExampleScreen(navController = navController)
            }
            composable(Routes.FORM_FILL) { backStackEntry ->
                val formId = backStackEntry.arguments?.getString("formId") ?: return@composable
                FormFillScreen(app = app, formId = formId, navController = navController)
            }
            composable(Routes.RESOURCES) {
                ResourcesScreen(app = app)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(app = app)
            }
        }
    }
}
