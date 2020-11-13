package io.perfometer.statistics

import io.perfometer.http.HttpMethod
import io.perfometer.http.HttpStatus
import java.time.Duration
import java.time.Instant

/**
 * Set of data classes representing different statistics collected during scenario life cycle.
 *
 * @author Tomasz Tarczyński
 */
sealed class Statistics

data class RequestStatistics(
    val name: String,
    val method: HttpMethod,
    val pathWithParams: String,
    val startTime: Instant,
    val endTime: Instant,
    val httpStatus: HttpStatus,
) : Statistics() {

    val timeTaken: Duration = Duration.between(startTime, endTime)
}

internal data class PauseStatistics(
    val duration: Duration,
) : Statistics()
