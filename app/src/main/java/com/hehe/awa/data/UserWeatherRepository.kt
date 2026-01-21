package com.hehe.awa.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserWeatherRepository {
    private val db = FirebaseFirestore.getInstance()
    private val userWeatherCollection = db.collection("user_weather")

    suspend fun saveUserWeather(uid: String, weather: Weather) {
        val data = mapOf(
            "uid" to uid,
            "location" to mapOf(
                "name" to weather.location.name,
                "region" to weather.location.region,
                "country" to weather.location.country,
                "lat" to weather.location.lat,
                "lon" to weather.location.lon,
                "tz_id" to weather.location.tz_id,
                "localtime_epoch" to weather.location.localtime_epoch,
                "localtime" to weather.location.localtime
            ),
            "current" to mapOf(
                "last_updated" to weather.current.last_updated,
                "temp_c" to weather.current.temp_c,
                "condition" to mapOf(
                    "text" to weather.current.condition.text,
                    "icon" to weather.current.condition.icon
                ),
                "wind_kph" to weather.current.wind_kph,
                "humidity" to weather.current.humidity,
                "cloud" to weather.current.cloud
            )
        )
        userWeatherCollection.document(uid).set(data).await()
    }

    suspend fun getUserWeather(uid: String): Weather? {
        return try {
            val doc = userWeatherCollection.document(uid).get().await()
            if (doc.exists()) {
                val locationObj = doc.get("location") as Map<*, *>
                val currentObj = doc.get("current") as Map<*, *>
                val conditionObj = currentObj["condition"] as Map<*, *>

                val location = Location(
                    name = locationObj["name"] as String,
                    region = locationObj["region"] as String,
                    country = locationObj["country"] as String,
                    lat = (locationObj["lat"] as Number).toDouble(),
                    lon = (locationObj["lon"] as Number).toDouble(),
                    tz_id = locationObj["tz_id"] as String,
                    localtime_epoch = (locationObj["localtime_epoch"] as Number).toLong(),
                    localtime = locationObj["localtime"] as String
                )

                val condition = WeatherCondition(
                    text = conditionObj["text"] as String,
                    icon = conditionObj["icon"] as String
                )

                val current = CurrentWeather(
                    last_updated = currentObj["last_updated"] as String,
                    temp_c = (currentObj["temp_c"] as Number).toDouble(),
                    condition = condition,
                    wind_kph = (currentObj["wind_kph"] as Number).toDouble(),
                    humidity = (currentObj["humidity"] as Number).toInt(),
                    cloud = (currentObj["cloud"] as Number).toInt()
                )

                Weather(location = location, current = current)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

