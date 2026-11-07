@file:OptIn(ExperimentalMaterial3Api::class)

package com.heitor.driverprofit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.DayEntry
import com.heitor.driverprofit.data.Settings
import com.heitor.driverprofit.domain.Platform
import com.heitor.driverprofit.domain.toStats
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

private enum class Period(val labelRes: Int) {
    TODAY(R.string.period_today),
    WEEK(R.string.period_week),
    MONTH(R.string.period_month),
}

@Composable
fun DashboardScreen(entries: List<DayEntry>, settings: Settings, onAdd: () -> Unit) {
    val fmt = rememberFmt(settings.currencyCode)
    val locale = LocalConfiguration.current.locales[0]
    val dist = distanceUnit(settings.metric)
    var period by rememberSaveable { mutableStateOf(Period.TODAY) }

    val today = LocalDate.now()
    val start = when (period) {
        Period.TODAY -> today
        Period.WEEK -> today.with(
            TemporalAdjusters.previousOrSame(WeekFields.of(locale).firstDayOfWeek),
        )
        Period.MONTH -> today.withDayOfMonth(1)
    }.toEpochDay()
    val filtered = entries.filter { it.epochDay >= start }
    val stats = filtered.toStats()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.dashboard_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Period.entries.forEach { p ->
                        FilterChip(
                            selected = period == p,
                            onClick = { period = p },
                            label = { Text(stringResource(p.labelRes)) },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.dashboard_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                stringResource(R.string.label_net),
                                style = MaterialTheme.typography.labelLarge,
                            )
                            Text(
                                fmt.money(stats.net),
                                style = MaterialTheme.typography.displaySmall,
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            stringResource(R.string.label_gross),
                            fmt.money(stats.gross),
                            Modifier.weight(1f),
                        )
                        StatCard(
                            stringResource(R.string.label_expenses),
                            fmt.money(stats.expenses),
                            Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            stringResource(R.string.label_distance),
                            "${fmt.number(stats.distance)} $dist",
                            Modifier.weight(1f),
                        )
                        StatCard(
                            stringResource(R.string.label_hours),
                            "${fmt.number(stats.hours)} ${stringResource(R.string.unit_h)}",
                            Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard(
                            stringResource(R.string.label_gross_per_distance, dist),
                            fmt.money(stats.grossPerDistance),
                            Modifier.weight(1f),
                        )
                        StatCard(
                            stringResource(R.string.label_gross_per_hour),
                            fmt.money(stats.grossPerHour),
                            Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Text(
                        stringResource(R.string.dashboard_platforms),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                val byPlatform = filtered.groupBy { Platform.fromName(it.platform) }
                    .map { (platform, list) -> platform to list.toStats() }
                    .sortedByDescending { it.second.net }
                items(byPlatform) { (platform, s) ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    platform.brand ?: stringResource(R.string.platform_other),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    "${fmt.money(s.grossPerDistance)} / $dist",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Text(fmt.money(s.net), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
