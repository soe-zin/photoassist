package com.example.photographyassistant.ui.compose

import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Photography Assistant",
            style = MaterialTheme.typography.headlineLarge
        )
    }
}
