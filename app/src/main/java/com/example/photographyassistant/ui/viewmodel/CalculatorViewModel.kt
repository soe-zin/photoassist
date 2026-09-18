package com.example.photographyassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photographyassistant.domain.exposure.ExposureCalculator
import com.example.photographyassistant.domain.exposure.ExposureValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Calculator feature using MVVM architecture.
 * 
 * This ViewModel handles various photography calculator functions including exposure,
 * lighting, and exposure compensation calculations. It provides the interface between
 * the exposure calculator domain layer and the UI layer for calculator functions.
 */

class CalculatorViewModel : ViewModel() {
    private val exposureCalculator = ExposureCalculator()
    
    private val _calculationResult = MutableStateFlow<String?>(null)
    val calculationResult: StateFlow<String?> = _calculationResult
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Calculate exposure value from ISO, aperture, and shutter speed.
     */
    fun calculateExposure(iso: Double, aperture: Double, shutterSpeed: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = exposureCalculator.calculateExposure(iso, aperture, shutterSpeed)
                _calculationResult.value = result.toString()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calculating exposure"
                _calculationResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculate exposure compensation for given base EV, metering mode, and scene brightness.
     */
    fun calculateExposureCompensation(
        baseEv: Double,
        meteringMode: String,
        sceneBrightness: String
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
                
                val compensation = exposureCalculator.calculateExposureCompensation(
                    baseEv, meteringModeEnum, sceneBrightnessEnum
                )
                
                _calculationResult.value = "Exposure Compensation: ${String.format("%.1f", compensation)} EV"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calculating compensation"
                _calculationResult.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }
}