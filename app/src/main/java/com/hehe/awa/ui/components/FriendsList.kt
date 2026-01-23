package com.hehe.awa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hehe.awa.data.Friend

@Composable
fun FriendsList(
    friends: List<Friend>,
    onFriendClick: (Friend) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        friends.forEachIndexed { index, friend ->
            FriendView(
                name = friend.name,
                weather = friend.weather,
                onClick = { onFriendClick(friend) },
                modifier = Modifier.fillMaxWidth()
            )
            if (index < friends.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

