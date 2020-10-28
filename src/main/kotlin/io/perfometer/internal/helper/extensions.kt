package io.perfometer.internal.helper

import io.perfometer.exception.InvalidScenarioConfigurationException
import java.net.URL
import java.time.Duration
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

internal fun Duration.toReadableString(): String {
    val seconds = this.seconds
    val positive = String.format(
        "%d:%02d:%02d.%03d",
        this.toHours(),
        this.toMinutes() % 60,
        this.seconds % 60,
        this.toMillis() % 1000
    )
    return if (seconds < 0) "-$positive" else positive
}
