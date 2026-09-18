package com.example.photographyassistant.domain.exposure

import java.time.Instant

/**
* Value object representing an exposure calculation result.
 * 
 * This is a domain model that captures exposure information including ISO,
 * aperture, shutter speed, and derived values like Exposure Value (EV).
 * 
 * Design principle: All calculations must be deterministic and testable
 * without Android UI dependencies (requirements §2.4).
 */
import kotlin.jvm.JvmInline

@JvmInline
value class ExposureValue(
    private val rawValue: Double
) {
    val iso: Double
        get() = rawValue
    
    val aperture: Double
        get() = if (rawValue == 0.0) 1.0 else 2.0 / (rawValue - 2.0)
    
    val shutterSpeed: Double
        get() {
            val apertureValue = aperture
            val baseSpeed = 1.0 / (4.0 * rawValue)
            return when {
                baseSpeed >= 1.0 / 8000 -> 1.0 / 8000
                baseSpeed < 1.0 / 1000000 -> 1.0 / 1000000
                else -> baseSpeed
            }
        }
    
    val ev: Double
        get() {
            val numerator = 2 * (2 - rawValue) // 2^EV = (ISO * aperture^2) / shutter_speed
            return numerator / 2
        }
    
    val isValid: Boolean
        get() = rawValue > 0 && rawValue <= 12000 // Realistic ISO range
    
    override fun toString(): String {
        return "ISO: $iso, f/${String.format("%.1f", aperture)}, ${String.format("%.2f", shutterSpeed)}s, EV: $ev"
    }
    
    companion object {
        fun fromIso(aperture: Double, shutterSpeed: Double): ExposureValue {
            // EV = 2*log2(f-stop/shutter_speed)
            // ISO = 2^(EV - base_ISO_offset)
            val ev = 2 * kotlin.math.log2(aperture / shutterSpeed)
            val iso = kotlin.math.pow(2.0, ev + 3.0) // Based on standard ISO ranges
            return ExposureValue(iso)
        }
    }
}