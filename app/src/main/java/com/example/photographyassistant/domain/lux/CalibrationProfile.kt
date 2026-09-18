package com.example.photographyassistant.domain.lux

/**
 * Camera calibration profile for converting estimated lux to calibrated lux measurements.
 * 
 * This defines how to transform camera sensor readings into accurate lux values
 * through calibration factors and offsets. This is used in the lux calibration process
 * (Task 12) to improve measurement accuracy.
 */

import kotlin.jvm.JvmInline

@JvmInline
value class CalibrationProfile(
    private val calibrationFactor: Double,
    private val luxOffset: Double
) {
    val factor
        get() = calibrationFactor
    
    val offset
        get() = luxOffset
    
    val isValid: Boolean
        get() = calibrationFactor > 0.0 && calibrationFactor <= 10.0 &&
                luxOffset >= -50000.0 && luxOffset <= 50000.0
    
    override fun toString(): String {
        return "Factor: $calibrationFactor, Offset: $luxOffset"
    }
    
    companion object {
        /**
         * Create a calibration profile with default values for typical camera.
         */
        fun default(): CalibrationProfile {
            return CalibrationProfile(1.2, 100.0)
        }
        
        /**
         * Create a calibration profile for low-light conditions.
         */
        fun lowLight(): CalibrationProfile {
            return CalibrationProfile(1.5, 50.0)
        }
        
        /**
         * Create a calibration profile for bright conditions.
         */
        fun bright(): CalibrationProfile {
            return CalibrationProfile(0.9, -50.0)
        }
    }
}