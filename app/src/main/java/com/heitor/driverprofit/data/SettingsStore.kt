package com.heitor.driverprofit.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.heitor.driverprofit.domain.CostProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "settings")

data class Settings(
    val currencyCode: String,
    val metric: Boolean,
    val profile: CostProfile,
    /** False until the user has completed the first-run language/currency/unit setup. */
    val onboarded: Boolean = true,
)

/** Sensible starting values derived from the device locale. */
fun defaultSettings(locale: Locale = Locale.getDefault()): Settings {
    val currencyCode = runCatching { Currency.getInstance(locale).currencyCode }
        .getOrDefault("USD")
    val metric = locale.country !in setOf("US", "LR", "MM")
    val profile = if (metric) {
        CostProfile(
            fuelPrice = 6.0,
            efficiency = 10.0,
            maintenancePerDistance = 0.25,
            minPerDistance = 1.8,
            minPerHour = 30.0,
        )
    } else {
        CostProfile(
            fuelPrice = 3.5,
            efficiency = 25.0,
            maintenancePerDistance = 0.15,
            minPerDistance = 1.2,
            minPerHour = 20.0,
        )
    }
    return Settings(currencyCode, metric, profile, onboarded = false)
}

class SettingsStore(private val context: Context) {

    private object Keys {
        val currency = stringPreferencesKey("currency")
        val metric = booleanPreferencesKey("metric")
        val onboarded = booleanPreferencesKey("onboarded")
        val fuelPrice = doublePreferencesKey("fuel_price")
        val efficiency = doublePreferencesKey("efficiency")
        val maintenance = doublePreferencesKey("maintenance")
        val minPerDistance = doublePreferencesKey("min_per_distance")
        val minPerHour = doublePreferencesKey("min_per_hour")
    }

    private val defaults = defaultSettings()

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            currencyCode = p[Keys.currency] ?: defaults.currencyCode,
            metric = p[Keys.metric] ?: defaults.metric,
            // Installs that predate onboarding already have a currency stored.
            onboarded = p[Keys.onboarded] ?: p.contains(Keys.currency),
            profile = CostProfile(
                fuelPrice = p[Keys.fuelPrice] ?: defaults.profile.fuelPrice,
                efficiency = p[Keys.efficiency] ?: defaults.profile.efficiency,
                maintenancePerDistance = p[Keys.maintenance] ?: defaults.profile.maintenancePerDistance,
                minPerDistance = p[Keys.minPerDistance] ?: defaults.profile.minPerDistance,
                minPerHour = p[Keys.minPerHour] ?: defaults.profile.minPerHour,
            ),
        )
    }

    suspend fun save(s: Settings) {
        context.dataStore.edit { p ->
            p[Keys.currency] = s.currencyCode
            p[Keys.metric] = s.metric
            p[Keys.onboarded] = s.onboarded
            p[Keys.fuelPrice] = s.profile.fuelPrice
            p[Keys.efficiency] = s.profile.efficiency
            p[Keys.maintenance] = s.profile.maintenancePerDistance
            p[Keys.minPerDistance] = s.profile.minPerDistance
            p[Keys.minPerHour] = s.profile.minPerHour
        }
    }
}
