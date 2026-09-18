package com.example.photographyassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.localViewModel
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.example.photographyassistant.ui.viewmodel.SolarPositionViewModel

@Composable
fun SolarCalculatorScreen(modifier: Modifier = Modifier) {
    val viewModel: SolarPositionViewModel = localViewModel()
    val solarPosition by viewModel.solarPosition.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Solar Calculator",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Calculate solar position, sunrise/sunset, and golden hour",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
        
        if (isLoading) {
            Text(
                text = "Calculating solar position...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        // Input fields for solar calculation
        var yearInput by remember { mutableStateOf("2024") }
        var monthInput by remember { mutableStateOf("6") }
        var dayInput by remember { mutableStateOf("15") }
        var hourInput by remember { mutableStateOf("12.0") }
        var latitudeInput by remember { mutableStateOf("40.7128") }
        var longitudeInput by remember { mutableStateOf("-74.0060") }
        
        TextField(
            value = yearInput,
            onValueChange = { yearInput = it },
            label = { Text("Year") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = monthInput,
            onValueChange = { monthInput = it },
            label = { Text("Month (1-12)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = dayInput,
            onValueChange = { dayInput = it },
            label = { Text("Day (1-31)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = hourInput,
            onValueChange = { hourInput = it },
            label = { Text("Hour (0-23.9)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = latitudeInput,
            onValueChange = { latitudeInput = it },
            label = { Text("Latitude (-90 to 90)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = longitudeInput,
            onValueChange = { longitudeInput = it },
            label = { Text("Longitude (-180 to 180)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(
                onClick = { 
                    try {
                        val year = yearInput.toInt()
                        val month = monthInput.toInt()
                        val day = dayInput.toInt()
                        val hour = hourInput.toDouble()
                        val latitude = latitudeInput.toDouble()
                        val longitude = longitudeInput.toDouble()
                        
                        viewModel.calculateSolarPosition(year, month, day, hour, latitude, longitude)
                    } catch (e: NumberFormatException) {
                        // ViewModel will handle validation errors
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Calculate Solar Position")
            }
            
            OutlinedButton(
                onClick = { viewModel.clearResults() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }
        
        solarPosition?.let { position ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Solar Position Results:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = viewModel.getFormattedSolarInfo(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = "Elevation: ${"%.1f".format(position.elevation)}°",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Azimuth: ${"%.1f".format(position.azimuth)}°",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            text = "Sunrise: ${"%.2f".format(position.sunriseTime)}h",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Sunset: ${"%.2f".format(position.sunsetTime)}h",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            text = "Solar Noon: ${"%.2f".format(viewModel.getSolarNoon(yearInput.toInt(), monthInput.toInt(), dayInput.toInt(), longitudeInput.toDouble()))}h",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Day Length: ${"%.2f".format(viewModel.getDayLength(yearInput.toInt(), monthInput.toInt(), dayInput.toInt(), latitudeInput.toDouble(), longitudeInput.toDouble()))}h",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = "Daylight: ${if (viewModel.isDaylightHour(yearInput.toInt(), monthInput.toInt(), dayInput.toInt(), hourInput.toDouble(), latitudeInput.toDouble(), longitudeInput.toDouble())) "Yes" else "No"}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Golden Hour: ${if (viewModel.isGoldenHour(yearInput.toInt(), monthInput.toInt(), dayInput.toInt(), hourInput.toDouble(), latitudeInput.toDouble(), longitudeInput.toDouble())) "Yes" else "No"}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } ?: run {
            Text(
                text = "Solar position results will appear here",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}