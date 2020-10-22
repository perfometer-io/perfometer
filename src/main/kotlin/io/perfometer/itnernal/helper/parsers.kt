package io.perfometer.itnernal.helper

import io.perfometer.exception.InvalidScenarioConfigurationException
import java.net.URL

internal fun String.toUrl(): URL {
    return try {
         URL(this)
    } catch (e: Exception) {
        throw InvalidScenarioConfigurationException("Invalid URL string", e)
    }
}
