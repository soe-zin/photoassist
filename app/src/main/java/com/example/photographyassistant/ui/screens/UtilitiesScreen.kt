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
import com.example.photographyassistant.ui.viewmodel.CalculatorViewModel

@Composable
fun UtilitiesScreen(modifier: Modifier = Modifier) {
    val viewModel: CalculatorViewModel = localViewModel()
    val calculationResult by viewModel.calculationResult.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Utilities",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = "Additional photography utilities and tools",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(8.dp)
        )
        
        if (isLoading) {
            Text(
                text = "Calculating...",
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
        
        // Basic exposure calculator utility
        Text(
            text = "Exposure Calculator",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )
        
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
                        viewModel.calculateExposure(iso, aperture, shutterSpeed)
                    } catch (e: NumberFormatException) {
                        // ViewModel will handle validation errors
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Calculate Exposure")
            }
            
            OutlinedButton(
                onClick = { viewModel.clearResults() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }
        }
        
        calculationResult?.let { result ->
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
                        text = "Result:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = result,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        } ?: run {
            Text(
                text = "Results will appear here",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}