package com.heitor.driverprofit.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RideCalculatorTest {

    private val profile = CostProfile(
        fuelPrice = 6.0,
        efficiency = 10.0,
        maintenancePerDistance = 0.4,
        minPerDistance = 2.0,
        minPerHour = 30.0,
    )

    @Test
    fun costPerDistance_combinesFuelAndMaintenance() {
        assertEquals(1.0, profile.costPerDistance, 1e-9)
    }

    @Test
    fun goodRide_isGreen() {
        // 20 for 8 km in 20 min: 2.5/km, 60/h, cost 8, net 12
        val result = RideCalculator.evaluate(RideOffer(20.0, 8.0, 0.0, 20.0), profile)
        assertNotNull(result)
        assertEquals(Verdict.GREEN, result!!.verdict)
        assertEquals(2.5, result.perDistance, 1e-9)
        assertEquals(60.0, result.perHour, 1e-9)
        assertEquals(12.0, result.net, 1e-9)
        assertEquals(0.6, result.margin, 1e-9)
    }

    @Test
    fun belowMinimumsButProfitable_isYellow() {
        // 12 for 8 km in 20 min: 1.5/km (below 2.0), net 4
        val result = RideCalculator.evaluate(RideOffer(12.0, 8.0, 0.0, 20.0), profile)
        assertEquals(Verdict.YELLOW, result!!.verdict)
    }

    @Test
    fun losingMoney_isRed() {
        // 6 for 8 km: cost 8, net -2
        val result = RideCalculator.evaluate(RideOffer(6.0, 8.0, 0.0, 20.0), profile)
        assertEquals(Verdict.RED, result!!.verdict)
    }

    @Test
    fun pickupDistance_countsTowardCostAndRate() {
        // 20 for 8 km trip + 4 km pickup: total 12 km, cost 12, net 8
        val result = RideCalculator.evaluate(RideOffer(20.0, 8.0, 4.0, 30.0), profile)
        assertEquals(12.0, result!!.cost, 1e-9)
        assertEquals(8.0, result.net, 1e-9)
        assertEquals(20.0 / 12.0, result.perDistance, 1e-9)
    }

    @Test
    fun incompleteOffer_returnsNull() {
        assertNull(RideCalculator.evaluate(RideOffer(0.0, 5.0, 0.0, 10.0), profile))
        assertNull(RideCalculator.evaluate(RideOffer(10.0, 0.0, 0.0, 10.0), profile))
        assertNull(RideCalculator.evaluate(RideOffer(10.0, 5.0, 0.0, 0.0), profile))
    }
}
