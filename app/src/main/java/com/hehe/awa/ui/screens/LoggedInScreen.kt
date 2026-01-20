package com.hehe.awa.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.hehe.awa.R
import com.hehe.awa.data.UserProfile

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoggedInScreen(
    user: FirebaseUser,
    profile: UserProfile?,
    onOpenProfile: () -> Unit,
) {
    val name = profile?.name?.takeIf { it.isNotBlank() } ?: (user.displayName ?: user.email ?: "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    TextButton(onClick = onOpenProfile) {
                        Text(stringResource(R.string.profile_title))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.logged_as_format, name),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}