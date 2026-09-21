package com.example.photographyassistant.data.camera

import android.hardware.camera2.CameraCharacteristics
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture

/**
 * Strategy interface for RAW capture processing.
 * 
 * Defines the contract for different RAW capture strategies based on camera capabilities.
 */
interface RawProcessingStrategy {
    
    /**
     * Get the image capture configuration for this strategy.
     * 
     * @return ImageCapture configuration
     */
    fun getImageCaptureConfig(): ImageCaptureConfig
    
    /**
     * Get the image analysis configuration for this strategy.
     * 
     * @return ImageAnalysis configuration
     */
    fun getImageAnalysisConfig(): ImageAnalysisConfig
    
    /**
     * Get the strategy name for debugging.
     * 
     * @return Strategy identifier
     */
    val strategyName: String
    
    /**
     * Check if this strategy supports RAW capture.
     * 
     * @return true if RAW capture is supported
     */
    val supportsRaw: Boolean
    
    /**
     * Get the preferred capture format.
     * 
     * @return ImageFormat for capture
     */
    val preferredCaptureFormat: Int
    
    /**
     * Get the preferred analysis format.
     * 
     * @return ImageFormat for analysis
     */
    val preferredAnalysisFormat: Int
    
    /**
     * Get quality settings for this strategy.
     * 
     * @return QualitySettings with strategy-specific parameters
     */
    fun getQualitySettings(): QualitySettings
}

/**
 * Configuration for ImageCapture use case.
 */
data class ImageCaptureConfig(
    val captureMode: Int = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
    val targetRotation: Int = 0,
    val maxResolution: Int = 0,
    val captureFormat: Int = ImageCaptureConfig.FORMAT_JPEG,
    val jpegQuality: Int = 95
) {
    companion object {
        const val FORMAT_JPEG = 1
        const val FORMAT_RAW = 2
        const val FORMAT_RAW_JPEG = 3
    }
}

/**
 * Configuration for ImageAnalysis use case.
 */
data class ImageAnalysisConfig(
    val targetResolution: androidx.camera.core.ImageAnalysis.TargetResolution? = null,
    val backpressureStrategy: Int = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
    val analysisFormat: Int = ImageAnalysisConfig.FORMAT_YUV_420_888
) {
    companion object {
        const val FORMAT_YUV_420_888 = 35
        const val FORMAT_RGBA_8888 = 1
        const val FORMAT_RAW_PRIVATE = 0x28 // Custom format for RAW
    }
}

/**
 * Quality settings for capture strategies.
 */
data class QualitySettings(
    val jpegQuality: Int = 95,
    val rawEnabled: Boolean = false,
    val enableHdr: Boolean = false,
    val enableNightMode: Boolean = false,
    val maxFramesForProcessing: Int = 1
)

/**
 * Strategy for cameras that fully support RAW capture.
 * 
 * Uses DNG RAW format with full sensor data preservation.
 */
class RawSupportedStrategy(
    private val cameraCharacteristics: CameraCharacteristics,
    private val sensorSize: SensorSize
) : RawProcessingStrategy {
    
    override val strategyName = 
RawSupportedStrategy
    override val supportsRaw = true
    override val preferredCaptureFormat = ImageCaptureConfig.FORMAT_RAW
    override val preferredAnalysisFormat = ImageAnalysisConfig.FORMAT_YUV_420_888
    
    override fun getImageCaptureConfig(): ImageCaptureConfig {
        val maxResolution = getMaxRawResolution()
        return ImageCaptureConfig(
            captureMode = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
            targetRotation = 0,
            maxResolution = maxResolution,
            captureFormat = ImageCaptureConfig.FORMAT_RAW,
            jpegQuality = 100
        )
    }
    
    override fun getImageAnalysisConfig(): ImageAnalysisConfig {
        return ImageAnalysisConfig(
            targetResolution = androidx.camera.core.ImageAnalysis.TargetResolution(
                getMaxRawResolution()
            ),
            backpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
            analysisFormat = preferredAnalysisFormat
        )
    }
    
    override fun getQualitySettings(): QualitySettings {
        return QualitySettings(
            jpegQuality = 100,
            rawEnabled = true,
            enableHdr = true,
            enableNightMode = true,
            maxFramesForProcessing = 3
        )
    }
    
    private fun getMaxRawResolution(): Int {
        val rawSize = cameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE)
        return if (rawSize != null) rawSize.width() * rawSize.height() else sensorSize.width * sensorSize.height
    }
}

/**
 * Strategy for cameras that do not support RAW capture.
 * 
 * Uses high-quality JPEG with maximum available resolution.
 */
class RawUnsupportedStrategy(
    private val cameraCharacteristics: CameraCharacteristics,
    private val sensorSize: SensorSize
) : RawProcessingStrategy {
    
    override val strategyName = RawUnsupportedStrategy
    override val supportsRaw = false
    override val preferredCaptureFormat = ImageCaptureConfig.FORMAT_JPEG
    override val preferredAnalysisFormat = ImageAnalysisConfig.FORMAT_YUV_420_888
    
    override fun getImageCaptureConfig(): ImageCaptureConfig {
        val maxResolution = getMaxJpegResolution()
        return ImageCaptureConfig(
            captureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
            targetRotation = 0,
            maxResolution = maxResolution,
            captureFormat = ImageCaptureConfig.FORMAT_JPEG,
            jpegQuality = 95
        )
    }
    
    override fun getImageAnalysisConfig(): ImageAnalysisConfig {
        return ImageAnalysisConfig(
            targetResolution = androidx.camera.core.ImageAnalysis.TargetResolution(
                getMaxJpegResolution()
            ),
            backpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
            analysisFormat = preferredAnalysisFormat
        )
    }
    
    override fun getQualitySettings(): QualitySettings {
        return QualitySettings(
            jpegQuality = 95,
            rawEnabled = false,
            enableHdr = true,
            enableNightMode = false,
            maxFramesForProcessing = 1
        )
    }
    
    private fun getMaxJpegResolution(): Int {
        val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val maxJpegSize = map?.getOutputSizes(ImageCaptureConfig.FORMAT_JPEG.toLong())?.maxByOrNull { it.width * it.height }
        return maxJpegSize?.width?.times(maxJpegSize?.height ?: 0) ?: sensorSize.width * sensorSize.height
    }
}

/**
 * Strategy for cameras with unknown RAW support.
 * 
 * Conservative approach using JPEG with good quality settings.
 */
class RawUnknownStrategy(
    private val cameraCharacteristics: CameraCharacteristics,
    private val sensorSize: SensorSize
) : RawProcessingStrategy {
    
    override val strategyName = RawUnknownStrategy
    override val supportsRaw = false
    override val preferredCaptureFormat = ImageCaptureConfig.FORMAT_JPEG
    override val preferredAnalysisFormat = ImageAnalysisConfig.FORMAT_YUV_420_888
    
    override fun getImageCaptureConfig(): ImageCaptureConfig {
        val resolution = getConservativeResolution()
        return ImageCaptureConfig(
            captureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY,
            targetRotation = 0,
            maxResolution = resolution,
            captureFormat = ImageCaptureConfig.FORMAT_JPEG,
            jpegQuality = 90
        )
    }
    
    override fun getImageAnalysisConfig(): ImageAnalysisConfig {
        return ImageAnalysisConfig(
            targetResolution = androidx.camera.core.ImageAnalysis.TargetResolution(getConservativeResolution()),
            backpressureStrategy = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST,
            analysisFormat = preferredAnalysisFormat
        )
    }
    
    override fun getQualitySettings(): QualitySettings {
        return QualitySettings(
            jpegQuality = 90,
            rawEnabled = false,
            enableHdr = false,
            enableNightMode = false,
            maxFramesForProcessing = 1
        )
    }
    
    private fun getConservativeResolution(): Int {
        // Use HD resolution as conservative fallback
        return 1920 * 1080
    }
}

/**
 * Factory for creating appropriate RAW processing strategies.
 */
object RawProcessingStrategyFactory {
    
    /**
     * Create the appropriate strategy based on camera characteristics.
     * 
     * @param cameraId Camera ID
     * @param characteristics Camera characteristics
     * @param sensorSize Sensor size
     * @param rawSupport RAW support status
     * @return Appropriate RawProcessingStrategy
     */
    fun createStrategy(
        cameraId: String,
        characteristics: CameraCharacteristics,
        sensorSize: SensorSize,
        rawSupport: RawSupport
    ): RawProcessingStrategy {
        return when (rawSupport) {
            RawSupport.SUPPORTED -> RawSupportedStrategy(characteristics, sensorSize)
            RawSupport.NOT_SUPPORTED -> RawUnsupportedStrategy(characteristics, sensorSize)
            RawSupport.UNKNOWN -> RawUnknownStrategy(characteristics, sensorSize)
        }
    }
    
    /**
     * Create strategy with enhanced detection.
     * 
     * @param cameraId Camera ID
     * @param characteristics Camera characteristics
     * @param sensorSize Sensor size
     * @return Strategy with enhanced detection
     */
    fun createStrategyWithEnhancedDetection(
        cameraId: String,
        characteristics: CameraCharacteristics,
        sensorSize: SensorSize
    ): RawProcessingStrategy {
        val enhancedRawSupport = detectEnhancedRawSupport(characteristics)
        return createStrategy(cameraId, characteristics, sensorSize, enhancedRawSupport)
    }
    
    /**
     * Enhanced RAW detection using multiple Camera2 API checks.
     * 
     * @param characteristics Camera characteristics
     * @return Enhanced RAW support status
     */
    private fun detectEnhancedRawSupport(characteristics: CameraCharacteristics): RawSupport {
        // Primary check: REQUEST_AVAILABLE_CAPABILITIES_RAW
        val availableCapabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        if (availableCapabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true) {
            return RawSupport.SUPPORTED
        }
        
        // Secondary check: SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE
        val preCorrectionSize = characteristics.get(CameraCharacteristics.SENSOR_INFO_PRE_CORRECTION_ACTIVE_ARRAY_SIZE)
        if (preCorrectionSize != null && preCorrectionSize.width() > 0 && preCorrectionSize.height() > 0) {
            // Sensor has raw data area
            return RawSupport.SUPPORTED
        }
        
        // Tertiary check: Check hardware level
        val hardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 ||
            hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL) {
            // Level 3 and external cameras typically support RAW
            return RawSupport.SUPPORTED
        }
        
        // Check for DNG support
        val dngSupported = characteristics.get(CameraCharacteristics.SENSOR_INFO_COLOR_FILTER_ARRANGEMENT)
        if (dngSupported != null) {
            // Color filter arrangement known, likely supports RAW
            return RawSupport.SUPPORTED
        }
        
        return RawSupport.UNKNOWN
    }
}
