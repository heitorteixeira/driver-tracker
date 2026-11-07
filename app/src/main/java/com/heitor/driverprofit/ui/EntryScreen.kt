@file:OptIn(ExperimentalMaterial3Api::class)

package com.heitor.driverprofit.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.DayEntry
import com.heitor.driverprofit.data.Settings
import com.heitor.driverprofit.domain.Platform
import java.time.LocalDate

private const val MILLIS_PER_DAY = 86_400_000L

@Composable
fun EntryScreen(
    id: Long,
    settings: Settings,
    vm: AppViewModel,
    onDone: () -> Unit,
) {
    val fmt = rememberFmt(settings.currencyCode)
    val dist = distanceUnit(settings.metric)

    var epochDay by remember { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var platform by remember { mutableStateOf(Platform.UBER) }
    var gross by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("") }
    var other by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        if (id != 0L) {
            vm.getEntry(id)?.let { e ->
                epochDay = e.epochDay
                platform = Platform.fromName(e.platform)
                gross = e.gross.toEditText()
                distance = e.distance.toEditText()
                hours = e.hours.toEditText()
                fuel = e.fuelExpense.toEditText()
                other = e.otherExpense.toEditText()
            }
        }
    }

    val grossValue = gross.toDecimal()
    val canSave = grossValue != null && grossValue >= 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (id == 0L) R.string.entry_title_new else R.string.entry_title_edit,
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
                actions = {
                    if (id != 0L) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.action_delete),
                            )
                        }
                    }
                },
            )
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
            Box(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fmt.date(epochDay),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.field_date)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true },
                )
            }
            Dropdown(
                label = stringResource(R.string.field_platform),
                selectedText = platform.brand ?: stringResource(R.string.platform_other),
                options = Platform.entries.map {
                    it to (it.brand ?: stringResource(R.string.platform_other))
                },
                onSelect = { platform = it },
            )
            NumberField(stringResource(R.string.field_gross), gross, { gross = it })
            NumberField(stringResource(R.string.field_distance, dist), distance, { distance = it })
            NumberField(stringResource(R.string.field_hours), hours, { hours = it })
            NumberField(stringResource(R.string.field_fuel), fuel, { fuel = it })
            NumberField(stringResource(R.string.field_other), other, { other = it })

            Button(
                enabled = canSave,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    vm.saveEntry(
                        DayEntry(
                            id = id,
                            epochDay = epochDay,
                            platform = platform.name,
                            gross = grossValue ?: 0.0,
                            distance = distance.toDecimal() ?: 0.0,
                            hours = hours.toDecimal() ?: 0.0,
                            fuelExpense = fuel.toDecimal() ?: 0.0,
                            otherExpense = other.toDecimal() ?: 0.0,
                        ),
                    )
                    onDone()
                },
            ) {
                Text(stringResource(R.string.action_save))
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = epochDay * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { epochDay = Math.floorDiv(it, MILLIS_PER_DAY) }
                        showDatePicker = false
                    },
                ) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteEntry(id)
                        showDeleteConfirm = false
                        onDone()
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
