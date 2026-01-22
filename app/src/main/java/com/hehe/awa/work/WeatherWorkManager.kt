package com.hehe.awa.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WeatherWorkManager {
    private const val WORK_NAME = "weather_update_work"

    fun scheduleWeatherUpdate(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            60, TimeUnit.MINUTES
        )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    fun cancelWeatherUpdate(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}


