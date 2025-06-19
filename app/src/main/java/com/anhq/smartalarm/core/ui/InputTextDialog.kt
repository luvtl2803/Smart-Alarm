package com.anhq.smartalarm.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.anhq.smartalarm.core.designsystem.theme.body2
import com.anhq.smartalarm.core.designsystem.theme.body4
import com.anhq.smartalarm.core.designsystem.theme.label1

@Composable
fun InputTextDialog(
    name: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var label by remember { mutableStateOf(name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(label)
            }) {
                Text(
                    text = "Lưu lại",
                    style = body4
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Hủy",
                    style = body4
                )
            }
        },
        title = {
            Text(
                text = "Đặt tên báo thức",
                style = label1
            )
        },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                singleLine = true,
                textStyle = body2,
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun InputTextDialogPreview() {
    InputTextDialog(
        name = "My Alarm",
        onDismiss = {},
        onConfirm = {}
    )
}