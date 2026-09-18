package com.example.photographyassistant.domain.solar

import java.time.LocalDateTime

/**
 * Value object representing a solar position calculation result.
 * 
 * This is a domain model that captures the sun's position at a given time and location,
 * including sunrise, sunset, and golden hour information. This is part of the solar
 * calculation engine (Task 13).
 */
import kotlin.jvm.JvmInline

@JvmInline
value class SolarPosition(
    private val julianDay: Double,
    private val latitude: Double,
    private val longitude: Double
) {
    val sunLongitude: Double
        get() {
            // Calculate sun's longitude (degrees)
            val daysSinceJ2000 = julianDay - 2451545.0
            val longitude = 280.460 + 0.9856474 * daysSinceJ2000
            return longitude % 360.0
        }
    
    val sunRightAscension: Double
        get() {
            // Calculate right ascension (RA) in degrees
            val obliquity = 23.4393 - 0.0000004 * julianDay
            val ra = kotlin.math.atan2(
                kotlin.math.cos(obliquity.toRad()) * kotlin.math.sin(sunLongitude.toRad()),
                kotlin.math.cos(sunLongitude.toRad())
            )
            return kotlin.math.toDegrees(ra) % 360.0
        }
    
    val sunDeclination: Double
        get() {
            // Calculate declination in degrees
            val obliquity = 23.4393 - 0.0000004 * julianDay
            val declination = kotlin.math.asin(
                kotlin.math.sin(obliquity.toRad()) * kotlin.math.sin(sunLongitude.toRad())
            )
            return kotlin.math.toDegrees(declination)
        }
    
    val sunriseHourAngle: Double
        get() {
            // Calculate sunrise/sunset hour angle
            val declinationRad = sunDeclination.toRad()
            val latitudeRad = latitude.toRad()
            
            val cosHourAngle = -kotlin.math.tan(latitudeRad) * kotlin.math.tan(declinationRad)
            
            return when {
                cosHourAngle <= -1.0 -> kotlin.math.PI
                cosHourAngle >= 1.0 -> 0.0
                else -> kotlin.math.acos(cosHourAngle)
            }
        }
    
    val sunriseTime: Double
        get() {
            // Calculate sunrise time in hours
            val hourAngle = sunriseHourAngle
            val dayFraction = hourAngle / kotlin.math.PI * 12.0 // Convert from radians to hours
            return 12.0 - dayFraction
        }
    
    val sunsetTime: Double
        get() {
            // Calculate sunset time in hours
            val hourAngle = sunriseHourAngle
            val dayFraction = hourAngle / kotlin.math.PI * 12.0
            return 12.0 + dayFraction
        }
    
    val solarNoonTime: Double
        get() {
            // Calculate solar noon time (when sun is at maximum elevation)
            val declinationRad = sunDeclination.toRad()
            val latitudeRad = latitude.toRad()
            val hourAngle = kotlin.math.acos(-kotlin.math.tan(latitudeRad) * kotlin.math.tan(declinationRad))
            return 12.0 + hourAngle / kotlin.math.PI * 12.0
        }
    
    val maxSolarElevation: Double
        get() {
            // Calculate maximum solar elevation angle
            val declinationRad = sunDeclination.toRad()
            val latitudeRad = latitude.toRad()
            return kotlin.math.asin(
                kotlin.math.sin(latitudeRad) * kotlin.math.sin(declinationRad)
            ).toDeg()
        }
    
    val isNighttime: Boolean
        get() {
            val currentHour = getCurrentHourOfDay() // This would be set by the calling code
            return currentHour < sunriseTime || currentHour > sunsetTime
        }
    
    val goldenHourStart: Double
        get() {
            // Golden hour starts ~1 hour before sunset
            return sunsetTime - 1.0
        }
    
    val goldenHourEnd: Double
        get() {
            // Golden hour ends ~1 hour after sunrise
            return sunriseTime + 1.0
        }
    
    fun isWithinGoldenHour(hour: Double): Boolean {
        return hour >= goldenHourStart && hour <= goldenHourEnd
    }
    
    fun isWithinDaylight(hour: Double): Boolean {
        return hour >= sunriseTime && hour <= sunsetTime
    }
    
    fun isDay(hour: Double): Boolean {
        return hour >= sunriseTime && hour <= sunsetTime
    }
    
    fun isNight(hour: Double): Boolean {
        return hour < sunriseTime || hour > sunsetTime
    }
    
    override fun toString(): String {
        return "Julian Day: $julianDay, Sunrise: ${String.format("%.2f", sunriseTime)}h, " +
               "Sunset: ${String.format("%.2f", sunsetTime)}h, Max Elevation: ${String.format("%.1f", maxSolarElevation)}°"
    }
    
    companion object {
        fun calculate(
            year: Int,
            month: Int,
            day: Int,
            hour: Double = 12.0,
            latitude: Double,
            longitude: Double
        ): SolarPosition {
            // Convert to Julian Day
            val julianDay = calculateJulianDay(year, month, day, hour)
            return SolarPosition(julianDay, latitude, longitude)
        }
        
        private fun calculateJulianDay(year: Int, month: Int, day: Int, hour: Double): Double {
            // Simplified Julian Day calculation
            val a = kotlin.math.floor((14 - month) / 12.0)
            val y = year + 4800 - a.toInt()
            val m = month + 12 * a - 3
            
            val julianDay = day + kotlin.math.floor((153 * m + 2) / 5.0) +
                   365 * y + kotlin.math.floor(y / 4.0) -
                   kotlin.math.floor(y / 100.0) + kotlin.math.floor(y / 400.0) - 32045 +
                    hour / 24.0
            
            return julianDay
        }
        
        // Extension functions for unit conversions
companion object {
        fun calculate(
            year: Int,
            month: Int,
            day: Int,
            hour: Double = 12.0,
            latitude: Double,
            longitude: Double
        ): SolarPosition {
            // Convert to Julian Day
            val julianDay = calculateJulianDay(year, month, day, hour)
            return SolarPosition(julianDay, latitude, longitude)
        }
        
        private fun calculateJulianDay(year: Int, month: Int, day: Int, hour: Double): Double {
            // Simplified Julian Day calculation
            val a = kotlin.math.floor((14 - month) / 12.0)
            val y = year + 4800 - a.toInt()
            val m = month + 12 * a - 3
            
            val julianDay = day + kotlin.math.floor((153 * m + 2) / 5.0) +
                   365 * y + kotlin.math.floor(y / 4.0) -
                   kotlin.math.floor(y / 100.0) + kotlin.math.floor(y / 400.0) - 32045 +
                     hour / 24.0
            
            return julianDay
        }
        
        // Extension functions for unit conversions
        private fun Double.toRad(): Double = kotlin.math.toRadians(this)
        private fun Double.toDeg(): Double = kotlin.math.toDegrees(this)
    }
        private fun Double.toRad(): Double = kotlin.math.toRadians(this)
        private fun Double.toDeg(): Double = kotlin.math.toDegrees(this)
    }
}