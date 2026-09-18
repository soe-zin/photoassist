package com.example.photographyassistant.feature.exposuremeter

import android.graphics.Bitmap
import android.graphics.Color
import com.example.photographyassistant.domain.exposure.ExposureCalculator
import com.example.photographyassistant.domain.exposure.ExposureValue
import com.example.photographyassistant.domain.exposure.MeteringMode
import com.example.photographyassistant.domain.exposure.SceneBrightness

/**
 * Exposure analyzer for camera frame processing.
 * 
 * Analyzes camera frames to calculate exposure values and recommendations.
 */
class ExposureMeterAnalyzer(
    private val exposureCalculator: ExposureCalculator = ExposureCalculator()
) {
    
    /**
     * Analyze camera frame for exposure calculation.
     */
    fun analyzeFrame(
        frame: Bitmap,
        meteringMode: MeteringMode,
        sceneBrightness: SceneBrightness,
        currentEv: Double
    ): ExposureResult {
        try {
            // Calculate average brightness
            val totalBrightness = calculateAverageBrightness(frame)
            val brightnessVariation = calculateBrightnessVariation(frame, totalBrightness)
            
            // Calculate recommended exposure
            val recommendedEv = calculateRecommendedExposure(
                totalBrightness, brightnessVariation, meteringMode, sceneBrightness, currentEv
            )
            
            return ExposureResult(
                exposureValue = recommendedEv,
                meteringMode = meteringMode,
                sceneBrightness = sceneBrightness,
                confidenceScore = calculateConfidenceScore(brightnessVariation, totalBrightness),
                averageBrightness = totalBrightness / frame.width / frame.height
            )
        } catch (e: Exception) {
            return ExposureResult(
                exposureValue = ExposureValue(100.0),
                meteringMode = meteringMode,
                sceneBrightness = sceneBrightness,
                confidenceScore = 0.0,
                errorMessage = e.message
            )
        }
    }
    
    private fun calculateAverageBrightness(frame: Bitmap): Double {
        var totalBrightness = 0.0
        val totalPixels = frame.width * frame.height
        
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                val pixel = frame.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
                totalBrightness += brightness
            }
        }
        
        return totalBrightness / totalPixels
    }
    
    private fun calculateBrightnessVariation(frame: Bitmap, averageBrightness: Double): Double {
        var sumSquares = 0.0
        val totalPixels = frame.width * frame.height
        
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                val pixel = frame.getPixel(x, y)
                val brightness = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3.0
                val diff = brightness - averageBrightness
                sumSquares += diff * diff
            }
        }
        
        return kotlin.math.sqrt(sumSquares / totalPixels)
    }
    
    private fun calculateRecommendedExposure(
        averageBrightness: Double,
        brightnessVariation: Double,
        meteringMode: MeteringMode,
        sceneBrightness: SceneBrightness,
        currentEv: Double
    ): ExposureValue {
        val targetBrightness = 128.0
        val exposureCompensation = (targetBrightness - averageBrightness) / 50.0
        
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
        
        val recommendedEv = currentEv + exposureCompensation + meteringOffset + sceneOffset
        val recommendedIso = kotlin.math.pow(2.0, recommendedEv + 3.0)
        
        return ExposureValue(recommendedIso)
    }
    
    private fun calculateConfidenceScore(brightnessVariation: Double, averageBrightness: Double): Double {
        var score = 1.0
        
        if (brightnessVariation > 30.0) score -= 0.3
        if (brightnessVariation < 10.0) score -= 0.1
        if (averageBrightness < 20.0 || averageBrightness > 235.0) score -= 0.2
        
        return kotlin.math.max(0.0, score)
    }
}