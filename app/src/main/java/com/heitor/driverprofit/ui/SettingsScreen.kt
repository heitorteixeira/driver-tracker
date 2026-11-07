@file:OptIn(ExperimentalMaterial3Api::class)

package com.heitor.driverprofit.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.Settings
import com.heitor.driverprofit.domain.CostProfile
import java.util.Currency

internal val CURRENCIES = listOf(
    "BRL", "USD", "EUR", "ARS", "MXN", "COP", "CLP", "PEN", "UYU", "PYG", "BOB", "GBP", "CAD",
)

/** Language tag to native display name; the empty tag means "follow the system". */
internal val LANGUAGES = listOf("en" to "English", "es" to "Español", "pt" to "Português")

@Composable
fun SettingsScreen(settings: Settings, onChange: (Settings) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val fmt = rememberFmt(settings.currencyCode)
    val dist = distanceUnit(settings.metric)
    val vol = volumeUnit(settings.metric)

    var fuel by remember { mutableStateOf(settings.profile.fuelPrice.toEditText()) }
    var efficiency by remember { mutableStateOf(settings.profile.efficiency.toEditText()) }
    var maintenance by remember {
        mutableStateOf(settings.profile.maintenancePerDistance.toEditText())
    }
    var minDistance by remember { mutableStateOf(settings.profile.minPerDistance.toEditText()) }
    var minHour by remember { mutableStateOf(settings.profile.minPerHour.toEditText()) }

    // Saves the cost fields whenever they all hold valid numbers.
    fun pushProfile() {
        val profile = CostProfile(
            fuelPrice = fuel.toDecimal() ?: return,
            efficiency = efficiency.toDecimal() ?: return,
            maintenancePerDistance = maintenance.toDecimal() ?: return,
            minPerDistance = minDistance.toDecimal() ?: return,
            minPerHour = minHour.toDecimal() ?: return,
        )
        onChange(settings.copy(profile = profile))
    }

    val currentLanguage = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    val languageOptions = listOf("" to stringResource(R.string.language_system)) + LANGUAGES
    val languageLabel = languageOptions
        .firstOrNull { it.first == currentLanguage.substringBefore('-') }?.second
        ?: stringResource(R.string.language_system)

    val currencyOptions = (CURRENCIES + settings.currencyCode).distinct().map { code ->
        code to "$code - ${Currency.getInstance(code).getDisplayName(locale)}"
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.nav_settings)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Dropdown(
                label = stringResource(R.string.settings_language),
                selectedText = languageLabel,
                options = languageOptions,
                onSelect = { tag ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(tag),
                    )
                },
            )
            Dropdown(
                label = stringResource(R.string.settings_currency),
                selectedText = currencyOptions.first { it.first == settings.currencyCode }.second,
                options = currencyOptions,
                onSelect = { onChange(settings.copy(currencyCode = it)) },
            )
            Dropdown(
                label = stringResource(R.string.settings_units),
                selectedText = stringResource(
                    if (settings.metric) R.string.units_metric else R.string.units_imperial,
                ),
                options = listOf(
                    true to stringResource(R.string.units_metric),
                    false to stringResource(R.string.units_imperial),
                ),
                onSelect = { onChange(settings.copy(metric = it)) },
            )

            Text(
                stringResource(R.string.settings_costs),
                style = MaterialTheme.typography.titleMedium,
            )
            NumberField(
                stringResource(R.string.settings_fuel_price, vol),
                fuel,
                { fuel = it; pushProfile() },
            )
            NumberField(
                stringResource(R.string.settings_efficiency, dist, vol),
                efficiency,
                { efficiency = it; pushProfile() },
            )
            NumberField(
                stringResource(R.string.settings_maintenance, dist),
                maintenance,
                { maintenance = it; pushProfile() },
            )
            Text(
                stringResource(
                    R.string.settings_real_cost,
                    fmt.money(settings.profile.costPerDistance),
                    dist,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )

            Text(
                stringResource(R.string.settings_minimums),
                style = MaterialTheme.typography.titleMedium,
            )
            NumberField(
                stringResource(R.string.settings_min_per_distance, dist),
                minDistance,
                { minDistance = it; pushProfile() },
            )
            NumberField(
                stringResource(R.string.settings_min_per_hour),
                minHour,
                { minHour = it; pushProfile() },
            )
        }
    }
}
