package com.hehe.awa.ui.screens

sealed class Route(val route: String) {
    object Home : Route("home")
    object Profile : Route("profile")
    object Auth: Route("auth")
}
