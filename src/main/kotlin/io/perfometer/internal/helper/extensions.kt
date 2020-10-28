package io.perfometer.internal.helper

import io.perfometer.exception.InvalidScenarioConfigurationException
import java.net.URL
import java.time.Instant
import java.time.ZoneId

internal fun String.toUrl(): URL {
    return try {
        URL(this)
    } catch (e: Exception) {
        throw InvalidScenarioConfigurationException("Invalid URL string", e)
    }
}

internal fun Instant.toZonedDateTimeUTC() = this.atZone(ZoneId.of("UTC"))
