package com.fauxx.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Dialog shown on app launch after a crash. Offers to share the crash report
 * via the system share sheet or dismiss it.
 */
@Composable
fun CrashReportDialog(
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Fauxx 已崩溃",
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                "Fauxx 在上次会话中崩溃。您可以分享崩溃报告以帮助诊断问题。报告中的个人数据已被清除。"
            )
        },
        confirmButton = {
            TextButton(onClick = onShare) {
                Text("分享报告")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("忽略")
            }
        }
    )
}

