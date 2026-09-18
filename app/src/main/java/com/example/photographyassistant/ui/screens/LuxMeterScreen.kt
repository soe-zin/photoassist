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
import com.example.photographyassistant.ui.viewmodel.LuxMeterViewModel

@Composable
fun LuxMeterScreen(modifier: Modifier = Modifier) {
    val viewModel: LuxMeterViewModel = localViewModel()
    val luxMeasurement by viewModel.luxMeasurement.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Lux Meter",
            style = MaterialTheme.typography.headlineMedium
        )
        
        if (isLoading) {
            Text(
                text = "Calculating lux...",
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
        
        var isoInput by remember { mutableStateOf("100") }
        var apertureInput by remember { mutableStateOf("2.8") }
        var shutterSpeedInput by remember { mutableStateOf("0.5") }
        
        TextField(
            value = isoInput,
            onValueChange = { isoInput = it },
            label = { Text("ISO") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = apertureInput,
            onValueChange = { apertureInput = it },
            label = { Text("Aperture (f-stop)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = shutterSpeedInput,
            onValueChange = { shutterSpeedInput = it },
            label = { Text("Shutter Speed (s)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(
                onClick = { 
                    try {
                        val iso = isoInput.toDouble()
                        val aperture = apertureInput.toDouble()
                        val shutterSpeed = shutterSpeedInput.toDouble()
                        viewModel.calculateLux(iso, aperture, shutterSpeed)
                    } catch (e: NumberFormatException) {
                        // ViewModel will handle validation errors
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Calculate Lux")
            }
            
            OutlinedButton(
                onClick = { viewModel.clearResults() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }
        
        luxMeasurement?.let { measurement ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (measurement.isCalibrated) 
                        MaterialTheme.colorScheme.secondaryContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lux Measurement:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${String.format("%.0f", measurement.lux)} lx",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (measurement.isCalibrated) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = "ISO: ${"%.0f".format(measurement.iso)}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Aperture: f/${"%.1f".format(measurement.aperture)}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Shutter: ${"%.2f".format(measurement.shutterSpeed)}s",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        Text(
                            text = if (measurement.isCalibrated) "Calibrated" else if (measurement.isEstimated) "Estimated" else "Raw",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (measurement.isCalibrated) 
                                MaterialTheme.colorScheme.secondary 
                            else if (measurement.isEstimated)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = measurement.qualityInfo,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } ?: run {
            Text(
                text = "Lux results will appear here",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}