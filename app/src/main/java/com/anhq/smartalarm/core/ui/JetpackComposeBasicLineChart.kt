package com.anhq.smartalarm.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.anhq.smartalarm.core.model.AlarmCount
import com.anhq.smartalarm.core.model.DayOfWeek
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@Composable
fun JetpackComposeBasicLineChart(
    alarmStats: List<AlarmCount>,
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    val sortedStats = DayOfWeek.entries.map { day ->
        alarmStats.find { it.dayOfWeek == day } ?: AlarmCount(day, 0)
    }

    val dayLabels = sortedStats.map { it.dayOfWeek.label }

    val alarmCounts = sortedStats.map { it.alarmCount }

    LaunchedEffect(alarmStats) {
        modelProducer.runTransaction {
            columnSeries {
                series(alarmCounts)
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(
                valueFormatter = { _, value, _ ->
                    dayLabels.getOrNull(value.toInt()) ?: ""
                }
            ),
        ),
        modelProducer = modelProducer
    )
}
