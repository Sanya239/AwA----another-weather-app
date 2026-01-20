package com.hehe.awa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hehe.awa.R

@Composable
fun FriendDetailsDialog(
    name: String,
    tag: String?,
    onDismiss: () -> Unit,
    onRemove: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(name) },
        text = {
            Column {
                Text(text = stringResource(R.string.profile_name_label) + ": $name")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(R.string.profile_tag_label) + ": ${tag ?: "No tag"}")
            }
        },
        confirmButton = {
            Button(
                onClick = onRemove,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.remove_friend))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

