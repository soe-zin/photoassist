package com.example.photographyassistant.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photographyassistant.domain.solar.SolarCalculator
import com.example.photographyassistant.domain.solar.SolarPosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Solar Position feature using MVVM architecture.
 * 
 * This ViewModel handles solar position calculations and UI state management for solar calculations.
 * It provides astronomical calculations for sunrise, sunset, golden hour, and sun direction.
 */

class SolarPositionViewModel : ViewModel() {
    private val solarCalculator = SolarCalculator()
    
    private val _solarPosition = MutableStateFlow<SolarPosition?>(null)
    val solarPosition: StateFlow<SolarPosition?> = _solarPosition
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * Calculate solar position for given date, time, and coordinates.
     */
    fun calculateSolarPosition(
        year: Int,
        month: Int,
        day: Int,
        hour: Double,
        latitude: Double,
        longitude: Double
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val result = solarCalculator.calculateSolarPosition(
                    year, month, day, hour, latitude, longitude
                )
                _solarPosition.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error calculating solar position"
                _solarPosition.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calculate sunrise time for given date and coordinates.
     */
    fun getSunriseTime(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double
    ): Double {
        return solarCalculator.calculateSunriseTime(year, month, day, latitude, longitude)
    }

    /**
     * Calculate sunset time for given date and coordinates.
     */
    fun getSunsetTime(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double
    ): Double {
        return solarCalculator.calculateSunsetTime(year, month, day, latitude, longitude)
    }

    /**
     * Calculate solar noon for given date and coordinates.
     */
    fun getSolarNoon(
        year: Int,
        month: Int,
        day: Int,
        longitude: Double
    ): Double {
        return solarCalculator.calculateSolarNoon(year, month, day, longitude)
    }

    /**
     * Calculate day length for given date and coordinates.
     */
    fun getDayLength(
        year: Int,
        month: Int,
        day: Int,
        latitude: Double,
        longitude: Double
    ): Double {
        val sunrise = getSunriseTime(year, month, day, latitude, longitude)
        val sunset = getSunsetTime(year, month, day, latitude, longitude)
        return sunset - sunrise
    }

    /**
     * Check if current time is within daylight hours.
     */
    fun isDaylightHour(
        year: Int,
        month: Int,
        day: Int,
        hour: Double,
        latitude: Double,
        longitude: Double
    ): Boolean {
        val solarPos = _solarPosition.value ?: return false
        return solarPos.isDay(hour)
    }

    /**
     * Check if current time is within golden hour.
     */
    fun isGoldenHour(
        year: Int,
        month: Int,
        day: Int,
        hour: Double,
        latitude: Double,
        longitude: Double
    ): Boolean {
        val solarPos = _solarPosition.value ?: return false
        return solarPos.isWithinGoldenHour(hour)
    }

    /**
     * Get solar elevation angle at current time.
     */
    fun getSolarElevation(
        year: Int,
        month: Int,
        day: Int,
        hour: Double,
        latitude: Double,
        longitude: Double
    ): Double {
        return solarCalculator.calculateSolarElevation(
            year, month, day, hour, latitude, longitude
        )
    }

    /**
     * Clear current solar position calculation and error messages.
     */
    fun clearResults() {
        _solarPosition.value = null
        _errorMessage.value = null
    }

    /**
     * Get formatted solar position information for display.
     */
    fun getFormattedSolarInfo(): String {
        val position = _solarPosition.value ?: return "No solar data available"
        return "Sunrise: ${String.format("%.2f", position.sunriseTime)}h, " +
               "Sunset: ${String.format("%.2f", position.sunsetTime)}h, " +
               "Max Elevation: ${String.format("%.1f", position.maxSolarElevation)}°"
    }
}