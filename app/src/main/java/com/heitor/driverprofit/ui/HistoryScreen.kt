@file:OptIn(ExperimentalMaterial3Api::class)

package com.heitor.driverprofit.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.DayEntry
import com.heitor.driverprofit.data.Settings
import com.heitor.driverprofit.domain.Platform

@Composable
fun HistoryScreen(
    entries: List<DayEntry>,
    settings: Settings,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    val fmt = rememberFmt(settings.currencyCode)
    val context = LocalContext.current
    val dist = distanceUnit(settings.metric)
    val chooserTitle = stringResource(R.string.export_chooser)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_history)) },
                actions = {
                    if (entries.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                CsvExporter.share(context, entries, settings.metric, chooserTitle)
                            },
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = stringResource(R.string.history_export),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_entry))
            }
        },
    ) { padding ->
        if (entries.isEmpty()) {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text(stringResource(R.string.history_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(entries, key = { it.id }) { e ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(e.id) },
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    fmt.date(e.epochDay),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    Platform.fromName(e.platform).brand
                                        ?: stringResource(R.string.platform_other),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    stringResource(
                                        R.string.history_item_detail,
                                        fmt.number(e.distance),
                                        dist,
                                        fmt.number(e.hours),
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                Text(
                                    fmt.money(e.net),
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    fmt.money(e.gross),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
