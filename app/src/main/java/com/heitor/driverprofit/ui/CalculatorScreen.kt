@file:OptIn(ExperimentalMaterial3Api::class)

package com.heitor.driverprofit.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.heitor.driverprofit.R
import com.heitor.driverprofit.data.Settings
import com.heitor.driverprofit.domain.RideCalculator
import com.heitor.driverprofit.domain.RideOffer
import com.heitor.driverprofit.domain.Verdict

@Composable
fun CalculatorScreen(settings: Settings) {
    val fmt = rememberFmt(settings.currencyCode)
    val dist = distanceUnit(settings.metric)

    var fare by rememberSaveable { mutableStateOf("") }
    var distance by rememberSaveable { mutableStateOf("") }
    var pickup by rememberSaveable { mutableStateOf("") }
    var minutes by rememberSaveable { mutableStateOf("") }

    val result = RideCalculator.evaluate(
        RideOffer(
            fare = fare.toDecimal() ?: 0.0,
            distance = distance.toDecimal() ?: 0.0,
            pickupDistance = pickup.toDecimal() ?: 0.0,
            minutes = minutes.toDecimal() ?: 0.0,
        ),
        settings.profile,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(title = { Text(stringResource(R.string.calc_title)) })
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
            NumberField(stringResource(R.string.calc_fare), fare, { fare = it })
            NumberField(stringResource(R.string.calc_distance, dist), distance, { distance = it })
            NumberField(stringResource(R.string.calc_pickup, dist), pickup, { pickup = it })
            NumberField(stringResource(R.string.calc_minutes), minutes, { minutes = it })

            if (result == null) {
                Text(
                    stringResource(R.string.calc_hint),
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                val (color, labelRes) = when (result.verdict) {
                    Verdict.GREEN -> VerdictGreen to R.string.verdict_green
                    Verdict.YELLOW -> VerdictYellow to R.string.verdict_yellow
                    Verdict.RED -> VerdictRed to R.string.verdict_red
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = color,
                        contentColor = Color.White,
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            stringResource(labelRes),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            "${stringResource(R.string.calc_net)}: ${fmt.money(result.net)}",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        stringResource(R.string.calc_per_distance, dist),
                        fmt.money(result.perDistance),
                        Modifier.weight(1f),
                    )
                    StatCard(
                        stringResource(R.string.calc_per_hour),
                        fmt.money(result.perHour),
                        Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        stringResource(R.string.calc_cost),
                        fmt.money(result.cost),
                        Modifier.weight(1f),
                    )
                    StatCard(
                        stringResource(R.string.calc_margin),
                        fmt.percent(result.margin),
                        Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
