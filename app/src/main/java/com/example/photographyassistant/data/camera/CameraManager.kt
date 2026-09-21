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

    /**
     * Extract calibration information from RAW capture configuration.
     * 
     * @param cameraId Camera ID
     * @return Calibration profile if available, null otherwise
     */
    fun extractCalibrationInfo(cameraId: String): CalibrationProfile? {
        return try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val sensorSize = getSensorSizeFromCharacteristics(characteristics)
            val rawSupport = RawProcessingStrategyFactory.detectEnhancedRawSupport(characteristics)
            
            if (rawSupport == RawSupport.SUPPORTED) {
                // Create a calibrated strategy for RAW-capable cameras
                val strategy = RawSupportedStrategy(characteristics, sensorSize)
                val qualitySettings = strategy.getQualitySettings()
                
                // Create calibration profile based on strategy and sensor characteristics
                return CalibrationProfile(
                    calibrationFactor = calculateCalibrationFactor(characteristics),
                    luxOffset = calculateLuxOffset(characteristics),
                    description = "RAW-calibrated profile for $cameraId"
                )
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate calibration factor based on camera characteristics.
     */
    private fun calculateCalibrationFactor(characteristics: CameraCharacteristics): Double {
        // Default calibration factor
        var calibrationFactor = 1.0
        
        // Try to get sensitivity from characteristics
        val sensitivity = characteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY)
        if (sensitivity != null) {
            // Adjust calibration factor based on sensor sensitivity
            calibrationFactor = sensitivity.toDouble() / 100.0 // Normalize to typical ISO 100
        }
        
        // Ensure calibration factor is within valid range
        return kotlin.math.max(0.1, kotlin.math.min(10.0, calibrationFactor))
    }

    /**
     * Calculate lux offset based on camera characteristics.
     */
    private fun calculateLuxOffset(characteristics: CameraCharacteristics): Double {
        // Default lux offset
        var luxOffset = 0.0
        
        // Try to get sensor size for lux calculations
        val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        if (sensorSize != null) {
            val width = sensorSize.getWidth()
            val height = sensorSize.getHeight()
            
            // Adjust lux offset based on sensor size (larger sensors typically have different calibration)
            luxOffset = (width * height) / 1000.0
        }
        
        return luxOffset
    }

    /**
     * Detect RAW support and provide fallback configuration.
     * This implements Task 9: Implement RAW capability detection and fallback.
     * 
     * @param cameraId Camera ID to analyze
     * @return RawCaptureConfig with appropriate strategy (RAW or fallback)
     */
    fun detectRawSupportAndFallback(cameraId: String): RawCaptureConfig {
        try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val sensorSize = getSensorSizeFromCharacteristics(characteristics)
            val rawSupport = RawProcessingStrategyFactory.detectEnhancedRawSupport(characteristics)
            
            return createRawCaptureConfig(cameraId, rawSupport, characteristics, sensorSize)
            
        } catch (e: Exception) {
            // Camera unavailable - return default fallback configuration
            return RawCaptureConfig.createDefault(cameraId)
        }
    }

    /**
     * Create fallback capture configuration when RAW is not available.
     * Implements graceful degradation from RAW to high-quality JPEG.
     * 
     * @param cameraId Camera ID
     * @return RawCaptureConfig configured for JPEG fallback
     */
    fun createJpegFallbackConfig(cameraId: String): RawCaptureConfig {
        return RawCaptureConfig(
            cameraId = cameraId,
            strategy = RawUnsupportedStrategy(
                cameraManager.getCameraCharacteristics(cameraId),
                SensorSize(36.0f, 24.0f) // Default sensor size
            ),
            rawSupport = RawSupport.NOT_SUPPORTED,
            sensorSize = SensorSize(36.0f, 24.0f),
            maxRawResolution = Resolution(0, 0),
            maxJpegResolution = Resolution(1920, 1080),
            isRawAvailable = false,
            fallbackReason = "Camera does not support RAW capture - falling back to JPEG"
        )
    }

    /**
     * Check if a camera supports RAW capture and provide status.
     * 
     * @param cameraId Camera ID to check
     * @return RawSupport status with error message if not supported
     */
    fun checkRawSupport(cameraId: String): Pair<RawSupport, String?> {
        try {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val rawSupport = RawProcessingStrategyFactory.detectEnhancedRawSupport(characteristics)
            
            val errorMessage = when (rawSupport) {
                RawSupport.SUPPORTED -> null
                RawSupport.NOT_SUPPORTED -> "Camera hardware does not support RAW capture"
                RawSupport.UNKNOWN -> "RAW support status could not be determined - using JPEG fallback"
            }
            
            return Pair(rawSupport, errorMessage)
            
        } catch (e: Exception) {
            return Pair(RawSupport.UNKNOWN, "Error checking RAW support: ${e.message}")
        }
    }

    /**
     * Discover RAW capture capabilities and create appropriate configuration.
     *
     * @param cameraId Camera ID to analyze
     * @return CameraCapabilities with RAW support information or null if camera unavailable
     */
    fun discoverRawCapabilities(cameraId: String): CameraCapabilities? {
        return try {
            // Get camera characteristics
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val sensorSize = getSensorSizeFromCharacteristics(characteristics)
            
            // Detect RAW support using enhanced detection
            val rawSupport = RawProcessingStrategyFactory.detectEnhancedRawSupport(characteristics)
            
            // Create appropriate capture configuration
            val captureConfig = createRawCaptureConfig(cameraId, rawSupport, characteristics, sensorSize)
            
            // Create CameraCapabilities with RAW information
            createCameraCapabilitiesWithRaw(cameraId, characteristics, captureConfig)
            
        } catch (e: Exception) {
            // Camera unavailable or error during detection
            null
        }
    }

    /**
     * Get sensor size from camera characteristics.
     */
    private fun getSensorSizeFromCharacteristics(characteristics: CameraCharacteristics): SensorSize {
        val sensorSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
        if (sensorSize != null) {
            return SensorSize(sensorSize.getWidth(), sensorSize.getHeight())
        }
        // Fallback to default size if not available
        return SensorSize(36.0f, 24.0f) // Typical sensor size
    }

    /**
     * Create RAW capture configuration based on detected support.
     */
    private fun createRawCaptureConfig(
        cameraId: String,
        rawSupport: RawSupport,
        characteristics: CameraCharacteristics,
        sensorSize: SensorSize
    ): RawCaptureConfig {
        val strategy = RawProcessingStrategyFactory.createStrategy(
            cameraId, characteristics, sensorSize, rawSupport
        )
        
        val sensorInfoSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        val maxRawResolution = if (sensorInfoSize != null && 
            rawSupport == RawSupport.SUPPORTED) {
            Resolution(sensorInfoSize.getWidth(), sensorInfoSize.getHeight())
        } else {
            Resolution(0, 0)
        }
        
        val maxJpegResolution = Resolution(1920, 1080) // HD fallback
        
        return RawCaptureConfig(
            cameraId = cameraId,
            strategy = strategy,
            rawSupport = rawSupport,
            sensorSize = sensorSize,
            maxRawResolution = maxRawResolution,
            maxJpegResolution = maxJpegResolution,
            isRawAvailable = rawSupport == RawSupport.SUPPORTED,
            fallbackReason = when (rawSupport) {
                RawSupport.SUPPORTED -> null
                RawSupport.NOT_SUPPORTED -> "Camera hardware does not support RAW capture"
                RawSupport.UNKNOWN -> "RAW support status could not be determined"
            }
        )
    }

    /**
     * Create CameraCapabilities with RAW information.
     */
    private fun createCameraCapabilitiesWithRaw(
        cameraId: String,
        characteristics: CameraCharacteristics,
        captureConfig: RawCaptureConfig
    ): CameraCapabilities {
        // Extract basic camera features
        val lensFacing = when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
            CameraCharacteristics.LENS_FACING_FRONT -> LensFacing.FRONT
            CameraCharacteristics.LENS_FACING_BACK -> LensFacing.BACK
            else -> LensFacing.EXTERNAL
        }
        
        val focusModes = listOf(FocusMode.AUTO_FOCUS, FocusMode.MANUAL_FOCUS)
        val exposureModes = listOf(ExposureMode.AUTO, ExposureMode.MANUAL)
        
        val cameraFeatures = CameraFeatures(
            opticalImageStabilization = false, // Would need to check capabilities
            electronicImageStabilization = false, // Would need to check capabilities
            burstShooting = false, // Would need to check capabilities
            continuousShooting = false // Would need to check capabilities
        )
        
        return CameraCapabilities(
            cameraId = cameraId,
            lensFacing = lensFacing,
            focusModes = focusModes,
            exposureModes = exposureModes,
            sensorSize = captureConfig.sensorSize,
            resolutions = listOf(Resolution(1920, 1080), Resolution(1280, 720)),
            rawSupport = captureConfig.rawSupport,
            features = cameraFeatures
        )
    }