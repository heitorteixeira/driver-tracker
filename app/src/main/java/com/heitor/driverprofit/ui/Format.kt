package com.heitor.driverprofit.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.heitor.driverprofit.R
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

/** Locale-aware formatting for money, numbers and dates. */
class Fmt(private val locale: Locale, currencyCode: String) {
    private val currency = NumberFormat.getCurrencyInstance(locale).also {
        it.currency = Currency.getInstance(currencyCode)
    }
    private val number = NumberFormat.getNumberInstance(locale).also {
        it.maximumFractionDigits = 2
        it.minimumFractionDigits = 0
    }
    private val dateFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(locale)

    fun money(value: Double): String = currency.format(value)
    fun number(value: Double): String = number.format(value)
    fun percent(fraction: Double): String = number.format(fraction * 100) + "%"
    fun date(epochDay: Long): String = dateFormatter.format(LocalDate.ofEpochDay(epochDay))
}

@Composable
fun rememberFmt(currencyCode: String): Fmt {
    val locale = LocalConfiguration.current.locales[0]
    return remember(locale, currencyCode) { Fmt(locale, currencyCode) }
}

/** Parses user input accepting both "12.5" and "12,5". */
fun String.toDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Plain text for editing a number in a text field (no grouping, dot decimal). */
fun Double.toEditText(): String =
    if (this == this.toLong().toDouble()) this.toLong().toString() else this.toString()

@Composable
fun distanceUnit(metric: Boolean): String =
    stringResource(if (metric) R.string.unit_km else R.string.unit_mi)

@Composable
fun volumeUnit(metric: Boolean): String =
    stringResource(if (metric) R.string.unit_l else R.string.unit_gal)
