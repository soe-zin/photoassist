package com.example.photographyassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photographyassistant.domain.exposure.ExposureCalculator
import com.example.photographyassistant.domain.exposure.ExposureValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Exposure feature using MVVM architecture.
 * 
 * This ViewModel handles exposure calculations and UI state management for the exposure meter.
 * It follows the clean architecture principle with separation between presentation and domain layers.
 */

class ExposureViewModel : ViewModel() {
    private val exposureCalculator = ExposureCalculator()
    
    private val _exposureValue = MutableStateFlow<ExposureValue?>(null)
    val exposureValue: StateFlow<ExposureValue?> = _exposureValue
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Calculate exposure based on ISO, aperture, and shutter speed.
     */
    fun calculateExposure(iso: Double, aperture: Double, shutterSpeed: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = exposureCalculator.calculateExposure(iso, aperture, shutterSpeed)
                _exposureValue.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calculating exposure"
                _exposureValue.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get exposure recommendation based on current light conditions.
     */
    fun getExposureRecommendation(
        currentEv: Double,
        meteringMode: String,
        sceneBrightness: String,
        aperture: Double,
        shutterSpeed: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Map string values to enum classes
                val meteringModeEnum = when (meteringMode) {
                    "Center Weighted" -> com.example.photographyassistant.domain.exposure.MeteringMode.CENTER_WEIGHTED
                    "Pattern" -> com.example.photographyassistant.domain.exposure.MeteringMode.PATTERN
                    "Multi" -> com.example.photographyassistant.domain.exposure.MeteringMode.MULTI
                    "Spatial" -> com.example.photographyassistant.domain.exposure.MeteringMode.SPATIAL
                    else -> com.example.photographyassistant.domain.exposure.MeteringMode.CENTER_WEIGHTED
                }
                
                val sceneBrightnessEnum = when (sceneBrightness) {
                    "Dark" -> com.example.photographyassistant.domain.exposure.SceneBrightness.DARK
                    "Normal" -> com.example.photographyassistant.domain.exposure.SceneBrightness.NORMAL
                    "Bright" -> com.example.photographyassistant.domain.exposure.SceneBrightness.BRIGHT
                    "Highlight" -> com.example.photographyassistant.domain.exposure.SceneBrightness.HIGHLIGHT
                    else -> com.example.photographyassistant.domain.exposure.SceneBrightness.NORMAL
                }
                
                val recommendation = exposureCalculator.calculateExposureCompensation(
                    currentEv, meteringModeEnum, sceneBrightnessEnum
                )
                
                val exposureValue = exposureCalculator.calculateExposureRecommendation(
                    currentEv, aperture, shutterSpeed
                )
                
                _exposureValue.value = exposureValue
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error getting recommendation"
                _exposureValue.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear current exposure calculation and error messages.
     */
    fun clearResults() {
        _exposureValue.value = null
        _errorMessage.value = null
    }

    /**
     * Calculate optimal aperture for given ISO and shutter speed.
     */
    fun calculateOptimalAperture(iso: Double, shutterSpeed: Double): Double {
        return exposureCalculator.calculateOptimalAperture(iso, shutterSpeed)
    }
}