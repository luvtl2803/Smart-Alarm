package com.anhq.smartalarm.features.statistics

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.ui.BottomNavScreen
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import java.util.*

@Composable
fun StatisticsRoute() {
    StatisticsScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    BottomNavScreen(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_statistics)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Thời gian ngủ trung bình theo tuần
            StatCard(
                title = "Thời gian ngủ trung bình",
                content = { SleepTimeLineChart() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phân bố thời gian thức dậy
            StatCard(
                title = "Phân bố thời gian thức dậy",
                content = { WakeUpDistributionPieChart() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Số lần báo thức trong tuần
            StatCard(
                title = "Số lần báo thức trong tuần",
                content = { AlarmFrequencyBarChart() }
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
fun SleepTimeLineChart() {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = onSurfaceColor
                    valueFormatter = IndexAxisValueFormatter(arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"))
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = onSurfaceColor
                }
                
                axisRight.isEnabled = false
                legend.textColor = onSurfaceColor
                
                setBackgroundColor(surfaceColor)
            }
        },
        update = { chart ->
            val entries = listOf(
                Entry(0f, 7.5f),
                Entry(1f, 6.8f),
                Entry(2f, 7.2f),
                Entry(3f, 6.5f),
                Entry(4f, 7.8f),
                Entry(5f, 8.2f),
                Entry(6f, 8.5f)
            )

            val dataSet = LineDataSet(entries, "Giờ ngủ").apply {
                color = primaryColor
                lineWidth = 2f
                setDrawCircles(true)
                setCircleColor(primaryColor)
                circleRadius = 4f
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun WakeUpDistributionPieChart() {
    val context = LocalContext.current
    var colors = listOf(
        MaterialTheme.colorScheme.primary.toArgb(),
        MaterialTheme.colorScheme.secondary.toArgb(),
        MaterialTheme.colorScheme.tertiary.toArgb()
    )
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                setDrawEntryLabels(false)
                
                legend.apply {
                    textColor = onSurfaceColor
                    verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.VERTICAL
                    setDrawInside(false)
                }
                
                setBackgroundColor(surfaceColor)
            }
        },
        update = { chart ->
            val entries = listOf(
                PieEntry(60f, "Đúng giờ"),
                PieEntry(30f, "Trễ < 10p"),
                PieEntry(10f, "Trễ > 10p")
            )

            val dataSet = PieDataSet(entries, "").apply {
                valueTextSize = 12f
                valueTextColor = Color.WHITE
                valueFormatter = PercentFormatter(chart)
            }

            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
}

@Composable
fun AlarmFrequencyBarChart() {
    val context = LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(false)
                setDrawGridBackground(false)
                
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = onSurfaceColor
                    valueFormatter = IndexAxisValueFormatter(arrayOf("T2", "T3", "T4", "T5", "T6", "T7", "CN"))
                }
                
                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = onSurfaceColor
                    axisMinimum = 0f
                }
                
                axisRight.isEnabled = false
                legend.textColor = onSurfaceColor
                
                setBackgroundColor(surfaceColor)
            }
        },
        update = { chart ->
            val entries = listOf(
                BarEntry(0f, 2f),
                BarEntry(1f, 3f),
                BarEntry(2f, 2f),
                BarEntry(3f, 4f),
                BarEntry(4f, 3f),
                BarEntry(5f, 2f),
                BarEntry(6f, 1f)
            )

            val dataSet = BarDataSet(entries, "Số lần báo thức").apply {
                color = primaryColor
                valueTextColor = onSurfaceColor
                valueTextSize = 10f
            }

            chart.data = BarData(dataSet)
            chart.invalidate()
        }
    )
}