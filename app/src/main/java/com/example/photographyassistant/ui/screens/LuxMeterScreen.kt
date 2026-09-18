package com.example.photographyassistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.localViewModel
import com.example.photographyassistant.ui.viewmodel.LuxMeterViewModel

@Composable
fun LuxMeterScreen(modifier: Modifier = Modifier) {
    val viewModel: LuxMeterViewModel = localViewModel()
    
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Lux Meter",
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Input fields for ISO, aperture, shutter speed
        TextField(
            value = "100",
            onValueChange = { /* TODO: Handle ISO input */ },
            label = { Text("ISO") },
            modifier = Modifier.padding(8.dp)
        )
        
        TextField(
            value = "2.8",
            onValueChange = { /* TODO: Handle aperture input */ },
            label = { Text("Aperture (f-stop)") },
            modifier = Modifier.padding(8.dp)
        )
        
        TextField(
            value = "0.5",
            onValueChange = { /* TODO: Handle shutter speed input */ },
            label = { Text("Shutter Speed (s)") },
            modifier = Modifier.padding(8.dp)
        )
        
        Button(
            onClick = { /* TODO: Calculate lux */ },
            modifier = Modifier.padding(8.dp)
        ) {
            Text("Calculate Lux")
        }
        
        // Display results
        Text(
            text = "Lux results will appear here",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}
