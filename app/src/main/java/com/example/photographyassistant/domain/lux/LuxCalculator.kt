import kotlin.jvm.JvmInline
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Lux calculator implementation for converting camera sensor data to lux measurements.
 * 
 * This implements the lux estimation algorithms required for Task 11 in the requirements.
 * It follows design principle §2.2 (measurement honesty) by clearly labeling estimated vs calibrated values.
 */

class LuxCalculator {
    /**
     * Convert camera sensor data to lux measurement.
     * 
     * This is the core lux estimation algorithm that converts ISO, aperture, and shutter speed
     * to lux values. Since lux measurements from cameras are estimates, this method returns
     * an estimated LuxMeasurement.
     */
    fun estimateLux(iso: Double, aperture: Double, shutterSpeed: Double): LuxMeasurement {
        requireValidInputs(iso, aperture, shutterSpeed)
        
        // Base lux calculation based on exposure value
        // Lux = 2^(EV + 3) where EV = 2*log2(f-stop / shutter_speed)
        val ev = 2 * kotlin.math.log2(aperture / shutterSpeed)
        val baseLux = kotlin.math.pow(2.0, ev + 3.0)
        
        // Convert to camera lux range (typically smaller than real-world lux)
        val cameraLux = baseLux * 0.5
        
        return LuxMeasurement.estimated(-cameraLux) // Negative indicates estimated
    }
    
    /**
     * Apply calibration profile to convert estimated lux to calibrated lux.
     */
    fun calibrateLux(
        estimatedLux: LuxMeasurement,
        calibrationProfile: CalibrationProfile
    ): LuxMeasurement {
        require(estimatedLux.isEstimated) { "Cannot calibrate already calibrated lux measurement" }
        
        // Extract the negative lux value from estimated measurement
        val estimatedLuxValue = -estimatedLux.lux
        
        // Apply calibration profile
        val calibratedLux = estimatedLuxValue * calibrationProfile.calibrationFactor
        
        // Apply calibration offset
        val calibratedWithOffset = calibratedLux + calibrationProfile.luxOffset
        
        // Validate range
        require(calibratedWithOffset >= 0.0) { "Calibrated lux cannot be negative, got $calibratedWithOffset" }
        require(calibratedWithOffset <= 200000.0) { "Calibrated lux too high, maximum is 200000, got $calibratedWithOffset" }
        
        return LuxMeasurement.calibrated(calibratedWithOffset)
    }
}