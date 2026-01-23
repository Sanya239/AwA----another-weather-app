package com.hehe.awa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hehe.awa.R
import com.hehe.awa.data.Friend

@Composable
fun FriendDetailsDialog(
    friend: Friend,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(friend.name + "(@" + friend.tag+")" ) },
        text = {
            Column {
                WeatherView(friend.weather)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.close))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { showConfirmDialog = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            ) {
                Text(stringResource(R.string.remove_friend))
            }
        }
    )

    if (showConfirmDialog) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_remove_friend_title),
            message = stringResource(R.string.confirm_remove_friend_message),
            confirmText = stringResource(R.string.remove_friend),
            onDismiss = { showConfirmDialog = false },
            onConfirm = {
                showConfirmDialog = false
                onRemove()
            },
            isDestructive = true
        )
    }
}

