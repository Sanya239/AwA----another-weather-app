package com.hehe.awa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hehe.awa.data.Weather

@Composable
fun WeatherView(weather: Weather?) {
    weather?.let { w ->
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Weather",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val context = LocalContext.current
        val iconUrl = "https:${w.current.condition.icon}"

        Column {
            Text("Location: ${w.location.name}, ${w.location.country}")
            Text("Last updated: ${w.current.last_updated}")
            Text("Condition: ${w.current.condition.text}")

            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(iconUrl)
                            .build()
                    ),
                    contentDescription = w.current.condition.text,
                    modifier = Modifier.size(64.dp)
                )
            }

            Text("Temperature: ${w.current.temp_c}°C")
            Text("Wind: ${w.current.wind_kph} km/h")
            Text("Humidity: ${w.current.humidity}%")
            Text("Cloud: ${w.current.cloud}%")
        }
    }
    if (weather == null){
        Text("Unexpected error happened when loading weather")
    }
}