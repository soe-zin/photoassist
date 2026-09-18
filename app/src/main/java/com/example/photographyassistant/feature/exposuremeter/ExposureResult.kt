package com.example.photographyassistant.feature.exposuremeter

import com.example.photographyassistant.domain.exposure.ExposureValue
import com.example.photographyassistant.domain.exposure.MeteringMode
import com.example.photographyassistant.domain.exposure.SceneBrightness

/**
 * Exposure analysis result.
 * 
 * @param exposureValue Calculated exposure value
 * @param meteringMode Metering mode used
 * @param sceneBrightness Scene brightness assessment
 * @param confidenceScore Confidence in calculation (0.0 to 1.0)
 * @param averageBrightness Average brightness of analyzed frame
 * @param brightnessVariation Brightness variation across frame
 * @param errorMessage Error message if analysis failed
 */
data class ExposureResult(
    val exposureValue: ExposureValue,
    val meteringMode: MeteringMode,
    val sceneBrightness: SceneBrightness,
    val confidenceScore: Double,
    val averageBrightness: Double,
    val brightnessVariation: Double,
    val errorMessage: String? = null
) {
    /**
     * Check if result is reliable based on confidence score.
     * 
     * @return true if result confidence is above threshold
     */
    fun isReliable(): Boolean = confidenceScore > 0.6
}