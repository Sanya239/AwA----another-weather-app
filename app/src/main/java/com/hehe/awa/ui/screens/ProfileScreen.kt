package com.hehe.awa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hehe.awa.R
import com.hehe.awa.data.UpdateResult
import com.hehe.awa.data.UserProfile
import com.hehe.awa.data.Weather
import com.hehe.awa.ui.components.PrivacyPolicyDialog
import com.hehe.awa.ui.components.SectionCard
import com.hehe.awa.ui.components.WeatherView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    weather: Weather?,
    onBack: () -> Unit,
    onSaveProfile: suspend (UserProfile) -> UpdateResult,
    onUpdateTag: suspend (String) -> UpdateResult,
    onSignOut: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var tag by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    var isInitialized by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val tagTakenMessage = stringResource(R.string.tag_already_taken)
    val tagUpdateErrorMessage = stringResource(R.string.tag_update_error)
    val allGoodMessage = stringResource(R.string.all_good)

    LaunchedEffect(profile) {
        if (profile != null && !isInitialized) {
            name = profile.name
            tag = profile.tag ?: ""
            isPrivate = profile.isPrivate
            isInitialized = true
        }
    }

    val hasChanges = remember(name, isPrivate, tag, profile) {
        isInitialized && profile != null && (
                name != profile.name ||
                        isPrivate != profile.isPrivate ||
                        tag != (profile.tag ?: "")
                )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.back_to_menu))
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isInitialized) {
                                val currentTag = tag.trim()
                                val oldTag = profile?.tag

                                scope.launch {
                                    if (currentTag != oldTag) {
                                        val result = onUpdateTag(currentTag)
                                        if (result is UpdateResult.Error) {
                                            val errorMessage = when {
                                                result.message?.contains(
                                                    "Tag already taken",
                                                    ignoreCase = true
                                                ) == true -> {
                                                    tagTakenMessage
                                                }

                                                else -> tagUpdateErrorMessage
                                            }
                                            snackbarHostState.showSnackbar(errorMessage)
                                            return@launch
                                        }
                                    }
                                    onSaveProfile(
                                        UserProfile(
                                            name = name,
                                            isPrivate = isPrivate,
                                            tag = if (currentTag.isNotEmpty()) currentTag else null
                                        )
                                    )
                                    snackbarHostState.showSnackbar(allGoodMessage)
                                }
                            }
                        },
                        enabled = hasChanges,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.save),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            SectionCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        if (newValue.length <= 60) {
                            name = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.profile_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(stringResource(R.string.name_length_format, name.length))
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tag,
                    onValueChange = { tag = it },
                    label = { Text(stringResource(R.string.profile_tag_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Switch(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.profile_private_label),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )

                }
                

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(stringResource(R.string.sign_out))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(modifier = Modifier.fillMaxWidth()) {
                WeatherView(weather = weather)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showPrivacyDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(stringResource(R.string.privacy_policy_button))
            }
        }
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}




