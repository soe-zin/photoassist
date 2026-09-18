package com.example.photographyassistant.domain.exposure

/**
 * Core exposure calculator implementation.
 * 
 * This provides deterministic mathematical calculations for exposure values,
 * adhering to requirements §2.4 (deterministic mathematical core).
 */
import kotlin.math.pow

class ExposureCalculator {
    /**
     * Calculate exposure value from ISO, aperture, and shutter speed.
     * 
     * Uses the formula: EV = log2(N^2 / t) where N = f-number, t = shutter speed
     * 
     * @param iso ISO sensitivity (typically 100-12800)
     * @param aperture f-stop value (e.g., 2.8, 5.6)
     * @param shutterSpeed seconds as decimal (e.g., 0.25, 0.5, 1.0)
     * @return ExposureValue with calculated components
     */
    fun calculateExposure(iso: Double, aperture: Double, shutterSpeed: Double): ExposureValue {
        validateInputs(iso, aperture, shutterSpeed)
        
        // Exposure Value calculation: EV = 2 * log2(f-stop / shutter_speed)
        // For reference: EV = log2(N^2 / t)
        val ev = 2 * kotlin.math.log2(aperture / shutterSpeed)
        
        // Convert back to ISO for the ExposureValue object
        // Using standard ISO scale where EV 0 = ISO 100 at f/1 with 1s
        val calculatedIso = kotlin.math.pow(2.0, ev + 3.0) // EV + 3 = log2(ISO/100)
        
        return ExposureValue(calculatedIso)
    }
    
    /**
     * Calculate exposure recommendation based on current light conditions.
     * 
     * This is the exposure recommendation engine (Task 4).
     */
    fun calculateExposureRecommendation(
        currentEv: Double,
        desiredAperture: Double,
        preferredShutterSpeed: Double
    ): ExposureValue {
        // Simple recommendation: adjust ISO to match preferred settings
        val baseEv = 2 * kotlin.math.log2(desiredAperture / preferredShutterSpeed)
        val recommendedIso = kotlin.math.pow(2.0, baseEv + 3.0)
        
        return ExposureValue(recommendedIso)
    }
    
    /**
     * Calculate optimal aperture for given ISO and shutter speed.
     */
    fun calculateOptimalAperture(iso: Double, shutterSpeed: Double): Double {
        // Solve for aperture: aperture = sqrt(2^EV / t)
        val ev = kotlin.math.log2(iso / 100.0) - 3.0
        val targetAperture = kotlin.math.sqrt(kotlin.math.pow(2.0, ev) / shutterSpeed)
        
        // Round to standard f-stop values
        val standardStops = listOf(1.4, 2.0, 2.8, 4.0, 5.6, 8.0, 11.0, 16.0, 22.0)
        return standardStops.minByOrNull { kotlin.math.abs(it - targetAperture) } ?: targetAperture
    }
    
    /**
     * Validate input parameters for exposure calculations.
     */
    private fun validateInputs(iso: Double, aperture: Double, shutterSpeed: Double) {
        require(iso > 0) { "ISO must be positive, got $iso" }
        require(iso <= 12800) { "ISO too high, maximum is 12800, got $iso" }
        require(aperture > 0) { "Aperture must be positive, got $aperture" }
        require(aperture <= 64.0) { "Aperture too large, maximum is f/64, got f/$aperture" }
        require(shutterSpeed > 0) { "Shutter speed must be positive, got $shutterSpeed" }
        require(shutterSpeed <= 1.0 / 8000) { "Shutter speed too fast, maximum is 1/8000s, got ${1/shutterSpeed}s" }
        require(shutterSpeed >= 1.0 / 1000000) { "Shutter speed too slow, minimum is 1/1000000s, got ${1/shutterSpeed}s" }
    }
    
    /**
     * Calculate exposure compensation based on metering mode and scene brightness.
     */
    fun calculateExposureCompensation(
        baseEv: Double,
        meteringMode: MeteringMode,
        sceneBrightness: SceneBrightness
    ): Double {
        val meteringOffset = when (meteringMode) {
            MeteringMode.CENTER_WEIGHTED -> 0.0
            MeteringMode.SPATIAL -> -0.3
            MeteringMode.PATTERN -> -0.5
            MeteringMode.MULTI -> -0.7
        }
        
        val sceneOffset = when (sceneBrightness) {
            SceneBrightness.DARK -> -1.0
            SceneBrightness.NORMAL -> 0.0
            SceneBrightness.BRIGHT -> 1.0
            SceneBrightness.HIGHLIGHT -> 1.5
        }
        
        return baseEv + meteringOffset + sceneOffset
    }
}

enum class MeteringMode {
    CENTER_WEIGHTED,
    SPATIAL,
    PATTERN,
    MULTI
}

enum class SceneBrightness {
    DARK,
    NORMAL,
    BRIGHT,
    HIGHLIGHT
}