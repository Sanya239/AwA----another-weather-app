package com.hehe.awa.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.hehe.awa.R
import com.hehe.awa.data.Friend
import com.hehe.awa.data.FriendRequest
import com.hehe.awa.data.UserProfile
import com.hehe.awa.ui.components.AddFriendDialog
import com.hehe.awa.ui.components.FriendDetailsDialog
import com.hehe.awa.ui.components.FriendsList
import com.hehe.awa.ui.components.IncomingRequestItem
import com.hehe.awa.ui.components.OutgoingRequestItem
import com.hehe.awa.ui.components.SectionCard
import com.hehe.awa.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoggedInScreen(
    user: FirebaseUser,
    profile: UserProfile?,
    requests: List<FriendRequest>,
    friends: List<Friend>,
    requestUserNames: Map<String, String>,
    viewModel: MainViewModel,
    onOpenProfile: () -> Unit,
) {
    val name = profile?.name?.takeIf { it.isNotBlank() } ?: (user.displayName ?: user.email ?: "")

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFriend by remember { mutableStateOf<Friend?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tagNotFoundMessage = stringResource(R.string.tag_not_found)
    val cannotAddYourselfMessage = stringResource(R.string.cannot_add_yourself)

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
        bottomBar = {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(
                        WindowInsets.navigationBars
                    )
                    .padding(16.dp)
                    .height(56.dp),
                ) {
                Text(stringResource(R.string.add_friend))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshData(user.uid) },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    item {
                        SectionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.logged_as_format, name),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    item {
                        SectionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.friend_requests),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )

                            val outgoingRequests = requests.filter { it.fromUid == user.uid }
                            val incomingRequests = requests.filter { it.toUid == user.uid }
                            val orderedRequests = outgoingRequests + incomingRequests

                            if (orderedRequests.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_friend_requests),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            } else {
                                orderedRequests.forEachIndexed { index, request ->
                                    if (request.fromUid == user.uid) {
                                        val userName = requestUserNames[request.toUid] ?: request.toUid
                                        OutgoingRequestItem(
                                            userName = userName,
                                            onReject = { viewModel.rejectRequest(user.uid, request.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    } else {
                                        val userName = requestUserNames[request.fromUid] ?: request.fromUid
                                        IncomingRequestItem(
                                            userName = userName,
                                            onAccept = { viewModel.acceptRequest(user.uid, request.id) },
                                            onReject = { viewModel.rejectRequest(user.uid, request.id) },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }

                                    if (index < orderedRequests.size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.friends),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                            )

                            if (friends.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.no_friends_message),
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            } else {
                                FriendsList(
                                    friends = friends,
                                    onFriendClick = { friend -> selectedFriend = friend },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFriendDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { tag ->
                scope.launch {
                    val friendRequestRepo = com.hehe.awa.data.FriendRequestRepository()
                    val toUid = friendRequestRepo.findUserByTag(tag)

                    if (toUid == null) {
                        snackbarHostState.showSnackbar(tagNotFoundMessage)
                    } else if (toUid == user.uid) {
                        snackbarHostState.showSnackbar(cannotAddYourselfMessage)
                    } else {
                        viewModel.createRequest(user.uid, toUid)
                        showAddDialog = false
                    }
                }
            }
        )
    }

    selectedFriend?.let { friend ->
        FriendDetailsDialog(
            friend,
            onDismiss = { selectedFriend = null },
            onRemove = {
                viewModel.removeFriend(user.uid, friend.uid)
                selectedFriend = null
            }
        )
    }
}