cd /vscode_workspace/photoassist
if exist app\src\main\java\com\example\photographyassistant\data\camera\CameraManager.kt (
    del app\src\main\java\com\example\photographyassistant\data\camera\CameraManager.kt
    echo "Removed old CameraManager.kt"
)
echo "Creating new CameraManager.kt..."
cat > app\src\main\java\com\example\photographyassistant\data\camera\CameraManager.kt << 'EOF'
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
EOF
echo "CameraManager.kt created successfully!"