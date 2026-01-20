package com.hehe.awa.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class WeatherRepository {
    private val apiKey = "a300a5592a464dc887d215547262001"
    private val baseUrl = "http://api.weatherapi.com/v1/current.json"

    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Weather? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$baseUrl?key=$apiKey&q=$latitude,$longitude")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val locationObj = json.getJSONObject("location")
                val location = Location(
                    name = locationObj.getString("name"),
                    region = locationObj.getString("region"),
                    country = locationObj.getString("country"),
                    lat = locationObj.getDouble("lat"),
                    lon = locationObj.getDouble("lon"),
                    tz_id = locationObj.getString("tz_id"),
                    localtime_epoch = locationObj.getLong("localtime_epoch"),
                    localtime = locationObj.getString("localtime")
                )

                val currentObj = json.getJSONObject("current")
                val conditionObj = currentObj.getJSONObject("condition")
                val condition = WeatherCondition(
                    text = conditionObj.getString("text"),
                    icon = conditionObj.getString("icon")
                )

                val current = CurrentWeather(
                    last_updated = currentObj.getString("last_updated"),
                    temp_c = currentObj.getDouble("temp_c"),
                    condition = condition,
                    wind_kph = currentObj.getDouble("wind_kph"),
                    humidity = currentObj.getInt("humidity"),
                    cloud = currentObj.getInt("cloud")
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

