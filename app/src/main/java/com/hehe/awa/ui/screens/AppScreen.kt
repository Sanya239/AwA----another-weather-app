package com.hehe.awa.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import com.hehe.awa.data.Friend
import com.hehe.awa.data.FriendRequest
import com.hehe.awa.data.FriendRequestRepository
import com.hehe.awa.data.FriendsRepository
import com.hehe.awa.data.UpdateResult
import com.hehe.awa.data.UserProfile
import com.hehe.awa.data.UserProfileRepository

@Composable
fun AppScreen(auth: FirebaseAuth) {
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var activeScreen by remember { mutableStateOf<ActiveScreen>(ActiveScreen.Main) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    val repo = remember { UserProfileRepository() }
    val friendRequestRepo = remember { FriendRequestRepository() }
    val friendsRepo = remember { FriendsRepository() }

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
    }

    LaunchedEffect(currentUser?.uid) {
        val user = currentUser ?: return@LaunchedEffect
        activeScreen = ActiveScreen.Main
        profile = repo.getOrCreate(
            uid = user.uid,
            fallbackName = user.displayName ?: user.email,
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (currentUser != null) {
            when (activeScreen) {
                ActiveScreen.Main -> LoggedInScreen(
                    user = currentUser!!,
                    profile = profile,
                    onOpenProfile = { activeScreen = ActiveScreen.Profile },
                    onLoadRequests = {
                        friendRequestRepo.getPendingRequests(currentUser!!.uid)
                    },
                    onLoadFriends = {
                        friendsRepo.getFriends(currentUser!!.uid)
                    },
                    onCreateRequest = { toUid ->
                        friendRequestRepo.createRequest(currentUser!!.uid, toUid)
                    },
                    onAcceptRequest = { requestId ->
                        friendRequestRepo.acceptRequest(requestId)
                    },
                    onRejectRequest = { requestId ->
                        friendRequestRepo.rejectRequest(requestId)
                    },
                    onGetUserName = { uid ->
                        repo.getUserName(uid)
                    },
                )

                ActiveScreen.Profile -> ProfileScreen(
                    profile = profile,
                    onBack = { activeScreen = ActiveScreen.Main },
                    onSaveProfile = { newProfile ->
                        var result: UpdateResult = UpdateResult.Success
                        currentUser?.let {
                            result = repo.save(it.uid, newProfile)
                        } ?: throw IllegalStateException("User not logged in")
                        if (result is UpdateResult.Success) {
                            profile = newProfile
                        }
                        result
                    },
                    onUpdateTag = { newTag ->
                        currentUser?.let { user ->
                            repo.updateTag(user.uid, newTag)
                        } ?: throw IllegalStateException("User not logged in")
                    },
                    onSignOut = {
                        auth.signOut()
                        currentUser = null
                        profile = null
                        activeScreen = ActiveScreen.Main
                    },
                )
            }
        } else {
            SignInScreen(
                onSignInSuccess = { user ->
                    currentUser = user
                }
            )
        }
    }
}

private enum class ActiveScreen {
    Main,
    Profile,
}
