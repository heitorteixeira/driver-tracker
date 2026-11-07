package com.driverprofit.domain

enum class Verdict { GREEN, YELLOW, RED }

/**
 * The driver's vehicle costs and personal minimums. All distance values are in the
 * unit chosen by the user (km or miles); the math is the same either way.
 */
data class CostProfile(
    val fuelPrice: Double,
    val efficiency: Double,
    val maintenancePerDistance: Double,
    val minPerDistance: Double,
    val minPerHour: Double,
) {
    /** Fuel spend per distance unit (fuel price / efficiency). */
    val fuelCostPerDistance: Double
        get() = if (efficiency > 0) fuelPrice / efficiency else 0.0

    val costPerDistance: Double
        get() = fuelCostPerDistance + maintenancePerDistance
}

data class RideOffer(
    val fare: Double,
    val distance: Double,
    val pickupDistance: Double,
    val minutes: Double,
)

data class RideResult(
    val perDistance: Double,
    val perHour: Double,
    val cost: Double,
    /** The fuel part of [cost]; the rest is maintenance and wear. */
    val fuelCost: Double,
    val net: Double,
    val margin: Double,
    val verdict: Verdict,
)

object RideCalculator {

    /** Returns null when the offer is incomplete (zero fare, distance or time). */
    fun evaluate(offer: RideOffer, profile: CostProfile): RideResult? {
        val totalDistance = offer.distance + offer.pickupDistance
        if (offer.fare <= 0 || totalDistance <= 0 || offer.minutes <= 0) return null

        val cost = totalDistance * profile.costPerDistance
        val net = offer.fare - cost
        val perDistance = offer.fare / totalDistance
        val perHour = offer.fare / (offer.minutes / 60.0)

        val verdict = when {
            net <= 0 -> Verdict.RED
            perDistance >= profile.minPerDistance && perHour >= profile.minPerHour -> Verdict.GREEN
            else -> Verdict.YELLOW
        }
        return RideResult(
            perDistance = perDistance,
            perHour = perHour,
            cost = cost,
            fuelCost = totalDistance * profile.fuelCostPerDistance,
            net = net,
            margin = net / offer.fare,
            verdict = verdict,
        )
    }
}
