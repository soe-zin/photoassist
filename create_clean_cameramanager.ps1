# PowerShell script to create clean CameraManager.kt
$cameraManagerPath = "app\src\main\java\com\example\photographyassistant\data\camera\CameraManager.kt"

Write-Host "=== Creating Clean CameraManager.kt ==="

# Remove if it exists
if (Test-Path $cameraManagerPath) {
    Remove-Item $cameraManagerPath -Force
    Write-Host "Removed old CameraManager.kt"
}

# Create the directory structure if it doesn't exist
$directory = Split-Path $cameraManagerPath -Parent
if (-not (Test-Path $directory)) {
    New-Item -Path $directory -ItemType Directory -Force | Out-Null
}

# Create the clean CameraManager.kt file
$content = @'
package com.example.photographyassistant.data.camera

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.photographyassistant.data.camera.CameraCapabilities
import com.example.photographyassistant.data.camera.LensFacing
import com.example.photographyassistant.data.camera.FocusMode
import com.example.photographyassistant.data.camera.ExposureMode
import com.example.photographyassistant.data.camera.Resolution
import com.example.photographyassistant.data.camera.SensorSize
import com.example.photographyassistant.data.camera.RawSupport
import com.example.photographyassistant.data.camera.CameraFeatures

/**
 * Camera capability discovery and management.
 *
 * Uses Camera2 API to discover available camera features and capabilities.
 * Provides comprehensive camera information for both preview and processing.
 */
class CameraManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val availableCameraIds = getAvailableCameraIds()

    /**
 * Discover all available camera capabilities.
 *
 * @return List of CameraCapabilities for all available cameras
 */
    fun discoverCameraCapabilities(): List<CameraCapabilities> {
        return availableCameraIds.mapNotNull { cameraId ->
            discoverCapabilitiesForCamera(cameraId)
        }
    }

    private fun getAvailableCameraIds(): List<String> {
        return try {
            cameraManager.cameraIdList.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
'

# Write the file
$content | Out-File -FilePath $cameraManagerPath -Encoding UTF8 -Force

Write-Host "CameraManager.kt created successfully at: $cameraManagerPath"
Write-Host "File size: $((Get-Item $cameraManagerPath).Length) bytes"

# Final verification
if (Test-Path $cameraManagerPath) {
    Write-Host "✅ CameraManager.kt created successfully"
} else {
    Write-Host "❌ CameraManager.kt creation failed"
}
