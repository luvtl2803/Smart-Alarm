package com.anhq.smartalarm.features.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.core.designsystem.theme.gradient1
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.model.AlarmCount
import com.anhq.smartalarm.core.model.DayOfWeek
import com.anhq.smartalarm.core.ui.JetpackComposeBasicLineChart

@Composable
fun StatisticsRoute() {
    StatisticsScreen()
}

@Composable
fun StatisticsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .background(color = Color(0xFF3C3D3F), shape = RoundedCornerShape(size = 16.dp)),
            content = {
                AlarmChart()
            }
        )
    }
}

@Composable
fun AlarmChart() {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Scheduled Alarm",
                style = MaterialTheme.typography.gradient1
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "5:30 AM",
                style = MaterialTheme.typography.label1
            )
        }
        val alarmStats = listOf(
            AlarmCount(DayOfWeek.SUN, 3),
            AlarmCount(DayOfWeek.MON, 5),
            AlarmCount(DayOfWeek.TUE, 2),
            AlarmCount(DayOfWeek.WED, 6),
            AlarmCount(DayOfWeek.THU, 1),
            AlarmCount(DayOfWeek.FRI, 4),
            AlarmCount(DayOfWeek.SAT, 3),
        )
        JetpackComposeBasicLineChart(alarmStats)
    }
}

@Preview
@Composable
private fun StatisticsPreview() {
    StatisticsScreen()
}