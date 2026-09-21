package com.example.photographyassistant.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photographyassistant.data.camera.CameraManager
import com.example.photographyassistant.data.camera.RawCaptureConfig
import com.example.photographyassistant.domain.lux.LuxCalculator
import com.example.photographyassistant.domain.lux.LuxMeasurement
import com.example.photographyassistant.domain.lux.CalibrationProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Pair

/**
 * ViewModel for Lux Meter feature using MVVM architecture.
 * 
 * This ViewModel handles lux measurements and calibration UI state management.
 * It provides the interface between the Lux calculator domain layer and the UI layer.
 */

class LuxMeterViewModel : ViewModel() {
    private val luxCalculator = LuxCalculator()
    
    private val _luxMeasurement = MutableStateFlow<LuxMeasurement?>(null)
    val luxMeasurement: StateFlow<LuxMeasurement?> = _luxMeasurement
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _rawCaptureConfig = MutableStateFlow<RawCaptureConfig?>(null)
    val rawCaptureConfig: StateFlow<RawCaptureConfig?> = _rawCaptureConfig
    
    private val _cameraManager by lazy { CameraManager(this) }

    /**
     * Calculate lux measurement from camera sensor data.
     */
    fun calculateLux(iso: Double, aperture: Double, shutterSpeed: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = luxCalculator.estimateLux(iso, aperture, shutterSpeed)
                _luxMeasurement.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calculating lux"
                _luxMeasurement.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Apply calibration profile to lux measurement.
     */
    fun calibrateLux(calibrationProfile: CalibrationProfile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val currentLux = _luxMeasurement.value
                if (currentLux != null && currentLux.isEstimated) {
                    val calibratedLux = luxCalculator.calibrateLux(currentLux, calibrationProfile)
                    _luxMeasurement.value = calibratedLux
                } else {
                    _errorMessage.value = "No estimated lux measurement available for calibration"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calibrating lux"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get default calibration profile.
     */
    fun getDefaultCalibrationProfile(): CalibrationProfile {
        return CalibrationProfile(1.2, 100.0)
    }

    /**
     * Clear current lux measurement and error messages.
     */
    fun clearResults() {
        _luxMeasurement.value = null
        _errorMessage.value = null
    }

    /**
     * Check if current lux measurement is calibrated.
     */
    fun isCalibrated(): Boolean {
        return _luxMeasurement.value?.isCalibrated ?: false
    }

    /**
     * Initialize camera RAW capability detection.
     * This implements Task 9: Detect RAW sensor support and setup fallback.
     */
    fun initializeRawCapabilityDetection() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // For now, use camera ID 0 as example. In real implementation,
                // this would come from the active camera source
                val cameraId = "0"
                
                // Detect RAW support and create appropriate capture config
                val rawConfig = _cameraManager.detectRawSupportAndFallback(cameraId)
                _rawCaptureConfig.value = rawConfig
                
                // Extract calibration info if RAW is supported
                val calibrationProfile = _cameraManager.extractCalibrationInfo(cameraId)
                if (calibrationProfile != null) {
                    calibrateLux(calibrationProfile)
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize RAW capability detection: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get RAW capture strategy and fallback information.
     */
    fun getRawCaptureStrategy(): Pair<String, String?> {
        val config = _rawCaptureConfig.value
        if (config == null) {
            return Pair("No camera config", null)
        }
        
        val strategyName = config.strategy.strategyName
        val fallbackInfo = config.fallbackReason
        
        return Pair(strategyName, fallbackInfo)
    }

    /**
     * Check if RAW capture is currently available.
     */
    fun isRawCaptureAvailable(): Boolean {
        return _rawCaptureConfig.value?.isRawAvailable ?: false
    }

    /**
     * Get lux measurement with RAW calibration status.
     */
    fun getLuxMeasurementWithCalibrationInfo(): String {
        val measurement = _luxMeasurement.value
        val rawInfo = getRawCaptureStrategy()
        
        if (measurement == null) {
            return "No measurement available"
        }
        
        val calibrationStatus = when {
            measurement.isCalibrated -> "CALIBRATED (RAW)"
            measurement.isEstimated -> "ESTIMATED (RAW fallback)"
            else -> "UNCALIBRATED"
        }
        
        val rawStatus = if (isRawCaptureAvailable()) "RAW SUPPORTED" else "JPEG ONLY"
        
        return "Lux: ${measurement.lux} lx | Calibration: $calibrationStatus | RAW: $rawStatus | ${getQualityInfo()}"
    }

    /**
     * Get lux measurement quality information.
     */
    fun getQualityInfo(): String {
        val measurement = _luxMeasurement.value
        return when {
            measurement == null -> "No measurement available"
            measurement.isCalibrated -> "Calibrated - High accuracy"
            measurement.isEstimated -> "Estimated - Medium accuracy"
            else -> "Uncalibrated - Low accuracy"
        }
    }
}