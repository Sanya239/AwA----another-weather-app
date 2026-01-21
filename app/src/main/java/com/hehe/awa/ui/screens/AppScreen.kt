package com.hehe.awa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.hehe.awa.data.UpdateResult
import com.hehe.awa.ui.viewmodel.MainViewModel

@Composable
fun AppScreen(auth: FirebaseAuth, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var activeScreen by remember { mutableStateOf<ActiveScreen>(ActiveScreen.Main) }

    val profile by viewModel.profile.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val requestUserNames by viewModel.requestUserNames.collectAsState()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) {}

    LaunchedEffect(Unit) {
        auth.addAuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
    }

    LaunchedEffect(Unit) {
        val hasCoarseLocation =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasCoarseLocation) {
            locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
    }

    LaunchedEffect(currentUser?.uid) {
        val user = currentUser ?: return@LaunchedEffect
        activeScreen = ActiveScreen.Main
        viewModel.loadProfile(
            uid = user.uid,
            fallbackName = user.displayName ?: user.email,
        )
        viewModel.refreshData(user.uid)
    }

    LaunchedEffect(currentUser?.uid, profile) {
        val user = currentUser ?: return@LaunchedEffect
        if (profile != null) {
            viewModel.loadWeather(context, user.uid)
        }
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
                    requests = requests,
                    friends = friends,
                    requestUserNames = requestUserNames,
                    viewModel = viewModel,
                    onOpenProfile = { activeScreen = ActiveScreen.Profile },
                )

                ActiveScreen.Profile -> ProfileScreen(
                    profile = profile,
                    weather = viewModel.weather.collectAsState().value,
                    onBack = { activeScreen = ActiveScreen.Main },
                    onSaveProfile = { newProfile ->
                        currentUser?.let {
                            viewModel.saveProfile(it.uid, newProfile)
                            UpdateResult.Success
                        } ?: UpdateResult.Error("User not logged in", null)
                    },
                    onUpdateTag = { newTag ->
                        currentUser?.let { user ->
                            viewModel.updateTag(user.uid, newTag)
                            UpdateResult.Success
                        } ?: UpdateResult.Error("User not logged in", null)
                    },
                    onSignOut = {
                        auth.signOut()
                        currentUser = null
                        viewModel.clearData()
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
