package com.example.photographyassistant.domain.exposure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExposureCalculatorTest {

    private val exposureCalculator = ExposureCalculator()

    @Test
    fun `calculateExposure_returnsCorrectExposureValue_whenValidInputs()`) {
        val iso = 100.0
        val aperture = 2.8
        val shutterSpeed = 0.5

        val exposureValue = exposureCalculator.calculateExposure(iso, aperture, shutterSpeed)

        assert(exposureValue.iso == iso)
        assert(exposureValue.isValid)
    }
}
