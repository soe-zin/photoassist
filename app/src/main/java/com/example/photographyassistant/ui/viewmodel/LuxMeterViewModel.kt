package com.example.photographyassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photographyassistant.domain.lux.LuxCalculator
import com.example.photographyassistant.domain.lux.LuxMeasurement
import com.example.photographyassistant.domain.lux.CalibrationProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

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