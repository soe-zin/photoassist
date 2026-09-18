package com.example.photographyassistant.data.camera

/**
 * Camera capabilities data structure.
 * 
 * Contains all discoverable camera features and specifications.
 */
import com.example.photographyassistant.data.camera.LensFacing
import com.example.photographyassistant.data.camera.FocusMode
import com.example.photographyassistant.data.camera.ExposureMode
import com.example.photographyassistant.data.camera.Resolution
import com.example.photographyassistant.data.camera.SensorSize
import com.example.photographyassistant.data.camera.RawSupport
import com.example.photographyassistant.data.camera.CameraFeatures

/**
 * Camera capabilities data class.
 * 
 * @param cameraId Unique identifier for the camera
 * @param lensFacing Camera lens facing direction
 * @param focusModes Available focus modes
 * @param exposureModes Available exposure modes
 * @param sensorSize Physical sensor dimensions
 * @param resolutions Supported image/video resolutions
 * @param rawSupport RAW sensor support status
 * @param features Additional camera features
 */
data class CameraCapabilities(
    val cameraId: String,
    val lensFacing: LensFacing,
    val focusModes: List<FocusMode>,
    val exposureModes: List<ExposureMode>,
    val sensorSize: SensorSize,
    val resolutions: List<Resolution>,
    val rawSupport: RawSupport,
    val features: CameraFeatures
) {
    /**
     * Check if camera supports a specific focus mode.
     * 
     * @param mode Focus mode to check
     * @return true if camera supports the mode
     */
    fun supportsFocusMode(mode: FocusMode): Boolean = mode in focusModes
    
    /**
     * Check if camera supports a specific exposure mode.
     * 
     * @param mode Exposure mode to check
     * @return true if camera supports the mode
     */
    fun supportsExposureMode(mode: ExposureMode): Boolean = mode in exposureModes
    
    /**
     * Get the maximum resolution supported by this camera.
     * 
     * @return Highest resolution (by pixel count)
     */
    fun getMaximumResolution(): Resolution {
        return resolutions.maxByOrNull { it.width * it.height } ?: Resolution(0, 0)
    }
    
    /**
     * Check if camera supports RAW photography.
     * 
     * @return true if RAW capture is supported
     */
    fun supportsRawCapture(): Boolean = rawSupport == RawSupport.SUPPORTED
    
    /**
     * Get the recommended preview resolution for this camera.
     * 
     * @return Best resolution for camera preview
     */
    fun getRecommendedPreviewResolution(): Resolution {
        // Prefer resolutions with aspect ratio close to 16:9 and good quality
        return resolutions.maxByOrNull { resolution ->
            if (resolution.width >= 1280 && resolution.height >= 720) {
                2  // HD+ or better
            } else if (resolution.width >= 800 && resolution.height >= 480) {
                1  // SD or better
            } else {
                0  // Lower quality
            }
        } ?: resolutions.firstOrNull() ?: Resolution(0, 0)
    }
}