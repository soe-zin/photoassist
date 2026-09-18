package com.example.photographyassistant.ui.views

import android.content.Context
import android.view.Surface
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera preview view with lifecycle management.
 * 
 * Provides a reusable CameraX preview component with proper lifecycle handling,
 * error handling, and configuration flexibility.
 * 
 * @param modifier Modifier for layout styling
 * @param onCameraReady Callback when camera is ready for use
 * @param onError Callback when camera error occurs
 * @param cameraSelector Camera lens selector (front/back)
 * @param previewConfig Preview configuration options
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier.fillMaxSize(),
    onCameraReady: (Camera) -> Unit = { _ -> },
    onError: (String) -> Unit = { _ -> },
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    previewConfig: PreviewConfig = PreviewConfig()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    Box(modifier = modifier) {
        LaunchedEffect(Unit) {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context)
                cameraProvider.await()
                
                val preview = Preview.Builder()
                    .setTargetResolution(previewConfig.resolution)
                    .setTargetRotation(previewConfig.rotation)
                    .build()
                    
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
                
                preview.setSurfaceProvider(previewView.surfaceProvider)
                
                onCameraReady(camera)
                
            } catch (e: Exception) {
                onError("Camera initialization failed: ${e.message}")
            }
        }
    }
    
    PreviewView(
        previewView,
        modifier = modifier
    )
}

/**
 * Preview configuration options.
 * 
 * @param resolution Preview resolution
 * @param rotation Display rotation
 * @param enableAudio Whether to enable audio capture
 */
data class PreviewConfig(
    val resolution: android.util.Size = android.util.Size(1920, 1080),
    val rotation: Int = android.view.Surface.ROTATION_0,
    val enableAudio: Boolean = false
) {
    /**
     * Get optimal resolution based on device capabilities.
     * 
     * @param preferHighQuality Whether to prefer higher quality over performance
     * @return Optimal resolution for the device
     */
    fun getOptimalResolution(preferHighQuality: Boolean = false): android.util.Size {
        return when {
            preferHighQuality -> android.util.Size(1920, 1080)  // Full HD
            else -> android.util.Size(1280, 720)  // HD
        }
    }
}