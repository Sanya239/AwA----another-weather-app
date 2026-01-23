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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.hehe.awa.R
import com.hehe.awa.data.Weather
import kotlin.math.roundToInt

@Composable
fun WeatherView(weather: Weather?, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        weather?.let { w ->
            Text(
                text = stringResource(R.string.weather),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val context = LocalContext.current
            val iconUrl = "https:${w.current.condition.icon}"

            Column {
                Text(stringResource(R.string.condition, w.current.condition.text))

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

                Text(stringResource(R.string.temperature, w.current.temp_c))
                Text(stringResource(R.string.feels_like, w.current.temp_c.roundToInt()))
                Text(stringResource(R.string.wind, w.current.wind_kph))
                Text(stringResource(R.string.humidity, w.current.humidity))
                Text(stringResource(R.string.cloud, w.current.cloud))
                Text(stringResource(R.string.last_updated, w.current.last_updated))
            }
        }
        if (weather == null){
            Text(stringResource(R.string.weather_load_error))
        }
    }
}