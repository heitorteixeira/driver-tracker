package com.driverprofit.domain

enum class Platform(val brand: String?) {
    UBER("Uber"),
    NINETY_NINE("99"),
    INDRIVE("inDrive"),
    BOLT("Bolt"),
    OTHER(null);

    companion object {
        fun fromName(name: String): Platform =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
