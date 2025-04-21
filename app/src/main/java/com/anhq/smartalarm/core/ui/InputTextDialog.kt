package com.anhq.smartalarm.core.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.anhq.smartalarm.core.designsystem.theme.body5
import com.anhq.smartalarm.core.designsystem.theme.label1
import com.anhq.smartalarm.core.designsystem.theme.label3

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
                    text = "OK",
                    style = MaterialTheme.typography.label3
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.label3
                )
            }
        },
        title = {
            Text(
                text = "Edit label",
                style = MaterialTheme.typography.label1
            )
        },
        text = {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.body5,
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