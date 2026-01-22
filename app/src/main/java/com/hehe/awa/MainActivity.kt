package com.hehe.awa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.messaging.FirebaseMessaging
import com.hehe.awa.notifications.NotificationChannels
import com.hehe.awa.ui.screens.AppScreen
import com.hehe.awa.ui.theme.AwaTheme
import com.hehe.awa.work.WeatherWorkManager

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        enableEdgeToEdge()
        
        // Создаем каналы уведомлений
        NotificationChannels.createChannels(this)
        
        // Получаем FCM токен для отладки
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }
            val token = task.result
            android.util.Log.d("FCM", "FCM Registration Token: $token")
        }
        
        WeatherWorkManager.scheduleWeatherUpdate(this)
        
        setContent {
            AwaTheme {
                AppScreen(auth)
            }
        }
    }
}


