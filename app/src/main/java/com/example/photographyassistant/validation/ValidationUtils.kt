package com.example.photographyassistant.validation

/**
 * Validation utilities for the photography assistant application.
 * 
 * This provides input validation logic for domain models and calculations,
 * ensuring data integrity and adherence to business rules defined in requirements.
 */

class ValidationUtils {
    /**
     * Validate ISO value for exposure calculations.
     * 
     * @param iso ISO value to validate
     * @return true if valid, false otherwise
     */
    fun isValidIso(iso: Double): Boolean {
        return iso > 0 && iso <= 12800
    }
    
    /**
     * Validate aperture value for exposure calculations.
     * 
     * @param aperture f-stop value to validate
     * @return true if valid, false otherwise
     */
    fun isValidAperture(aperture: Double): Boolean {
        return aperture > 0 && aperture <= 64.0
    }
    
    /**
     * Validate shutter speed for exposure calculations.
     * 
     * @param shutterSpeed shutter speed in seconds to validate
     * @return true if valid, false otherwise
     */
    fun isValidShutterSpeed(shutterSpeed: Double): Boolean {
        return shutterSpeed > 0 && shutterSpeed <= 1.0 / 8000 && shutterSpeed >= 1.0 / 1000000
    }
    
    /**
     * Validate geographic coordinates.
     * 
     * @param latitude Latitude (-90 to 90)
     * @param longitude Longitude (-180 to 180)
     * @return true if valid, false otherwise
     */
    fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
        return latitude >= -90.0 && latitude <= 90.0 &&
               longitude >= -180.0 && longitude <= 180.0
    }
    
    /**
     * Validate camera calibration factor.
     * 
     * @param factor Calibration factor to validate
     * @return true if valid, false otherwise
     */
    fun isValidCalibrationFactor(factor: Double): Boolean {
        return factor > 0.0 && factor <= 10.0
    }
    
    /**
     * Validate calibration offset.
     * 
     * @param offset Offset value to validate
     * @return true if valid, false otherwise
     */
    fun isValidCalibrationOffset(offset: Double): Boolean {
        return offset >= -50000.0 && offset <= 50000.0
    }
    
    /**
     * Validate lux measurement value.
     * 
     * @param lux Lux value to validate
     * @param isEstimated Whether this is an estimated measurement
     * @return true if valid, false otherwise
     */
    fun isValidLux(lux: Double, isEstimated: Boolean): Boolean {
        return when {
            isEstimated -> lux >= -50000.0 && lux < 0.0
            else -> lux >= 0.0 && lux <= 200000.0
        }
    }
    
    /**
     * Validate solar position calculation parameters.
     * 
     * @param latitude Latitude (-90 to 90)
     * @param longitude Longitude (-180 to 180)
     * @return true if valid, false otherwise
     */
    fun isValidSolarPosition(latitude: Double, longitude: Double): Boolean {
        return latitude >= -90.0 && latitude <= 90.0 &&
               longitude >= -180.0 && longitude <= 180.0
    }
    
    /**
     * Validate date parameters for solar calculations.
     * 
     * @param year Year to validate
     * @param month Month (1-12) to validate
     * @param day Day (1-31) to validate
     * @param hour Hour (0-24) to validate
     * @return true if valid, false otherwise
     */
    fun isValidDateTime(year: Int, month: Int, day: Int, hour: Double): Boolean {
        return year >= 2000 && year <= 2100 &&
               month >= 1 && month <= 12 &&
               day >= 1 && day <= 31 &&
               hour >= 0.0 && hour <= 24.0
    }
    
    /**
     * Get validation error message for ISO value.
     */
    fun getIsoErrorMessage(iso: Double): String {
        return when {
            iso <= 0 -> "ISO must be positive"
            iso > 12800 -> "ISO too high, maximum is 12800"
            else -> ""
        }
    }
    
    /**
     * Get validation error message for aperture value.
     */
    fun getApertureErrorMessage(aperture: Double): String {
        return when {
            aperture <= 0 -> "Aperture must be positive"
            aperture > 64.0 -> "Aperture too large, maximum is f/64"
            else -> ""
        }
    }
    
    /**
     * Get validation error message for shutter speed value.
     */
    fun getShutterSpeedErrorMessage(shutterSpeed: Double): String {
        return when {
            shutterSpeed <= 0 -> "Shutter speed must be positive"
            shutterSpeed > 1.0 / 8000 -> "Shutter speed too fast, maximum is 1/8000s"
            shutterSpeed < 1.0 / 1000000 -> "Shutter speed too slow, minimum is 1/1000000s"
            else -> ""
        }
    }
}