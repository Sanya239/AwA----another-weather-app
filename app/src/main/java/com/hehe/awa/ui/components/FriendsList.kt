package com.hehe.awa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hehe.awa.R
import com.hehe.awa.data.Friend

@Composable
fun FriendsList(
    friends: List<Friend>,
    onFriendClick: (Friend) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.friends),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        friends.forEach { friend ->
            FriendView(
                name = friend.name,
                weather = friend.weather,
                onClick = { onFriendClick(friend) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

