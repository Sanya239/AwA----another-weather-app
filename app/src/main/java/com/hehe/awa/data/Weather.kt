package com.hehe.awa.data

data class Weather(
    val location: Location,
    val current: CurrentWeather,
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    val localtime: String
)

data class CurrentWeather(
    val last_updated: String,
    val temp_c: Double,
    val condition: WeatherCondition,
    val wind_kph: Double,
    val humidity: Int,
    val cloud: Int
)

data class WeatherCondition(
    val text: String,
    val icon: String
)

