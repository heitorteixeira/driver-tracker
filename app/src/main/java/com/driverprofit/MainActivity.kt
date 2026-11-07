package com.driverprofit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.driverprofit.ui.AppViewModel
import com.driverprofit.ui.CalculatorScreen
import com.driverprofit.ui.DashboardScreen
import com.driverprofit.ui.DriverProfitTheme
import com.driverprofit.ui.EntryScreen
import com.driverprofit.ui.FeedbackScreen
import com.driverprofit.ui.HistoryScreen
import com.driverprofit.ui.OnboardingScreen
import com.driverprofit.ui.SettingsScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DriverProfitTheme {
                AppRoot()
            }
        }
    }
}

private data class Tab(val route: String, val labelRes: Int, val icon: ImageVector)

private val TABS = listOf(
    Tab("dashboard", R.string.nav_dashboard, Icons.Default.Home),
    Tab("calculator", R.string.nav_calculator, Icons.Default.Calculate),
    Tab("history", R.string.nav_history, Icons.AutoMirrored.Filled.List),
    Tab("settings", R.string.nav_settings, Icons.Default.Settings),
)

@Composable
private fun AppRoot(vm: AppViewModel = viewModel()) {
    val settings by vm.settings.collectAsState()
    val entries by vm.entries.collectAsState()
    val s = settings ?: return

    if (!s.onboarded) {
        OnboardingScreen(s, onDone = vm::saveSettings)
        return
    }

    val nav = rememberNavController()
    val route = nav.currentBackStackEntryAsState().value?.destination?.route
    val showBar = TABS.any { it.route == route }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo("dashboard") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding),
        ) {
            composable("dashboard") {
                DashboardScreen(entries, s, onAdd = { nav.navigate("entry/0") })
            }
            composable("calculator") { CalculatorScreen(s) }
            composable("history") {
                HistoryScreen(
                    entries = entries,
                    settings = s,
                    onAdd = { nav.navigate("entry/0") },
                    onOpen = { id -> nav.navigate("entry/$id") },
                )
            }
            composable("settings") {
                SettingsScreen(
                    s,
                    onChange = vm::saveSettings,
                    onOpenFeedback = { nav.navigate("feedback") },
                )
            }
            composable("feedback") { FeedbackScreen(onBack = { nav.popBackStack() }) }
            composable(
                route = "entry/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { backStackEntry ->
                EntryScreen(
                    id = backStackEntry.arguments?.getLong("id") ?: 0L,
                    settings = s,
                    vm = vm,
                    onDone = { nav.popBackStack() },
                )
            }
        }
    }
}
