package com.hehe.awa.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_friend)
                        )
                    }
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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshData(user.uid) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    Text(
                        text = stringResource(R.string.logged_as_format, name),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (requests.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.friend_requests),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    val outgoingRequests = requests.filter { it.fromUid == user.uid }
                    val incomingRequests = requests.filter { it.toUid == user.uid }

                    items(outgoingRequests) { request ->
                        val userName = requestUserNames[request.toUid] ?: request.toUid
                        OutgoingRequestItem(
                            userName = userName,
                            onReject = {
                                viewModel.rejectRequest(user.uid, request.id)
                            }
                        )
                    }

                    items(incomingRequests) { request ->
                        val userName = requestUserNames[request.fromUid] ?: request.fromUid
                        IncomingRequestItem(
                            userName = userName,
                            onAccept = {
                                viewModel.acceptRequest(user.uid, request.id)
                            },
                            onReject = {
                                viewModel.rejectRequest(user.uid, request.id)
                            }
                        )
                    }
                    if(isRefreshing){
                        item { Text("isRefreshing") }
                    }
                }

                item {
                    FriendsList(
                        friends = friends,
                        onFriendClick = { friend ->
                            selectedFriend = friend
                        },
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
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
                        snackbarHostState.showSnackbar("Cannot add yourself")
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
            name = friend.name,
            tag = friend.tag,
            onDismiss = { selectedFriend = null },
            onRemove = {
                viewModel.removeFriend(user.uid, friend.uid)
                selectedFriend = null
            }
        )
    }
}