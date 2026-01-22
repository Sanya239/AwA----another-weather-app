package com.hehe.awa.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.hehe.awa.data.UserProfileRepository
import com.hehe.awa.data.UserWeatherRepository
import com.hehe.awa.data.WeatherRepository
import com.hehe.awa.data.getCurrentLocation

class WeatherUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser ?: return Result.success()

            val userProfileRepository = UserProfileRepository()
            val weatherRepository = WeatherRepository()
            val userWeatherRepository = UserWeatherRepository()

            val profile = userProfileRepository.getOrCreate(
                uid = currentUser.uid,
                fallbackName = currentUser.displayName ?: currentUser.email
            )

            if (profile.isPrivate) {
                return Result.success()
            }

            val userLocation = getCurrentLocation(applicationContext) ?: return Result.retry()

            val weather = weatherRepository.getCurrentWeather(
                userLocation.latitude,
                userLocation.longitude
            )

            if (weather != null) {
                userWeatherRepository.saveUserWeather(currentUser.uid, weather)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}


