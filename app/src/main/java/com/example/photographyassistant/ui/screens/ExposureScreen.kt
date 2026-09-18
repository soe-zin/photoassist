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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.localViewModel
import com.example.photographyassistant.ui.viewmodel.ExposureViewModel

@Composable
fun ExposureScreen(modifier: Modifier = Modifier) {
    val viewModel: ExposureViewModel = localViewModel()
    val exposureValue by viewModel.exposureValue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Exposure Calculator",
            style = MaterialTheme.typography.headlineMedium
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
        
        TextField(
            value = "100",
            onValueChange = { /* Handle ISO input */ },
            label = { Text("ISO") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = "2.8",
            onValueChange = { /* Handle aperture input */ },
            label = { Text("Aperture (f-stop)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        TextField(
            value = "0.5",
            onValueChange = { /* Handle shutter speed input */ },
            label = { Text("Shutter Speed (s)") },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        
        Button(
            onClick = { /* Calculate exposure */ },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Calculate Exposure")
        }
        
        exposureValue?.let { value ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Exposure Result:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = "ISO: ${"%.0f".format(value.iso)}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Aperture: f/${"%.1f".format(value.aperture)}",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Shutter: ${"%.2f".format(value.shutterSpeed)}s",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } ?: run {
            Text(
                text = "Results will appear here",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
