package com.anhq.smartalarm.features.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anhq.smartalarm.core.data.repository.EnhancedSleepData
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.headline3
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.title2
import com.anhq.smartalarm.core.designsystem.theme.title3
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun StatisticsRoute(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    StatisticsScreen(
        uiState = uiState,
        onRequestPermission = {
            context.startActivity(viewModel.getPermissionIntent())
        },
        formatDuration = viewModel::formatDuration,
        formatDate = viewModel::formatDate,
        formatDayOfWeek = viewModel::formatDayOfWeek,
        formatTime = viewModel::formatTime,
        formatActionInfo = viewModel::formatActionInfo
    )
}

@Composable
fun StatisticsScreen(
    uiState: StatisticsUiState,
    onRequestPermission: () -> Unit,
    formatDuration: (Long) -> String,
    formatDate: (EnhancedSleepData) -> String,
    formatDayOfWeek: (EnhancedSleepData) -> String,
    formatTime: (EnhancedSleepData) -> String,
    formatActionInfo: (EnhancedSleepData) -> String
) {
    Scaffold(
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        topBar = {
            Text(
                text = "Thống kê giấc ngủ",
                style = headline3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                !uiState.hasPermission -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Cần quyền truy cập dữ liệu giấc ngủ",
                            style = title2,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ElevatedButton(onClick = onRequestPermission) {
                            Text(
                                text = "Cấp quyền",
                                style = label1
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Đã xảy ra lỗi",
                            style = title2,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error,
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                uiState.sleepData.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Không có dữ liệu giấc ngủ",
                            style = title2,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Hãy kết nối với ứng dụng theo dõi giấc ngủ",
                            style = body2,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Thống kê tổng quan
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "Tổng quan",
                                        style = title3
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    val avgDuration = uiState.sleepData
                                        .map { it.durationMinutes }
                                        .average()
                                        .toLong()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = formatDuration(avgDuration),
                                                style = title2,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Trung bình",
                                                style = body2,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${uiState.sleepData.size} ngày",
                                                style = title2,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Đã ghi nhận",
                                                style = body2,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Biểu đồ thời gian ngủ
                        item {
                            StatChart(
                                title = "Thời gian ngủ 7 ngày gần đây",
                                content = {
                                    val entries = uiState.sleepData
                                        .takeLast(7)
                                        .mapIndexed { index, data ->
                                            FloatEntry(
                                                x = index.toFloat(),
                                                y = data.durationMinutes.toFloat() / 60 // Chuyển đổi phút sang giờ
                                            )
                                        }

                                    Chart(
                                        chart = lineChart(),
                                        model = entryModelOf(entries),
                                        startAxis = rememberStartAxis(
                                            title = "Giờ",
                                            tickLength = 0.dp
                                        ),
                                        bottomAxis = rememberBottomAxis(
                                            title = "Ngày",
                                            tickLength = 0.dp,
                                            valueFormatter = { value, _ ->
                                                uiState.sleepData
                                                    .takeLast(7)
                                                    .getOrNull(value.toInt())
                                                    ?.let { formatDate(it) }
                                                    ?: ""
                                            }
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                    )
                                }
                            )
                        }

                        // Chi tiết từng ngày
                        items(uiState.sleepData.reversed()) { data ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 2.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column (
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = formatDate(data),
                                                style = title3
                                            )
                                            Text(
                                                text = formatDayOfWeek(data),
                                                style = body2,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = formatDuration(data.durationMinutes),
                                            style = title3,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = formatTime(data),
                                        style = body2,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Thêm thông tin về báo thức và hành động
                                    Text(
                                        text = formatActionInfo(data),
                                        style = body2,
                                        color = if (data.snoozeCount > 0)
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatChart(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .height(300.dp),
        ) {
        Text(
            text = title, style = label1,
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