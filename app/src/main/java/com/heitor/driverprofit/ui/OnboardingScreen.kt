package com.heitor.driverprofit.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.Settings
import java.util.Currency

/**
 * First-run setup asking for language, currency and units. Changing the language
 * recreates the activity, so the picks are kept with [rememberSaveable].
 */
@Composable
fun OnboardingScreen(initial: Settings, onDone: (Settings) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    var currency by rememberSaveable { mutableStateOf(initial.currencyCode) }
    var metric by rememberSaveable { mutableStateOf(initial.metric) }

    // Show the language actually in effect (explicit choice or the system's, if supported).
    val explicit = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore('-')
    val currentLanguage = explicit.ifEmpty { locale.language }
    val languageLabel = LANGUAGES.firstOrNull { it.first == currentLanguage }?.second
        ?: LANGUAGES.first().second

    val currencyOptions = (CURRENCIES + currency).distinct().map { code ->
        code to "$code - ${Currency.getInstance(code).getDisplayName(locale)}"
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(8.dp))

            Dropdown(
                label = stringResource(R.string.settings_language),
                selectedText = languageLabel,
                options = LANGUAGES,
                onSelect = { tag ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(tag),
                    )
                },
            )
            Dropdown(
                label = stringResource(R.string.settings_currency),
                selectedText = currencyOptions.first { it.first == currency }.second,
                options = currencyOptions,
                onSelect = { currency = it },
            )
            Dropdown(
                label = stringResource(R.string.settings_units),
                selectedText = stringResource(
                    if (metric) R.string.units_metric else R.string.units_imperial,
                ),
                options = listOf(
                    true to stringResource(R.string.units_metric),
                    false to stringResource(R.string.units_imperial),
                ),
                onSelect = { metric = it },
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    onDone(initial.copy(currencyCode = currency, metric = metric, onboarded = true))
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
    }
}
