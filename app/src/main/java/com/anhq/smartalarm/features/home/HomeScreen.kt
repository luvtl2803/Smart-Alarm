package com.anhq.smartalarm.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anhq.smartalarm.R
import com.anhq.smartalarm.core.designsystem.theme.gradient1
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title1
import com.anhq.smartalarm.core.ui.BottomNavScreen

@Composable
fun HomeRoute() {
    HomeScreen()
}

@Composable
fun HomeScreen() {
    BottomNavScreen(
        topBar = {
            Text(
                text = "Trang chủ",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF3C3D3F), shape = RoundedCornerShape(16.dp))
            ) {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = "Don't stop when you are tired STOP when you're DONE",
                    style = MaterialTheme.typography.title1
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color(0xFF3C3D3F), shape = RoundedCornerShape(16.dp))
            ) {
                AlarmCard()
            }
        }
    }
}

@Composable
fun AlarmCard(

) {
    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
    val selectedDays = listOf(false, false, true, true, true, false, false)
    var isAlarmOn by remember { mutableStateOf(true) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
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

            Row {
                Switch(
                    checked = isAlarmOn,
                    onCheckedChange = { isAlarmOn = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFB388FF)
                    )
                )

                IconButton(
                    onClick = { }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_3dot),
                        contentDescription = null
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xB8474747))
                .padding(15.dp)
        ) {
            days.forEachIndexed { index, day ->
                Text(
                    text = day,
                    color = if (selectedDays[index]) Color.White else Color.Gray,
                    fontWeight = if (selectedDays[index]) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.wrapContentWidth()
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}


@Preview
@Composable
private fun HomePreview() {
    HomeScreen()
}