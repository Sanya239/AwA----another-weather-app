package com.hehe.awa.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.hehe.awa.R
import com.hehe.awa.data.UpdateResult
import com.hehe.awa.ui.viewmodel.MainViewModel

@Composable
fun AppScreen(auth: FirebaseAuth, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val userNotLoggedInMessage = stringResource(R.string.user_not_logged_in)
    var currentUser by remember { mutableStateOf(auth.currentUser) }

    val profile by viewModel.profile.collectAsState()
    val requests by viewModel.requests.collectAsState()
    val friends by viewModel.friends.collectAsState()
    val requestUserNames by viewModel.requestUserNames.collectAsState()

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) {}

    val notificationPermissionLauncher =
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

        // Запрашиваем разрешение на уведомления для Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        val user = currentUser ?: return@LaunchedEffect
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

    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = (if (auth.currentUser == null) Route.Auth.route else Route.Home.route)
        ) {
            composable(Route.Home.route) {
                LoggedInScreen(
                    user = currentUser!!,
                    profile = profile,
                    requests = requests,
                    friends = friends,
                    requestUserNames = requestUserNames,
                    viewModel = viewModel,
                    onOpenProfile = { navController.navigate(Route.Profile.route) },
                )
            }
            composable(Route.Profile.route) {
                ProfileScreen(
                    profile = profile,
                    weather = viewModel.weather.collectAsState().value,
                    onBack = { navController.navigate(Route.Home.route) },
                    onSaveProfile = { newProfile ->
                        currentUser?.let {
                            viewModel.saveProfile(it.uid, newProfile)
                            UpdateResult.Success
                        } ?: UpdateResult.Error(userNotLoggedInMessage, null)
                    },
                    onUpdateTag = { newTag ->
                        currentUser?.let { user ->
                            viewModel.updateTag(user.uid, newTag)
                            UpdateResult.Success
                        } ?: UpdateResult.Error(userNotLoggedInMessage, null)
                    },
                    onSignOut = {
                        auth.signOut()
                        currentUser = null
                        viewModel.clearData()
                        navController.navigate(Route.Auth.route)
                    },
                )
            }
            composable(Route.Auth.route) {
                SignInScreen(
                    onSignInSuccess = { user ->
                        currentUser = user
                        navController.navigate(Route.Home.route)
                    }
                )
            }
        }
    }
}
