package com.example.photographyassistant.feature.exposuremeter

/**
 * Exposure recommendation engine for generating intelligent exposure recommendations.
 * 
 * This implements Task 4: Create algorithm to generate exposure recommendations based on light meter readings.
 * It provides smart exposure suggestions considering metering modes and scene brightness.
 */

import com.example.photographyassistant.domain.exposure.ExposureCalculator
import com.example.photographyassistant.domain.exposure.ExposureValue

class ExposureRecommendationEngine(
    private val exposureCalculator: ExposureCalculator = ExposureCalculator()
) {
    /**
     * Generate exposure recommendation based on current light conditions.
     * 
     * This is the core recommendation algorithm that analyzes current metering conditions
     * and provides optimal exposure settings.
     */
    fun generateRecommendation(
        currentEv: Double,
        meteringMode: MeteringMode,
        sceneBrightness: SceneBrightness,
        preferredAperture: Double? = null,
        preferredShutterSpeed: Double? = null
    ): ExposureRecommendation {
        // Calculate base compensation based on metering mode and scene
        val compensation = exposureCalculator.calculateExposureCompensation(
            currentEv, meteringMode, sceneBrightness
        )
        
        // Calculate recommended settings
        val recommendedAperture = if (preferredAperture != null) {
            preferredAperture
        } else {
            // Calculate optimal aperture for current conditions
            val recommendedIso = kotlin.math.pow(2.0, compensation + 3.0)
            exposureCalculator.calculateOptimalAperture(recommendedIso, 1.0 / 125.0)
        }
        
        val recommendedShutterSpeed = if (preferredShutterSpeed != null) {
            preferredShutterSpeed
        } else {
            1.0 / 125.0 // Default shutter speed
        }
        
        // Calculate final ISO based on compensation
        val finalEv = currentEv + compensation
        val finalIso = kotlin.math.pow(2.0, finalEv + 3.0)
        
        return ExposureRecommendation(
            exposureValue = ExposureValue(finalIso),
            meteringMode = meteringMode,
            sceneBrightness = sceneBrightness,
            compensation = compensation,
            recommendedAperture = recommendedAperture,
            recommendedShutterSpeed = recommendedShutterSpeed,
            confidence = calculateConfidence(meteringMode, sceneBrightness)
        )
    }
    
    /**
     * Generate multiple exposure options for the photographer to choose from.
     */
    fun generateExposureOptions(
        currentEv: Double,
        meteringMode: MeteringMode,
        sceneBrightness: SceneBrightness
    ): List<ExposureRecommendation> {
        val options = mutableListOf<ExposureRecommendation>()
        
        // Generate under, correct, and overexposed options
        val evVariations = listOf(-1.0, 0.0, 1.0)
        
        for (evVariation in evVariations) {
            val targetEv = currentEv + evVariation
            val recommendation = generateRecommendation(
                currentEv = targetEv,
                meteringMode = meteringMode,
                sceneBrightness = sceneBrightness,
                preferredAperture = exposureCalculator.calculateOptimalAperture(
                    kotlin.math.pow(2.0, targetEv + 3.0), 1.0 / 125.0
                ),
                preferredShutterSpeed = 1.0 / 125.0
            )
            options.add(recommendation)
        }
        
        return options
    }
    
    /**
     * Calculate confidence level for the recommendation based on metering conditions.
     */
    private fun calculateConfidence(meteringMode: MeteringMode, sceneBrightness: SceneBrightness): ConfidenceLevel {
        val meteringConfidence = when (meteringMode) {
            MeteringMode.MULTI -> ConfidenceLevel.HIGH
            MeteringMode.PATTERN -> ConfidenceLevel.MEDIUM
            MeteringMode.SPATIAL -> ConfidenceLevel.MEDIUM
            MeteringMode.CENTER_WEIGHTED -> ConfidenceLevel.LOW
        }
        
        val sceneConfidence = when (sceneBrightness) {
            SceneBrightness.NORMAL -> ConfidenceLevel.HIGH
            SceneBrightness.DARK, SceneBrightness.BRIGHT -> ConfidenceLevel.MEDIUM
            SceneBrightness.HIGHLIGHT -> ConfidenceLevel.LOW
        }
        
        return when {
            meteringConfidence == ConfidenceLevel.HIGH && sceneConfidence == ConfidenceLevel.HIGH -> ConfidenceLevel.HIGH
            meteringConfidence == ConfidenceLevel.LOW || sceneConfidence == ConfidenceLevel.LOW -> ConfidenceLevel.LOW
            else -> ConfidenceLevel.MEDIUM
        }
    }
}

/**
 * Data class representing an exposure recommendation.
 */
data class ExposureRecommendation(
    val exposureValue: ExposureValue,
    val meteringMode: MeteringMode,
    val sceneBrightness: SceneBrightness,
    val compensation: Double,
    val recommendedAperture: Double,
    val recommendedShutterSpeed: Double,
    val confidence: ConfidenceLevel
) {
    val isRecommended: Boolean
        get() = confidence != ConfidenceLevel.LOW
    
    val description: String
        get() = when {
            compensation < -0.5 -> "Underexposed recommendation (${String.format("%.1f", compensation)} EV)"
            compensation > 0.5 -> "Overexposed recommendation (${String.format("%.1f", compensation)} EV)"
            else -> "Standard exposure recommendation"
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

enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW
}