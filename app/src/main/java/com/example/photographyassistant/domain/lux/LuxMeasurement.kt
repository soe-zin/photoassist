package com.example.photographyassistant.domain.lux

import java.time.Instant

/**
 * Value object representing a lux measurement.
 * 
 * This domain model distinguishes between calibrated and estimated lux values,
 * adhering to requirements §2.2 (measurement honesty) and §2.4 (deterministic calculations).
 */
import kotlin.jvm.JvmInline

@JvmInline
value class LuxMeasurement(
    private val rawLux: Double
) {
    val lux: Double
        get() = rawLux
    
    val isCalibrated: Boolean
        get() = rawLux >= 0.0
    
    val isEstimated: Boolean
        get() = !isCalibrated
    
    val calibrationStatus: CalibrationStatus
        get() = when {
            rawLux >= 0.0 -> CalibrationStatus.CALIBRATED
            rawLux > -50.0 -> CalibrationStatus.PARTIALLY_CALIBRATED
            else -> CalibrationStatus.UNCALIBRATED
        }
    
    val displayValue: Double
        get() = when {
            isCalibrated -> rawLux
            else -> -rawLux // Show absolute value for estimated readings
        }
    
    val accuracy: AccuracyLevel
        get() = when {
            rawLux >= 0.0 -> AccuracyLevel.HIGH
            rawLux > -100.0 -> AccuracyLevel.MEDIUM
            else -> AccuracyLevel.LOW
        }
    
    val isValid: Boolean
        get() = lux >= -50000.0 && lux <= 200000.0
    
    override fun toString(): String {
        val status = if (isCalibrated) "Calibrated" else "Estimated"
        return "${String.format("%.0f", displayValue)} lux ($status)"
    }
    
    companion object {
        fun calibrated(lux: Double): LuxMeasurement {
            require(lux >= 0.0) { "Calibrated lux must be non-negative, got $lux" }
            require(lux <= 200000.0) { "Calibrated lux too high, maximum is 200000, got $lux" }
            return LuxMeasurement(lux)
        }
        
        fun estimated(lux: Double): LuxMeasurement {
            require(lux < 0.0) { "Estimated lux must be negative, got $lux" }
            require(lux >= -50000.0) { "Estimated lux too low, minimum is -50000, got $lux" }
            return LuxMeasurement(lux)
        }
    }
}

enum class CalibrationStatus {
    CALIBRATED,
    PARTIALLY_CALIBRATED,
    UNCALIBRATED
}

enum class AccuracyLevel {
    HIGH,
    MEDIUM,
    LOW
}