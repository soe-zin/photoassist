package com.example.photographyassistant.data.camera

import android.hardware.camera2.CameraCharacteristics

/**
 * RAW capture configuration and state management.
 */
data class RawCaptureConfig(
    val cameraId: String,
    val strategy: RawProcessingStrategy,
    val rawSupport: RawSupport,
    val sensorSize: SensorSize,
    val maxRawResolution: Resolution,
    val maxJpegResolution: Resolution,
    val isRawAvailable: Boolean,
    val fallbackReason: String? = null
) {
    fun getCaptureMode(): Int = strategy.getImageCaptureConfig().captureMode
    fun getJpegQuality(): Int = strategy.getImageCaptureConfig().jpegQuality
    fun getAnalysisFormat(): Int = strategy.preferredAnalysisFormat
    fun getQualitySettings(): QualitySettings = strategy.getQualitySettings()
    
    companion object {
        fun createDefault(cameraId: String): RawCaptureConfig {
            return RawCaptureConfig(
                cameraId = cameraId,
                strategy = RawUnsupportedStrategy(null, SensorSize(0, 0)),
                rawSupport = RawSupport.NOT_SUPPORTED,
                sensorSize = SensorSize(0, 0),
                maxRawResolution = Resolution(0, 0),
                maxJpegResolution = Resolution(1920, 1080),
                isRawAvailable = false,
                fallbackReason = "Default configuration"
            )
        }
    }
}