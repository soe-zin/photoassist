package com.example.photographyassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.photographyassistant.ui.views.CameraPreview
import com.example.photographyassistant.data.camera.PreviewHandler
import android.widget.Toast
import android.content.Context

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraPreviewScreen()
        }
    }
}

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val previewHandler = remember {
        PreviewHandler(context, previewView)
    }
    
    // Configure frame analysis
    val exposureMeterAnalyzer = remember {
        ExposureMeterAnalyzer()
    }
    
    LaunchedEffect(Unit) {
        previewHandler.configureFrameAnalysis(
            onExposureAnalysis = { exposureValue ->
                // Handle exposure analysis results
                android.util.Log.d("CameraPreview", "Exposure value: $exposureValue")
            },
            onError = { errorMsg ->
                android.util.Log.e("CameraPreview", "Error: $errorMsg")
            }
        )
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Camera Test Screen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(8.dp)
        )
        
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            onCameraReady = { camera ->
                previewHandler.startPreview(
                    onStarted = { camera ->
                        android.util.Log.d("CameraPreview", "Camera started successfully")
                    },
                    onError = { errorMsg ->
                        android.util.Log.e("CameraPreview", "Camera error: $errorMsg")
                    }
                )
            },
            onError = { errorMsg ->
                android.util.Log.e("CameraPreview", "Camera error: $errorMsg")
            }
        )
        
        Button(
            onClick = {
                previewHandler.stopPreview()
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("Stop Camera")
        }
    }
}