package com.example

import com.example.domain.model.BreathingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun boxBreathing_hasEqualPhases() {
        val box = BreathingMode.BOX
        assertEquals(4, box.inhaleSeconds)
        assertEquals(4, box.holdAfterInhaleSeconds)
        assertEquals(4, box.exhaleSeconds)
        assertEquals(4, box.holdAfterExhaleSeconds)
        val cycleTime = box.inhaleSeconds + box.holdAfterInhaleSeconds + box.exhaleSeconds + box.holdAfterExhaleSeconds
        assertEquals(16, cycleTime)
    }

    @Test
    fun relax478_hasCorrectPhases() {
        val relax = BreathingMode.RELAX_478
        assertEquals(4, relax.inhaleSeconds)
        assertEquals(7, relax.holdAfterInhaleSeconds)
        assertEquals(8, relax.exhaleSeconds)
        assertEquals(0, relax.holdAfterExhaleSeconds)
    }

    @Test
    fun calorieEstimation_fromSteps_calculatesSensibly() {
        val steps = 7842
        val estimatedBurn = (steps * 0.045f).toInt() + 130
        assertTrue(estimatedBurn in 450..550)
    }

    @Test
    fun waterIntake_percentage_calculatesProperly() {
        val currentMl = 1600
        val goalMl = 2500
        val percentage = (currentMl.toFloat() / goalMl) * 100f
        assertEquals(64f, percentage, 0.1f)
    }
}
